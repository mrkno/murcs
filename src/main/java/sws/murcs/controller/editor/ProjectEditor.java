package sws.murcs.controller.editor;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import sws.murcs.controller.GenericPopup;
import sws.murcs.controller.NavigationManager;
import sws.murcs.exceptions.CustomException;
import sws.murcs.model.Organisation;
import sws.murcs.model.Project;
import sws.murcs.model.Team;
import sws.murcs.model.WorkAllocation;
import sws.murcs.model.persistence.PersistenceManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the model creator popup window.
 * Since there should only be one instance of this PopUp
 */
public class ProjectEditor extends GenericEditor<Project> {
    /**
     * The labels inside the form.
     */
    @FXML
    private Label shortNameLabel, longNameLabel, descriptionLabel;

    /**
     * The shortName, longName for a project.
     */
    @FXML
    private TextField shortNameTextField, longNameTextField;

    /**
     * A description for the current project.
     */
    @FXML
    private TextArea descriptionTextArea;

    /**
     * The Work Allocation table, team view.
     */
    @FXML
    private TableView<WorkAllocation> teamsViewer;

    /**
     * The Work Allocation table, team column.
     */
    @FXML
    private TableColumn<WorkAllocation, Team> tableColumnTeams;

    /**
     * The Work Allocation table, start and end date columns.
     */
    @FXML
    private TableColumn<WorkAllocation, LocalDate> tableColumnStartDates, tableColumnEndDates;

    /**
     * The date picker for the start and end dates of a work allocation.
     */
    @FXML
    private DatePicker datePickerStartDate, datePickerEndDate;

    /**
     * The team picker for a work allocation.
     */
    @FXML
    private ChoiceBox<Team> choiceBoxAddTeam;

    /**
     * An observable list of work allocations.
     */
    private ObservableList<WorkAllocation> observableAllocations;

