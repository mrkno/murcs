package sws.murcs.controller.editor;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sws.murcs.controller.GenericPopup;
import sws.murcs.controller.NavigationManager;
import sws.murcs.controller.controls.SearchableComboBox;
import sws.murcs.controller.controls.md.MaterialDesignButton;
import sws.murcs.debug.errorreporting.ErrorReporter;
import sws.murcs.exceptions.CustomException;
import sws.murcs.exceptions.DuplicateObjectException;
import sws.murcs.model.AcceptanceCondition;
import sws.murcs.model.Backlog;
import sws.murcs.model.EstimateType;
import sws.murcs.model.Person;
import sws.murcs.model.Story;
import sws.murcs.model.Task;
import sws.murcs.model.helpers.DependenciesHelper;
import sws.murcs.model.helpers.DependencyTreeInfo;
import sws.murcs.model.helpers.UsageHelper;
import sws.murcs.model.persistence.PersistenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * An editor for the story model.
 */
public class StoryEditor extends GenericEditor<Story> {

    /**
     * The short name of the story.
     */
    @FXML
    private TextField shortNameTextField;

    /**
     * The description of the story.
     */
    @FXML
    private TextArea descriptionTextArea;

    /**
     * A choice box for the creator and the estimate choice box and a choice box for changing the story state.
     */
    @FXML
    private ChoiceBox creatorChoiceBox, estimateChoiceBox, storyStateChoiceBox;

    /**
     * Drop down with dependencies that can be added to this story.
     */
    @FXML
    private ComboBox<Story> dependenciesDropDown;

    /**
     * Container that dependencies are added to when they are added.
     * Also the container for the tasks.
     */
    @FXML
    private VBox dependenciesContainer, taskContainer;

    /**
     * A map of dependencies and their respective nodes.
     */
    private Map<Story, Node> dependenciesMap;

    /**
     * A decorator to make the ComboBox searchable.
     * Done a little weirdly to ensure SceneBuilder still works.
     */
    private SearchableComboBox<Story> searchableComboBoxDecorator;

    /**
     * A table for displaying and updating acceptance conditions.
     */
    @FXML
    private TableView<AcceptanceCondition> acceptanceCriteriaTable;

    /**
     * The columns on the AC table.
     */
    @FXML
    private TableColumn<AcceptanceCondition, String> conditionColumn;

    /**
     * Buttons for increasing and decreasing the priority of an AC. Also the button for adding a new AC.
     */
    @FXML
    private Button increasePriorityButton, decreasePriorityButton, addACButton;

    /**
     * The TextField containing the text for the new condition.
     */
    @FXML
    private TextField addConditionTextField;

    private FXMLLoader taskLoader = new FXMLLoader(getClass().getResource("/sws/murcs/TaskEditor.fxml"));

    private Thread thread;

    private boolean stop;

    @Override
    public final void loadObject() {
        String modelShortName = getModel().getShortName();
        setIsCreationWindow(modelShortName == null);
        String viewShortName = shortNameTextField.getText();
        setIsCreationWindow(modelShortName == null);
        if (isNotEqual(modelShortName, viewShortName)) {
            shortNameTextField.setText(modelShortName);
        }

        updateStoryState();

        //Add all the story states to the choice box
        storyStateChoiceBox.getItems().clear();
        storyStateChoiceBox.getItems().addAll(Story.StoryState.values());

        String modelDescription = getModel().getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNotEqual(modelDescription, viewDescription)) {
            descriptionTextArea.setText(modelDescription);
        }

        dependenciesDropDown.getItems().clear();
        dependenciesDropDown.getItems().addAll(PersistenceManager.getCurrent().getCurrentModel().getStories());
        dependenciesDropDown.getItems().remove(getModel());
        dependenciesDropDown.getItems().removeAll(getModel().getDependencies());

        dependenciesMap.clear();
        dependenciesContainer.getChildren().clear();
        getModel().getDependencies().forEach(dependency -> {
            Node dependencyNode = generateStoryNode(dependency);
            dependenciesContainer.getChildren().add(dependencyNode);
            dependenciesMap.put(dependency, dependencyNode);
        });

