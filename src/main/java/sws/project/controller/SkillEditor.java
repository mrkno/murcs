package sws.project.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sws.project.model.RelationalModel;
import sws.project.model.Skill;
import sws.project.model.persistence.PersistenceManager;
import sws.project.view.App;

import java.util.concurrent.Callable;

/**
 * A controller to edit skills
 */
public class SkillEditor extends GenericEditor<Skill> {

    @FXML
    TextField shortNameTextField, longNameTextField;
    @FXML
    TextArea descriptionTextArea;
    @FXML
    Label labelErrorMessage;
    private Skill skill;
    private Callable<Void> onSaved;

    /**
     * Creates a new form for editing a skill
     *
     * @param skill The skill
     * @return The form
     */
    public static Parent createFor(Skill skill) {
        return createFor(skill, null);
    }

    /**
     * Creates a new form for editing a skill which will call the saved callback
     * every time a change is saved
     *
     * @param skill   The skill
     * @param onSaved The save callback
     * @return The form
     */
    private static Parent createFor(Skill skill, Callable<Void> onSaved, boolean autoSaving) {
        try {
            FXMLLoader loader = new FXMLLoader(SkillEditor.class.getResource("/sws/project/SkillEditor.fxml"));
            AnchorPane anchorPane = loader.load();

            assert anchorPane != null;

            SkillEditor controller = loader.getController();
            controller.skill = skill;
            controller.onSaved = onSaved;
            controller.load();

            return anchorPane;
        } catch (Exception e) {
            System.err.println("Unable to create a skill editor!(this is seriously bad)");
            e.printStackTrace();
        }

        return null;
    }

    public static Parent createFor(Skill skill, Callable<Void> onSaved) {
        return createFor(skill, onSaved, true);
    }

    /**
     * Displays a new window for creating a new form
     *
     * @param okay   The okay callback
     * @param cancel The cancelled callback
     */
    public static void displayWindow(Callable<Void> okay, Callable<Void> cancel) {
        try {
            Parent content = createFor(new Skill(), null, false);

            Parent root = CreateWindowController.newCreateNode(content, okay, cancel);
            Scene scene = new Scene(root);

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Create Skill");

            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(App.stage);

            newStage.show();
        } catch (Exception e) {

        }
    }

    /**
     * Saves the skill being edited
     */
    public void update() {
        try {
            labelErrorMessage.setText("");
            skill.setShortName(shortNameTextField.getText());
            skill.setLongName(longNameTextField.getText());
            skill.setDescription(descriptionTextArea.getText());

            RelationalModel model = PersistenceManager.Current.getCurrentModel();

            //If we haven't added the skill yet, throw them in the list of unassigned people
            if (!model.getSkills().contains(skill))
                model.getSkills().add(skill);

            //If we have a saved callBack, call it
            if (onSaved != null)
                onSaved.call();

        } catch (Exception e) {
            labelErrorMessage.setText(e.getMessage());
            return;
        }
    }

    /**
     * Loads the skill into the form
     */
    @Override
    public void load() {
        shortNameTextField.setText(skill.getShortName());
        longNameTextField.setText(skill.getLongName());
        descriptionTextArea.setText(skill.getDescription());
    }

    /**
     * Initializes the editor for use, sets up listeners etc.
     */
    @FXML
    public void initialize() {
        shortNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue)
                update();
        });

        longNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue)
                update();
        });

        descriptionTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue)
                update();
        });
    }
}
