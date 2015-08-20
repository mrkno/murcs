package sws.murcs.controller.editor;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import sws.murcs.controller.GenericPopup;
import sws.murcs.controller.SearchController;
import sws.murcs.controller.controls.popover.ArrowLocation;
import sws.murcs.controller.controls.popover.PopOver;
import sws.murcs.debug.errorreporting.ErrorReporter;
import sws.murcs.model.Person;
import sws.murcs.model.Story;
import sws.murcs.model.Task;
import sws.murcs.model.TaskState;

import java.io.IOException;
import java.util.Objects;

/**
 * The editor for a task contained within a story.
 */
public class TaskEditor {

    /**
     * The model of the tasks you are editing.
     */
    private Task task;

    /**
     * The parent node from the FXML.
     */
    private Parent parent;

    /**
     * The StoryEditor that this TaskEditror is contained within.
     */
    private StoryEditor storyEditor;

    /**
     * Whether this is a creation box or not.
     */
    private boolean creationBox;

    /**
     * Whether or not the box is maximised.
     */
    private boolean descriptionVisible;

    private PopOver assigneePopOver;

    /**
     * The anchor pane that contains the entire editor.
     */
    @FXML
    private AnchorPane editor;

    /**
     * The minimise editor, delete task, create and edit assignees buttons.
     */
    @FXML
    private Button toggleButton, createButton, editAssignedButton;

    /**
     * The state choice boxes.
     */
    @FXML
    private ChoiceBox stateChoiceBox;

    /**
     * The separator between tasks.
     */
    @FXML
    private Separator separator;

    /**
     * The description text area.
     */
    @FXML
    private TextArea descriptionTextArea;

    /**
     * The name and estimate text fields.
     */
    @FXML
    private TextField nameTextField, estimateTextField;

    /**
     * The label that has the assigned people's names on it.
     */
    @FXML
    private Label assigneesLabel;

    /**
     * The height of the window during creation.
     */
    private static final double CREATION_HEIGHT = 270.0;

    /**
     * The height of the window when it is collapsed.
     */
    private static final double COLLAPSED_HEIGHT = 80.0;

    /**
     * The height of the window when it is expanded.
     */
    private static final double EXPANDED_HEIGHT = 240.0;

    /**
     * Sets up the form with all its event handlers and things.
     */
    @FXML
    private void initialize() {
        ChangeListener changeListener = (observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                saveChanges();
            }
        };

        stateChoiceBox.getItems().clear();
        stateChoiceBox.getItems().addAll(TaskState.values());