        taskContainer.getChildren().clear();
        javafx.concurrent.Task<Void> taskThread = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (Task task : getModel().getTasks()) {
                    if (stop) break;
                    injectTaskEditor(task, false);
                }
                return null;
            }
        };
        thread = new Thread(taskThread);
        thread.setDaemon(true);
        thread.start();

        // Enable or disable whether you can change the creator
        if (getIsCreationWindow()) {
            Person modelCreator = getModel().getCreator();
            creatorChoiceBox.getItems().clear();
            creatorChoiceBox.getItems().addAll(PersistenceManager.getCurrent().getCurrentModel().getPeople());
            if (modelCreator != null) {
                creatorChoiceBox.getSelectionModel().select(modelCreator);
            }
        }
        else {
            creatorChoiceBox.getItems().clear();
            creatorChoiceBox.getItems().add(getModel().getCreator());
            creatorChoiceBox.setDisable(true);
        }

        storyStateChoiceBox.getSelectionModel().select(getModel().getStoryState());
        if (!getIsCreationWindow()) {
            creatorChoiceBox.getSelectionModel().select(getModel().getCreator());
        }
        updateAcceptanceCriteria();
        if (!getIsCreationWindow()) {
            super.setupSaveChangesButton();
        }
        super.clearErrors();
    }

    /**
     * Updates the estimation on choicebox.
     */
    private void updateEstimation() {
        String currentEstimation = getModel().getEstimate();
        Backlog backlog = (Backlog) UsageHelper.findUsages(getModel())
                .stream()
                .filter(model -> model instanceof Backlog)
                .findFirst()
                .orElse(null);

        estimateChoiceBox.getItems().clear();
        estimateChoiceBox.getItems().add(EstimateType.NOT_ESTIMATED);
        if (backlog == null  || getModel().getAcceptanceCriteria().size() == 0) {
            estimateChoiceBox.getSelectionModel().select(0);
            estimateChoiceBox.setDisable(true);
        }
        else {
            estimateChoiceBox.setDisable(false);
            estimateChoiceBox.getItems().addAll(backlog.getEstimateType().getEstimates());
            estimateChoiceBox.getSelectionModel().select(currentEstimation);
        }
    }

    /**
     * Updates the list of acceptance criteria in the Table.
     */
    private void updateAcceptanceCriteria() {
        //store selection
        AcceptanceCondition selected = acceptanceCriteriaTable.getSelectionModel().getSelectedItem();

        //Load the acceptance conditions
        acceptanceCriteriaTable.getItems().clear();
        acceptanceCriteriaTable.getItems().addAll(getModel().getAcceptanceCriteria());

        //restore selection
        acceptanceCriteriaTable.getSelectionModel().select(selected);
        refreshPriorityButtons();

        //Update the story state because otherwise we might have a ready story with no ACs
        updateStoryState();

        //Update the estimation so we don't end up with an estimated story and no ACs
        updateEstimation();
    }

    /**
     * Refreshes the priority buttons so they have the correct enable state.
     */
    private void refreshPriorityButtons() {
        //Enable both buttons, we'll turn them off if we have to
        increasePriorityButton.setDisable(false);
        decreasePriorityButton.setDisable(false);

        AcceptanceCondition selected = acceptanceCriteriaTable.getSelectionModel().getSelectedItem();

        //If nothing is selected then both buttons should be disabled
        if (selected == null || getModel().getAcceptanceCriteria().size() == 0) {
            increasePriorityButton.setDisable(true);
            decreasePriorityButton.setDisable(true);
            return;
        }


        // and this is the first item priority wise, we can't increase its priority
        if (selected == getModel().getAcceptanceCriteria().get(0)) {
            increasePriorityButton.setDisable(true);
        }

        //If this is the last item, we can't go down
        if (selected == getModel().getAcceptanceCriteria().get(getModel().getAcceptanceCriteria().size() - 1)) {
            decreasePriorityButton.setDisable(true);
        }
    }

    @FXML
    @Override
    public final void initialize() {
        dependenciesContainer.getStylesheets().add(
                getClass().getResource("/sws/murcs/styles/materialDesign/dependencies.css").toExternalForm());

        setChangeListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                saveChanges();
            }
        });
        searchableComboBoxDecorator = new SearchableComboBox(dependenciesDropDown);
        dependenciesMap = new HashMap<>();

        shortNameTextField.focusedProperty().addListener(getChangeListener());
        descriptionTextArea.focusedProperty().addListener(getChangeListener());
        creatorChoiceBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
        estimateChoiceBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
        storyStateChoiceBox.getSelectionModel().selectedItemProperty().addListener(getChangeListener());
        dependenciesDropDown.valueProperty().addListener(getChangeListener());

        acceptanceCriteriaTable.getSelectionModel().selectedItemProperty().addListener(c -> refreshPriorityButtons());
        conditionColumn.setCellFactory(param -> new AcceptanceConditionCell());
        conditionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCondition()));
    }

    @Override
    public final void dispose() {
        if (thread != null && thread.isAlive()) {
            stop = true;
            try {
                thread.join();
            } catch (InterruptedException e) {
                //We don't care about this. Don't report this as all we really want to do is stop the thread.
                //We don't care if it has a hissy fit while killing it.
            }
        }
        shortNameTextField.focusedProperty().removeListener(getChangeListener());
        descriptionTextArea.focusedProperty().removeListener(getChangeListener());
        creatorChoiceBox.getSelectionModel().selectedItemProperty().removeListener(getChangeListener());
        estimateChoiceBox.getSelectionModel().selectedItemProperty().removeListener(getChangeListener());
        storyStateChoiceBox.getSelectionModel().selectedItemProperty().removeListener(getChangeListener());
        dependenciesDropDown.valueProperty().removeListener(getChangeListener());
        searchableComboBoxDecorator.dispose();
        searchableComboBoxDecorator = null;
        dependenciesMap = null;
        taskContainer.getChildren().clear();
        super.dispose();
    }

    @Override
    protected final void saveChangesAndErrors() {
        String modelShortName = getModel().getShortName();
        String viewShortName = shortNameTextField.getText();
        if (isNullOrNotEqual(modelShortName, viewShortName)) {
            try {
                getModel().setShortName(viewShortName);
            } catch (CustomException e) {
                addFormError(shortNameTextField, e.getMessage());
            }
        }

        String modelDescription = getModel().getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNullOrNotEqual(modelDescription, viewDescription)) {
            getModel().setDescription(viewDescription);
        }

        updateStoryState();

        if (getIsCreationWindow()) {
            Person viewCreator = (Person) creatorChoiceBox.getValue();
            if (viewCreator != null) {
                getModel().setCreator(viewCreator);
            } else {
                addFormError(creatorChoiceBox, "Creator cannot be empty");
            }
        }

        if (estimateChoiceBox.getValue() != null && getModel().getEstimate() != estimateChoiceBox.getValue()) {
            getModel().setEstimate((String) estimateChoiceBox.getValue());
            // Updates the story state as this gets changed if you set the estimate to Not Estimated
            storyStateChoiceBox.setValue(getModel().getStoryState());
        }

        Story selectedStory = dependenciesDropDown.getValue();
        if (selectedStory != null) {
            try {
                Platform.runLater(() -> {
                    dependenciesDropDown.getSelectionModel().clearSelection();
                });
                getModel().addDependency(selectedStory);
                Node dependencyNode = generateStoryNode(selectedStory);
                dependenciesContainer.getChildren().add(dependencyNode);
                dependenciesMap.put(selectedStory, dependencyNode);
                Platform.runLater(() -> {
                    searchableComboBoxDecorator.remove(selectedStory);
                });
            } catch (CustomException e) {
                addFormError(dependenciesDropDown, e.getMessage());
            }
        }
    }

    /**
     * Checks to see if the current story state is valid and
     * displays an error if it isn't.
     */
    private void updateStoryState() {
        Story.StoryState state = (Story.StoryState) storyStateChoiceBox.getSelectionModel().getSelectedItem();
        Story model = getModel();
        boolean hasErrors = false;

        if (state == Story.StoryState.Ready) {
            if (getModel().getAcceptanceCriteria().size() == 0) {
                addFormError(storyStateChoiceBox, "The story must have at least one AC to set the state to Ready");
                hasErrors = true;
            }
            if (UsageHelper.findUsages(model).stream().noneMatch(m -> m instanceof Backlog)) {
                addFormError(storyStateChoiceBox, "The story must be part of a backlog to set the state to Ready");
                hasErrors = true;
            }
            if (model.getEstimate().equals(EstimateType.NOT_ESTIMATED)) {
                addFormError(storyStateChoiceBox, "The story must be estimated to set the state to Ready");
                hasErrors = true;
            }
        }

        if (!hasErrors && state != null) {
            getModel().setStoryState((Story.StoryState) storyStateChoiceBox.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Generate a new node for a story dependency.
     * @param newDependency story to generate a node for.
     * @return a JavaFX node representing the dependency.
     */
    private Node generateStoryNode(final Story newDependency) {
        MaterialDesignButton removeButton = new MaterialDesignButton("X");
        removeButton.getStyleClass().add("mdr-button");
        removeButton.getStyleClass().add("mdrd-button");
        removeButton.setOnAction(event -> {
            GenericPopup popup = new GenericPopup();
            popup.setMessageText("Are you sure you want to remove the dependency "
                    + newDependency.getShortName() + "?");
            popup.setTitleText("Remove Dependency");
            popup.addYesNoButtons(func -> {
                searchableComboBoxDecorator.add(newDependency);
                Node dependencyNode = dependenciesMap.get(newDependency);
                dependenciesContainer.getChildren().remove(dependencyNode);
                dependenciesMap.remove(newDependency);
                getModel().removeDependency(newDependency);
                popup.close();
            });
            popup.show();
        });

        GridPane pane = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.SOMETIMES);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);

        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.NEVER);

        pane.getColumnConstraints().add(column1);
        pane.getColumnConstraints().add(column2);
        pane.getColumnConstraints().add(column3);

        if (getIsCreationWindow()) {
            Label nameText = new Label(newDependency.toString());
            pane.add(nameText, 0, 0);
        }
        else {
            Hyperlink nameLink = new Hyperlink(newDependency.toString());
            nameLink.setOnAction(a -> NavigationManager.navigateTo(newDependency));
            pane.add(nameLink, 0, 0);
        }
        DependencyTreeInfo treeInfo = DependenciesHelper.dependenciesTreeInformation(newDependency);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        ObservableList<Node> children = hBox.getChildren();
        children.add(new Label("["));
        Label storiesLabel = new Label(Integer.toString(treeInfo.getCount()));
        storiesLabel.setTooltip(
                new Tooltip("The number of stories that this story depends on in total (including itself)."));
        storiesLabel.getStyleClass().add("story-depends-on");
        children.add(storiesLabel);
        HBox.setHgrow(storiesLabel, Priority.ALWAYS);

        children.add(new Label(", "));
        Label immediateLabel = new Label(Integer.toString(treeInfo.getImmediateDepth()));
        immediateLabel.setTooltip(new Tooltip("The number of stories this story immediately depends on."));
        immediateLabel.getStyleClass().add("story-depends-direct");
        children.add(immediateLabel);
        HBox.setHgrow(immediateLabel, Priority.ALWAYS);

        children.add(new Label(", "));
        Label deepLabel = new Label(Integer.toString(treeInfo.getMaxDepth()));
        deepLabel.setTooltip(new Tooltip("The maximum number of stories this story transitively depends on."));
        deepLabel.getStyleClass().add("story-depends-deep");
        children.add(deepLabel);
        HBox.setHgrow(deepLabel, Priority.ALWAYS);
        children.add(new Label("] "));

        hBox.setOnMouseEntered(event -> transitionText(hBox, storiesLabel, Integer.toString(treeInfo.getCount())
                        + " stories", immediateLabel, Integer.toString(treeInfo.getImmediateDepth()) + " direct",
                deepLabel, Integer.toString(treeInfo.getMaxDepth()) + " deep"));

        hBox.setOnMouseExited(event -> transitionText(hBox, storiesLabel, Integer.toString(treeInfo.getCount()),
                immediateLabel, Integer.toString(treeInfo.getImmediateDepth()),
                deepLabel, Integer.toString(treeInfo.getMaxDepth())));

        pane.add(hBox, 1, 0);
        pane.add(removeButton, 2, 0);
        GridPane.setMargin(removeButton, new Insets(1, 1, 1, 0));

        return pane;
    }

    /**
     * Performs a transition to new text on the dependencies detail text.
     * @param itemsContainer container of the details labels.
     * @param storiesLabel the label to set to storiesText.
     * @param storiesText the new text for storiesLabel.
     * @param immediateLabel the label to set to immediateText.
     * @param immediateText the new text for immediateLabel.
     * @param deepLabel the label to set to deepText.
     * @param deepText the new text for deepLabel.
     */
    private void transitionText(final Node itemsContainer, final Label storiesLabel, final String storiesText,
                                final Label immediateLabel, final String immediateText,
                                final Label deepLabel, final String deepText) {
        final Duration transitionTime = Duration.seconds(0.15);
        FadeTransition fadeOut = new FadeTransition(transitionTime, itemsContainer);
        fadeOut.setFromValue(itemsContainer.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(evt -> {
            storiesLabel.setText(storiesText);
            immediateLabel.setText(immediateText);
            deepLabel.setText(deepText);
            fadeOut.setFromValue(itemsContainer.getOpacity());
            fadeOut.setToValue(1);
            fadeOut.setOnFinished(null);
            fadeOut.play();
        });
        fadeOut.play();
    }

    /**
     * Called when the "Add Condition" button is clicked. Adds the Acceptance Condition
     * created by the user
     * @param event The event information
     */
    @FXML
    protected final void addConditionButtonClicked(final ActionEvent event) {
        String conditionText = addConditionTextField.getText();

        //Create a new condition
        AcceptanceCondition newCondition = new AcceptanceCondition();
        try {
            newCondition.setCondition(conditionText);
        } catch (CustomException e) {
            addFormError(addACButton, e.getMessage());
            return;
        }

        //Add the new condition to the model
        getModel().addAcceptanceCondition(newCondition);

        //Clear the acceptance condition box
        addConditionTextField.setText("");

        //Make sure that the table gets updated
        updateAcceptanceCriteria();

        //Select the item we just created
        acceptanceCriteriaTable.getSelectionModel().select(newCondition);
    }

    /**
     * Decreases the priority of a selected row in the table.
     * @param event the event information
     */
    @FXML
    protected final void increasePriorityClicked(final ActionEvent event) {
        //Get the selected item and move it up one place
        AcceptanceCondition condition = acceptanceCriteriaTable.getSelectionModel().getSelectedItem();
        moveCondition(condition, -1);
    }

    /**
     * Increases the priority of a selected row in the table.
     * @param event the event information
     */
    @FXML
    protected final void decreasePriorityClicked(final ActionEvent event) {
        //Get the selected item and move it down one place
        AcceptanceCondition condition = acceptanceCriteriaTable.getSelectionModel().getSelectedItem();
        moveCondition(condition, 1);
    }

    /**
     * Moves a condition down the list of Acceptance Criteria by a specified number of places (the number of
     * places wraps).
     * @param condition The condition to move
     * @param places The number of places to move it.
     */
    public final void moveCondition(final AcceptanceCondition condition, final int places) {
        //Get the current index of the AC
        int index = getModel().getAcceptanceCriteria().indexOf(condition);

        //If the item is not in the list, return
        if (index == -1) {
            return;
        }

        index += places;

        //Wrap the index.
        while (index < 0) {
            index += getModel().getAcceptanceCriteria().size();
        }
        while (index >= getModel().getAcceptanceCriteria().size()) {
            index -= getModel().getAcceptanceCriteria().size();
        }

        //Reposition the item to our calculated index in the model
        getModel().repositionCondition(condition, index);

        //Update the ACs in the table
        updateAcceptanceCriteria();
    }

    /**
     * Injects a task editor tied to the given task.
     * @param task The task to display
     * @param creationBox Whether or not this is a creation box
     */
    private void injectTaskEditor(final Task task, final boolean creationBox) {
        try {
            taskLoader.setRoot(null);
            TaskEditor controller = new TaskEditor();
            taskLoader.setController(controller);
            if (taskLoader == null) return;
            Parent view = taskLoader.load();
            controller.configure(task, creationBox, view, this);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                taskContainer.getChildren().add(view);
                countDownLatch.countDown();
            });
            countDownLatch.await();
        } catch (LoadException e) {
            // lol
        } catch (Exception e) {
            ErrorReporter.get().reportError(e, "Unable to create new task");
        }
    }

    /**
     * Is called when you click the 'Create Task' button and inserts a new task
     * fxml into the task container.
     * @param event The event that caused the function to be called
     */
    @FXML
    private void createTaskClick(final ActionEvent event) {
        Task task = new Task();
        injectTaskEditor(task, true);
    }

    /**
     * Adds a task to this story.
     * @param task The task to add
     */
    protected final void addTask(final Task task) {
        try {
            getModel().addTask(task);
        }
        catch (DuplicateObjectException e) {
            addFormError(taskContainer, e.getMessage());
        }
    }

    /**
     * Removes a task from this story.
     * @param task The task to remove
     */
    protected final void removeTask(final Task task) {
        if (getModel().getTasks().contains(task)) {
            getModel().removeTask(task);
        }
    }

    /**
     * Removes the editor of a task.
     * @param view The parent node of the task editor
     */
    protected final void removeTaskEditor(final Parent view) {
        taskContainer.getChildren().remove(view);
    }

    /**
     * A cell representing an acceptance condition in the table of conditions.
     */
    private class AcceptanceConditionCell extends TableCell<AcceptanceCondition, String> {
        /**
         * The editable acceptance condition description text field.
         */
        TextArea textField = new TextArea();
        /**
         * The acceptance condition description text field.
         */
        Label textLabel = new Label();

        @Override
        public void startEdit() {
            super.startEdit();
            if (!isEmpty()) {
                clearErrors();
                setGraphic(createCell(true));
                textField.requestFocus();
            }
        }

        @Override
        public void commitEdit(final String newValue) {
            super.commitEdit(newValue);
            if (!isEmpty()) {
                try {
                    AcceptanceCondition acceptanceCondition = (AcceptanceCondition) getTableRow().getItem();
                    acceptanceCondition.setCondition(textField.getText());
                    textLabel.setText(acceptanceCondition.getCondition());
                    setGraphic(createCell(false));
                    clearErrors();
                } catch (CustomException e) {
                    clearErrors();
                    addFormError(textField, e.getMessage());
                }

            }
        }

        @Override
        public void cancelEdit() {
            if (!isEmpty()) {
                super.cancelEdit();
                AcceptanceCondition acceptanceCondition = (AcceptanceCondition) getTableRow().getItem();
                textLabel.setText(acceptanceCondition.getCondition());
                setGraphic(createCell(false));
            }
        }

        @Override
        protected void updateItem(final String newCondition, final boolean empty) {
            super.updateItem(newCondition, empty);
            textField.setText(newCondition);
            textLabel.setText(newCondition);

            if (newCondition == null || empty) {
                setText(null);
                setGraphic(null);
                return;
            } else if (isEditing()) {
                setGraphic(createCell(true));
            } else {
                setGraphic(createCell(false));
            }

            textLabel.setOnMousePressed(event -> startEdit());
            textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    commitEdit(textField.getText());
                }
            });
            textField.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                }
                if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        /**
         * Creates a node to be used as the cell graphic.
         * @param isEdit True if the cell should be editable
         * @return The create cell node
         */
        @SuppressWarnings("checkstyle:magicnumber")
        private Node createCell(final Boolean isEdit) {
            Node node;
            if (isEdit) {
                textField.setWrapText(true);
                Platform.runLater(() -> {
                    ScrollPane scrollPane = (ScrollPane) textField.lookup(".scroll-pane");
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                });
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    Text text = new Text();
                    text.setFont(textField.getFont());
                    text.setWrappingWidth(textField.getWidth());
                    text.setText(newValue + ' ');
                    textField.setPrefRowCount((int) text.getLayoutBounds().getHeight() / 15);
                });
                textField.setPrefRowCount(1);
                node = textField;
            }
            else {
                textLabel.setWrapText(true);
//                Text text = new Text();
//                text.setFont(textLabel.getFont());
//                text.setWrappingWidth(conditionColumn.getWidth() - 30);
//                text.setText(textLabel.getText());
//                textLabel.setPrefHeight(text.getLayoutBounds().getHeight());
                node = textLabel;
            }
            AcceptanceCondition acceptanceCondition = (AcceptanceCondition) getTableRow().getItem();
            Button button = new Button("X");
            button.getStyleClass().add("mdr-button");
            button.getStyleClass().add("mdrd-button");
            button.setOnAction(event -> {
                GenericPopup popup = new GenericPopup();
                popup.setTitleText("Are you sure?");
                popup.setMessageText("Are you sure you wish to remove this acceptance condition?");
                popup.addYesNoButtons(p -> {
                    getModel().removeAcceptanceCondition(acceptanceCondition);
                    updateAcceptanceCriteria();
                    popup.close();
                });
                popup.show();
            });
//            AnchorPane conditionCell = new AnchorPane();
//            HBox hBox = new HBox();
//            hBox.getChildren().add(node);
//            AnchorPane.setLeftAnchor(hBox, 0.0);
//            AnchorPane.setRightAnchor(hBox, 30.0);
//            AnchorPane.setRightAnchor(button, 0.0);
//            conditionCell.getChildren().addAll(node, button);
            GridPane conditionCell = new GridPane();
            conditionCell.add(node, 0, 0);
            conditionCell.add(button, 1, 0);
            conditionCell.getColumnConstraints().add(0, new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
            conditionCell.getColumnConstraints().add(1, new ColumnConstraints(30, 30, 30, Priority.NEVER, HPos.CENTER, true));
            conditionCell.getRowConstraints().add(0, new RowConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.TOP, true));
            return conditionCell;
        }
    }


}
