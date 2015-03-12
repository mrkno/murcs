package sws.project.magic.fxml;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 *
 */
public abstract class BasicEditController<T> implements EditController<T> {
    protected ArrayList<Predicate<T>> validators = new ArrayList<>();
    protected ArrayList<ChangeListener<T>> changeListeners = new ArrayList<>();

    @FXML
    protected Node invalidNode;

    @Override
    public void addValidator(Predicate<T> predicate) {
        validators.add(predicate);
    }

    @Override
    public void addChangeListener(ChangeListener<T> listener) {
        changeListeners.add(listener);
    }

    /**
     * Notify all the change listeners that we've changed, if the new value is valid
     * @param observable The observable that has changed
     * @param oldValue The old value
     * @param newValue The new value
     */
    protected void notifyChanged(ObservableValue<? extends T> observable, T oldValue, T newValue){
        //If nothing has changed, there's no point in us doing anything is there?
        if (oldValue == newValue) return;

        //If our new value is not valid
        if (!isValid(newValue)) {
            //Show an invalid message and return
            showInvalid();
            return;
        }

        //If we have satisfied all the predicates, we must be valid!
        showValid();

        //Loop through the change listeners and notify them all
        for (ChangeListener<T> listener : changeListeners){
            listener.changed(observable, oldValue, newValue);
        }
    }

    /**
     * Checks to see if a specified value is valid by our list of predicates
     * @param value The value to verify
     * @return Whether the value is valid
     */
    protected boolean isValid(T value){
        //Check if we're valid. If any validator is false, we're invalid
        for (Predicate<T> predicate : validators){
            if (!predicate.test(value)) {
                showInvalid();
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the value in the GUI is changed to something Invalid
     */
    protected void showValid(){
        if (invalidNode == null) return;
        invalidNode.setVisible(false);
    }

    /**
     * Called when the value in the GUI is changed to something Valid
     */
    protected void showInvalid(){
        if (invalidNode == null) return;
        invalidNode.setVisible(true);
    }
}