        nameTextField.focusedProperty().addListener(changeListener);
        nameTextField.focusedProperty().addListener(changeListener);
        estimateTextField.focusedProperty().addListener(changeListener);
        estimateTextField.focusedProperty().addListener(changeListener);
        stateChoiceBox.focusedProperty().addListener(changeListener);
        stateChoiceBox.focusedProperty().addListener(changeListener);
        descriptionTextArea.focusedProperty().addListener(changeListener);
    }

    /**
     * Sets the task for the editor.
     * @param newTask The new task to be edited
     * @param isCreationBox Whether or not this is a creation box
     * @param view The storyEditor node from the FXML
     * @param containingStoryEditor The storyEditor node from the fxml
     */
    public final void configure(final Task newTask,
                                final boolean isCreationBox,
                                final Parent view,
                                final StoryEditor containingStoryEditor) {
        task = newTask;
        parent = view;
        storyEditor = containingStoryEditor;
        descriptionVisible = false;
        creationBox = isCreationBox;
        if (isCreationBox) {
            toggleButtonClicked(null);
            editor.setPrefHeight(CREATION_HEIGHT);
            createButton.setVisible(true);
            toggleButton.setVisible(false);
            separator.setVisible(false);
            stateChoiceBox.getSelectionModel().select(0);
        }
        else {
            nameTextField.setText(newTask.getName());
            estimateTextField.setText(String.valueOf(newTask.getEstimate()));
            stateChoiceBox.getSelectionModel().select(newTask.getState());
            descriptionTextArea.setText(newTask.getDescription());
        }
    }

    /**
     * Called whenever an element of the task is edited and saves the changes.
     */
    @FXML
    private void saveChanges() {
        storyEditor.clearErrors("tasks");

        // Check name
        String name = nameTextField.getText();
        if (name != null && !nameExists(name) && !name.isEmpty()) {
            if (!Objects.equals(name, task.getName())) {
                task.setName(name);
            }
        }
        else {
            nameTextField.setText(task.getName());
            storyEditor.addFormError("tasks", nameTextField,
                    "Task names must be unique and have at least one character!");
        }

        // Check estimate
        try {
            Float estimate = Float.parseFloat(estimateTextField.getText());
            if (estimate != task.getEstimate()) {
                task.setEstimate(estimate);
            }
        }
        catch (NumberFormatException e) {
            storyEditor.addFormError("tasks", estimateTextField, "Estimate must be a number!");
        }

        // Check state
        TaskState state = (TaskState) stateChoiceBox.getSelectionModel().getSelectedItem();
        if (state != task.getState()) {
            task.setState(state);
        }

        // Check description on maximized description field
        String description = descriptionTextArea.getText();
        if (!Objects.equals(description, task.getDescription())) {
            task.setDescription(description);
        }
    }

    /**
     * Checks whether a task with a specified name already exists.
     * @param name The name to check for
     * @return Whether a task already exists with that name
     */
    private boolean nameExists(final String name) {
        return storyEditor.getModel().getTasks().stream().anyMatch(t -> t.getName().equals(name) && !t.equals(task));
    }

    /**
     * Called by the create button in the task editor.
     * @param event The button press event.
     */
    @FXML
    private void createButtonClicked(final ActionEvent event) {
        storyEditor.clearErrors("tasks");
        boolean acceptable = true;

        String name = nameTextField.getText();
        if (!nameExists(name) && name != null && !name.isEmpty()) {
            task.setName(name);
        }
        else {
            acceptable = false;
            storyEditor.addFormError("tasks", nameTextField,
                    "Task names must be unique and have at least one character!");
        }

        try {
            float estimate = Float.parseFloat(estimateTextField.getText());
            task.setEstimate(estimate);
        }
        catch (NumberFormatException e) {
            acceptable = false;
            storyEditor.addFormError("tasks", estimateTextField, "Estimate must be a number!");
        }

        if (acceptable) {
            creationBox = false;
            createButton.setVisible(false);
            toggleButton.setVisible(true);
            storyEditor.addTask(task);
            toggleButtonClicked(null);
            separator.setVisible(true);
        }
    }

    /**
     * The function that is called toggle the state of the editor.
     * @param event The event that maximises/minimises the task editor
     */
    @FXML
    private void toggleButtonClicked(final ActionEvent event) {
        if (descriptionVisible) {
            descriptionTextArea.setVisible(false);
            editor.setPrefHeight(COLLAPSED_HEIGHT);
            toggleButton.setText("+");
            descriptionVisible = false;
        }
        else {
            descriptionTextArea.setVisible(true);
            editor.setPrefHeight(EXPANDED_HEIGHT);
            toggleButton.setText("-");
            descriptionVisible = true;
        }
    }

    /**
     * Deletes the task or cancels the creation.
     * @param event The button press event.
     */
    @FXML
    private void deleteButtonClicked(final ActionEvent event) {
        if (creationBox) {
            storyEditor.removeTask(task);
            storyEditor.removeTaskEditor(parent);
            return;
        }

        GenericPopup popup = new GenericPopup();
        popup.setTitleText("Really?");
        popup.setMessageText("Are you sure you wish to remove this task?");
        popup.addYesNoButtons(() -> {
            storyEditor.removeTask(task);
            storyEditor.removeTaskEditor(parent);
            popup.close();
        });
        popup.show();
    }

    /**
     * The opens a new popover window to add and remove assigned people.
     * @param event An event, probably clicking.
     */
    @FXML
    private void editAssignedButtonClicked(final ActionEvent event) {
        if (assigneePopOver == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TaskEditor.class.getResource("/sws/murcs/AssigneesPopOver.fxml"));

            try {
                Parent parent = loader.load();
                assigneePopOver = new PopOver(parent);
                AssigneeController controller = loader.getController();
                controller.setUp(this, null);
                assigneePopOver.hideOnEscapeProperty().setValue(true);
            }
            catch (IOException e) {
                ErrorReporter.get().reportError(e, "Could not create a assignee popover");
            }
        }

        assigneePopOver.arrowLocationProperty().setValue(ArrowLocation.RIGHT_CENTER);
        assigneePopOver.show(editAssignedButton);
    }

    public void addAssignee(Person assignee) {
        task.addAssignee(assignee);
    }

    public void removeAssignee(Person assignee) {
        task.removeAssignee(assignee);
    }

    public Task getTask() {
        return task;
    }

    public Story getStory() {
        return storyEditor.getModel();
    }
}
