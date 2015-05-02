package sws.murcs.controller.editor;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import sws.murcs.exceptions.InvalidParameterException;
import sws.murcs.magic.tracking.UndoRedoManager;
import sws.murcs.model.Project;
import sws.murcs.model.Release;
import sws.murcs.model.persistence.PersistenceManager;

import java.time.LocalDate;
import java.util.Optional;

/**
 * An editor for editing/creating a release.
 */
public class ReleaseEditor extends GenericEditor<Release> {

    /**
     * The shortName of a Release.
     */
    @FXML
    private TextField shortNameTextField;
    /**
     * The Description of a release.
     */
    @FXML
    private TextArea descriptionTextArea;
    /**
     * Release date picker.
     */
    @FXML
    private DatePicker releaseDatePicker;
    /**
     * The label to show the error message.
     */
    @FXML
    private Label labelErrorMessage;
    /**
     * The list of projects to choose from.
     */
    @FXML
    private ChoiceBox<Project> projectChoiceBox;
    /**
     * The change listener for an associated project.
     */
    private ChangeListener<Project> projectChangeListener;
    /**
     * The releases associated project.
     */
    private Project associatedProject;
    /**
     * The release to edit.
     */
    private Release model;

    @FXML
    @Override
    public final void initialize() {
        descriptionTextArea.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                saveChanges();
            }
        });
        shortNameTextField.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                saveChanges();
            }
        });
        releaseDatePicker.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                saveChanges();
            }
        });
        projectChoiceBox.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                saveChanges();
            }
        });

        projectChangeListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                saveChanges();
            }
        };

        projectChoiceBox.getSelectionModel()
                .selectedItemProperty().addListener(projectChangeListener);

        setErrorCallback(message -> {
            if (message.getClass() == String.class) {
                labelErrorMessage.setText(message);
            }
        });
        this.model = super.getModel();
    }


    @Override
    public final void loadObject() {
        Optional<Project> projectCheck
                = PersistenceManager.Current
                .getCurrentModel().getProjects().stream().
                filter(project ->
                        project.getReleases().contains(model))
                .findFirst();
        if (projectCheck.isPresent()) {
            associatedProject = projectCheck.get();
        }

        // While the project choice box is being populated,
        // don't fire listeners attached to it.
        // this is achieved by removing the listener temporarily
        projectChoiceBox.getSelectionModel()
                .selectedItemProperty().removeListener(projectChangeListener);
        projectChoiceBox.getItems().clear();
        projectChoiceBox.getItems()
                .addAll(PersistenceManager.Current
                        .getCurrentModel().getProjects());
        if (associatedProject != null) {
            projectChoiceBox.getSelectionModel().select(associatedProject);
        }
        projectChoiceBox.getSelectionModel()
                .selectedItemProperty().addListener(projectChangeListener);

        String modelShortName = model.getShortName();
        String viewShortName = shortNameTextField.getText();
        if (isNotEqual(modelShortName, viewShortName)) {
            shortNameTextField.setText(modelShortName);
        }

        String modelDescription = model.getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNotEqual(modelDescription, viewDescription)) {
            descriptionTextArea.setText(modelDescription);
        }

        LocalDate modelReleaseDate = model.getReleaseDate();
        LocalDate viewReleaseDate = releaseDatePicker.getValue();
        if (isNotEqual(modelReleaseDate, viewReleaseDate)) {
            releaseDatePicker.setValue(modelReleaseDate);
        }
    }

    @Override
    protected final void saveChangesWithException() throws Exception {
        String modelShortName = model.getShortName();
        String viewShortName = shortNameTextField.getText();
        if (isNotEqualOrIsEmpty(modelShortName, viewShortName)) {
            model.setShortName(viewShortName);
        }

        String modelDescription = model.getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNotEqualOrIsEmpty(modelDescription, viewDescription)) {
            model.setDescription(viewDescription);
        }

        LocalDate modelReleaseDate = model.getReleaseDate();
        LocalDate viewReleaseDate = releaseDatePicker.getValue();
        if (isNotEqualOrIsEmpty(modelReleaseDate, viewReleaseDate)) {
            model.setReleaseDate(viewReleaseDate);
        }

        updateAssociatedProject();
    }

    @Override
    public final void dispose() {
        projectChoiceBox.getSelectionModel()
                .selectedItemProperty().removeListener(projectChangeListener);
        projectChangeListener = null;
        associatedProject = null;
        model = null;
        UndoRedoManager.removeChangeListener(this);
        super.setModel(null);
        this.setErrorCallback(null);
    }

    /**
     * Updates the associated project.
     * @throws Exception when updating fails.
     */
    private void updateAssociatedProject() throws Exception {
        //fixme This code feels out of place,
        // fixme seems like some of it should be in the model
        //We've just changed what project we are associating this with
        // so remove the release from the last one
        if (associatedProject != null) {
            associatedProject.removeRelease(model);
        }

        //Update the associated project
        associatedProject = projectChoiceBox.getValue();

        if (associatedProject != null) {
            associatedProject.addRelease(model);
        } else {
            throw new InvalidParameterException(
                    "There needs to be an associated project");
        }

        UndoRedoManager.commit("edit release");
    }
}