    @FXML
    @Override
    public final void initialize() {
        setChangeListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                saveChanges();
            }
        });

        shortNameTextField.focusedProperty().addListener(getChangeListener());
        longNameTextField.focusedProperty().addListener(getChangeListener());
        descriptionTextArea.focusedProperty().addListener(getChangeListener());

        observableAllocations = FXCollections.observableArrayList();
        tableColumnTeams.setCellValueFactory(new PropertyValueFactory<>("team"));
        tableColumnTeams.setCellFactory(a -> new HyperlinkTeamCell());
        tableColumnStartDates.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        tableColumnStartDates.setCellFactory(a -> new NullableLocalDateCell());
        tableColumnEndDates.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        tableColumnEndDates.setCellFactory(a -> new NullableLocalDateCell());
        teamsViewer.setItems(observableAllocations);
        registerFormLabel(shortNameTextField, shortNameLabel);
        registerFormLabel(longNameTextField, longNameLabel);
        registerFormLabel(descriptionTextArea, descriptionLabel);

        descriptionTextArea.setWrapText(true);
        Platform.runLater(() -> {
            ScrollPane scrollPane = (ScrollPane) descriptionTextArea.lookup(".scroll-pane");
            if (scrollPane != null) {
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });
        descriptionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Text text = new Text();
            text.setFont(descriptionTextArea.getFont());
            text.setWrappingWidth(descriptionTextArea.getWidth());
            text.setText(newValue + ' ');
            descriptionTextArea.setPrefRowCount((int) text.getLayoutBounds().getHeight() / 18);
        });
        descriptionTextArea.setPrefRowCount(1);
    }

    @Override
    public final void loadObject() {
        // todo decouple from model
        Organisation organisation = PersistenceManager.getCurrent().getCurrentModel();

        String modelShortName = getModel().getShortName();
        String viewShortName = shortNameTextField.getText();
        if (isNotEqual(modelShortName, viewShortName)) {
            shortNameTextField.setText(modelShortName);
        }

        String modelLongName = getModel().getLongName();
        String viewLongName = longNameTextField.getText();
        if (isNotEqual(modelLongName, viewLongName)) {
            longNameTextField.setText(modelLongName);
        }

        String modelDescription = getModel().getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNotEqual(modelDescription, viewDescription)) {
            descriptionTextArea.setText(modelDescription);
        }

        choiceBoxAddTeam.getItems().setAll(organisation.getTeams());
        observableAllocations.setAll(organisation.getProjectsAllocations(getModel()));

        setIsCreationWindow(modelShortName == null);
        if (!getIsCreationWindow()) {
            super.setupSaveChangesButton();
        }
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

        String modelLongName = getModel().getLongName();
        String viewLongName = longNameTextField.getText();
        if (isNullOrNotEqual(modelLongName, viewLongName)) {
            getModel().setLongName(viewLongName);
        }

        String modelDescription = getModel().getDescription();
        String viewDescription = descriptionTextArea.getText();
        if (isNullOrNotEqual(modelDescription, viewDescription)) {
            getModel().setDescription(viewDescription);
        }
    }

    @Override
    public final void dispose() {
        shortNameTextField.focusedProperty().removeListener(getChangeListener());
        longNameTextField.focusedProperty().removeListener(getChangeListener());
        shortNameTextField.focusedProperty().removeListener(getChangeListener());
        observableAllocations = null;
        super.dispose();
    }

    /**
     * Called by the "Add Team" button.
     * Adds a work allocation to the list for the selected project
     */
    @FXML
    private void buttonScheduleTeamClick() {
        //Save all the changes first so that if there are any problems in the form they will show up as errors.
        saveChanges();

        Team team = choiceBoxAddTeam.getValue();
        LocalDate startDate = datePickerStartDate.getValue();
        LocalDate endDate = datePickerEndDate.getValue();
        boolean hasErrors = false;

        // Must meet minimum requirements for an allocation
        if (team == null) {
            addFormError(choiceBoxAddTeam, "Team may not be null");
            hasErrors = true;
        }
        if (startDate == null) {
            addFormError(datePickerStartDate, "Start date must be specified");
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        try {
            // Attempt to save the allocation
            Organisation organisation = PersistenceManager.getCurrent().getCurrentModel();
            WorkAllocation allocation = new WorkAllocation(getModel(), team, startDate, endDate);
            organisation.addAllocation(allocation);
            observableAllocations.setAll(organisation.getProjectsAllocations(getModel()));

            // Clear user inputs for work period
            choiceBoxAddTeam.getSelectionModel().clearSelection();
            datePickerStartDate.setValue(null);
            datePickerEndDate.setValue(null);
        }
        catch (CustomException e) {
            addFormError(e.getMessage());
            addFormError(datePickerStartDate);
            addFormError(datePickerEndDate);
        }
    }

    /**
     * Called by the "Unschedule Work" button.
     * Removes a work period from both the project and team
     */
    @FXML
    private void buttonUnscheduleTeamClick() {
        if (teamsViewer.getSelectionModel().getSelectedIndex() == -1) {
            return;
        }

        int rowNumber = teamsViewer.getSelectionModel().getSelectedIndex();
        WorkAllocation allocation = observableAllocations.get(rowNumber);
        GenericPopup alert = new GenericPopup();
        alert.setTitleText("Unshedule A Team");
        alert.setMessageText("Are you sure you wish to unshedule \""
                + allocation.getTeam()
                + "\" from \""
                + allocation.getProject()
                + "\"?");
        alert.addYesNoButtons(a -> {
            PersistenceManager.getCurrent().getCurrentModel().removeAllocation(allocation);
            observableAllocations.remove(rowNumber);
            alert.close();
        });
        alert.show();
    }

    /**
     * A TableView cell that contains a link to the team it represents.
     */
    private class HyperlinkTeamCell extends TableCell<WorkAllocation, Team> {
        @Override
        protected void updateItem(final Team team, final boolean empty) {
            super.updateItem(team, empty);
            if (team == null) {
                setText("");
            }
            else if (getIsCreationWindow()) {
                setText(team.toString());
            }
            else {
                Hyperlink text = new Hyperlink(team.toString());
                text.setOnAction(param -> NavigationManager.navigateTo(team));
                setGraphic(text);
            }
        }
    }

    /**
     * Used to represent a date cell that can contain a null value.
     */
    private class NullableLocalDateCell extends TableCell<WorkAllocation, LocalDate> {
        /**
         * The format for displaying the date in a cell.
         */
        private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        @Override
        protected void updateItem(final LocalDate date, final boolean empty) {
            super.updateItem(date, empty);
            if (date != null) {
                setText(dateFormatter.format(date));
            }
            else {
                setText(null);
            }
        }
    }

    /**
     * Stuff and things.
     * @param node Node
     * @param label Label
     */
    public final void registerFormLabel(final Node node, final Label label) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                label.getStyleClass().remove("input-label");
                label.getStyleClass().add("input-label-focused");
            }
            else {
                label.getStyleClass().remove("input-label-focused");
                label.getStyleClass().add("input-label");
            }
        });
    }
}
