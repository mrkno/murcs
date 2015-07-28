package sws.murcs.controller.editor;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sws.murcs.exceptions.CustomException;
import sws.murcs.model.*;
import sws.murcs.model.persistence.PersistenceManager;

/**
 * The controller for editing sprints.
 */
public class SprintEditor extends GenericEditor<Sprint>{
    /**
     * Text fields for the short and long name of the sprint
     */
    @FXML
    private TextField shortNameTextField, longNameTextField;

    /**
     * A text area for the description of a sprint
     */
    @FXML
    private TextArea descriptionTextArea;

    /**
     * A combo box for picking the team associated with the sprint
     */
    @FXML
    private ComboBox<Team> teamComboBox;

    /**
     * A combobox for picking the backlog associated with a sprint
     */
    @FXML
    private ComboBox<Backlog> backlogComboBox;

    /**
     * A combobox for picking the release associated with a sprint
     */
    @FXML
    private ComboBox<Release> releaseComboBox;

    @Override
    public void loadObject() {
        Organisation organisation = PersistenceManager.getCurrent().getCurrentModel();
        Sprint sprint = getModel();

        //Fill the short name field
        shortNameTextField.setText(sprint.getShortName());

        //Fill the long name field
        longNameTextField.setText(sprint.getLongName());

        //Fill the description field
        descriptionTextArea.setText(sprint.getDescription());

        //Update the backlog combo box
        backlogComboBox.getItems().clear();
        backlogComboBox.getItems().addAll(organisation.getBacklogs());
        backlogComboBox.setValue(sprint.getBacklog());

        //Update the releases combo box
        releaseComboBox.getItems().clear();
        releaseComboBox.getItems().addAll(organisation.getReleases());
        releaseComboBox.setValue(sprint.getAssociatedRelease());

        //Update the team combo box
        teamComboBox.getItems().clear();
        teamComboBox.getItems().addAll(organisation.getTeams());
        teamComboBox.setValue(sprint.getTeam());
    }

    @Override
    protected void saveChangesAndErrors() {
        clearErrors();

        Sprint sprint = getModel();

        //Try and save the short name
        if (isNullOrNotEqual(sprint.getShortName(), shortNameTextField.getText())) {
            try {
                sprint.setShortName(shortNameTextField.getText());
            } catch (CustomException e) {
                addFormError(shortNameTextField, e.getMessage());
            }
        }

        //Save the long name
        if (isNullOrNotEqual(sprint.getLongName(), longNameTextField.getText())) {
            sprint.setLongName(longNameTextField.getText());
        }

        //Save the description
        if (isNullOrNotEqual(sprint.getDescription(), descriptionTextArea.getText())) {
            sprint.setDescription(descriptionTextArea.getText());
        }

        //Save the team
        if (isNullOrNotEqual(sprint.getTeam(), teamComboBox.getValue())) {
            if (teamComboBox.getValue() != null) {
                sprint.setTeam(teamComboBox.getValue());
            } else {
                addFormError(teamComboBox, "You must select a team to associate with the sprint");
            }
        }

        //Save the backlog
        if (isNullOrNotEqual(sprint.getBacklog(), backlogComboBox.getValue())) {
            if (backlogComboBox.getValue() != null) {
                sprint.setBacklog(backlogComboBox.getValue());
            }
            else {
                addFormError(backlogComboBox, "You must select a backlog for this sprint");
            }
        }

        //Save the release
        if (isNullOrNotEqual(sprint.getAssociatedRelease(), sprint.getAssociatedRelease())) {
            if (releaseComboBox.getValue() != null) {
                sprint.setAssociatedRelease(releaseComboBox.getValue());
            } else {
                addFormError(releaseComboBox, "You must select a release for this sprint");
            }
        }
    }

    @Override @FXML
    protected void initialize() {
        setChangeListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                saveChanges();
            }
        });

        shortNameTextField.focusedProperty().addListener(getChangeListener());
        longNameTextField.focusedProperty().addListener(getChangeListener());
        descriptionTextArea.focusedProperty().addListener(getChangeListener());

        backlogComboBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
        teamComboBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
        releaseComboBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
    }
}
