package sws.project.magic;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Uses automagic (and reflection) to generate a form
 * for editing an object. Don't question the magic.
 */
public class EditFormGenerator {
    /**
     * Generates a pane for editing an object
     * @param from The object to generate a pane for
     * @return The edit pane
     */
    public static Parent generatePane(Object from){
        VBox generated = new VBox(20);

        Class clazz = from.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields){
            if (!isEditable(field)) continue;

            //field --> getField or setField
            String getterName = "get" + capitalizeFieldName(field);
            String setterName = "set" + capitalizeFieldName(field);

            Editable editable = getEditable(field);
            if (editable.value() == null) throw new UnsupportedOperationException("Can't create a new type of 'null.' Check you've assigned an EditPaneGenerator to " + field.getName());

            if (!editable.getterName().isEmpty())
                getterName = editable.getterName();
            if (!editable.setterName().isEmpty())
                setterName = editable.setterName();

            try {
                Method getter = clazz.getMethod(getterName);
                Method setter = clazz.getMethod(setterName, field.getType());

                Node child = generateFor(field, getter, setter, from, editable);
                generated.getChildren().add(child);

            }catch (NoSuchMethodException e){
                continue;
            }
        }

        return generated;
    }

    /**
     * Capitalizes the name of field fooBar --> FooBar
     * @param field The field
     * @return The capitalized name of the field
     */
    private static String capitalizeFieldName(Field field){
        String text = field.getName();
        text = Character.toUpperCase(text.charAt(0)) + text.substring(1, text.length());
        return text;
    }

    /**
     * Indicates if a specified field is editable.
     * @param field the field to check for editableness.
     * @return Returns whether the field is editable
     */
    public static boolean isEditable(Field field){
        return getEditable(field) != null;
    }

    /**
     * Gets the friendly name of a field. For example, passing in a field named 'fooBar'
     * would result in 'Foo Bar'
     * @param field The field to get the friendly name of
     * @return The friendly name of the field
     */
    public static String getFriendlyName(Field field) {
        String raw = field.getName();

        String result = "";
        for (int i = 0; i < raw.length(); ++i){
            if (i == 0) {
                result += Character.toUpperCase(raw.charAt(i));
                continue;
            }

            if (Character.isUpperCase(raw.charAt(i)) && !Character.isUpperCase(raw.charAt(i - 1))) result += " ";

            result += raw.charAt(i);
        }

        return result;
    }

    /**
     * Gets the editable annotation for a specified field
     *
     * @param field The field to get the editable annotation for
     * @return The editable annotation. Null if there is not one
     */
    public static Editable getEditable(Field field){
        Annotation[] annotations = field.getDeclaredAnnotations();

        for (Annotation annotation : annotations){
            if (annotation instanceof Editable)
                return (Editable)annotation;
        }
        return null;
    }

    /**
     * Generates a node for a specific field automagically
     * @param field The field to generate an edit node for
     * @param getter The getter for the field
     * @param setter The setter for the field
     * @param from The object the field is from
     * @param editable The editable annotation of the
     * @return
     */
    private static Node generateFor(final Field field, final Method getter, final Method setter, final Object from, Editable editable){
        try {
            Constructor<?> constructor = editable.value().getConstructor();
            EditPaneGenerator generator = (EditPaneGenerator)constructor.newInstance();

            //If there was an argument set on the field, pass it on to the generator
            if (editable.argument() != null && !editable.argument().isEmpty())
                generator.setArgument(editable.argument());

            Class[] supportedClasses = generator.supportedTypes();
            boolean supported = false;

            for (Class clazz : supportedClasses)
            {
                if (field.getType().isAssignableFrom(clazz)) {
                    supported = true;
                    break;
                }
            }

            if (!supported)
                throw new UnsupportedOperationException("You've tried to generate a Form for the " + field.getName() + " property on the " + from.getClass().getName() + " object using a " + editable.value().getName() + " generator. Check and make sure that the converter is assigned and that it supports the field type.");

            return generator.generate(field, getter, setter, from);
        }catch (NoSuchMethodException e){
            throw new IllegalArgumentException("Unable to instantiate a new " + editable.value().getName() + ". You should check it has a default constructor.");
        }
        catch (Exception e){
            if (e instanceof UnsupportedOperationException)
                throw new UnsupportedOperationException(e.getMessage());
        }

        return new VBox();
    }
}
