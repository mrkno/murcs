package sws.murcs.controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sws.murcs.controller.editor.BacklogEditor;
import sws.murcs.controller.tabs.Tabbable;
import sws.murcs.controller.windowManagement.Window;
import sws.murcs.debug.errorreporting.ErrorReporter;
import sws.murcs.listeners.ViewUpdate;
import sws.murcs.magic.tracking.UndoRedoManager;
import sws.murcs.magic.tracking.listener.ChangeState;
import sws.murcs.magic.tracking.listener.UndoRedoChangeListener;
import sws.murcs.model.Backlog;
import sws.murcs.model.Model;
import sws.murcs.model.ModelType;
import sws.murcs.model.Organisation;
import sws.murcs.model.Person;
import sws.murcs.model.Project;
import sws.murcs.model.Release;
import sws.murcs.model.Skill;
import sws.murcs.model.Story;
import sws.murcs.model.Team;
import sws.murcs.model.helpers.UsageHelper;
import sws.murcs.model.observable.ModelObservableArrayList;
import sws.murcs.model.persistence.PersistenceManager;
import sws.murcs.view.App;
import sws.murcs.view.CreatorWindowView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Main app class controller. This controls all the main window functionality, so anything that isn't in a separate
 * window is controlled here.
 */
public class AppController implements ViewUpdate<Model>, UndoRedoChangeListener, Tabbable, ToolBarCommands {
    /**
     * The main display of the window which contains the display
     * list and the content pane.
     */
    @FXML
    private HBox hBoxMainDisplay;

    /**
     * The choice box for selecting the model type to be
     * displayed in the list.
     */
    @FXML
    private ChoiceBox<ModelType> displayChoiceBox;

    /**
     * The list which contains the models of the type selected
     * in the display list choice box.
     */
    @FXML
    private ListView displayList;

    /**
     * The content pane contains the information about the
     * currently selected model item.
     */
    @FXML
    private GridPane contentPane;

    /**
     * The Vbox containing the display list
     */
    @FXML
    private VBox vBoxSideDisplay;

    /**
     * A creation window.
     */
    private CreatorWindowView creatorWindow;

    /**
     * The content pane to show the view for a model.
     */
    private EditorPane editorPane;

    /***
     * The toolbar associated with this controller
     */
    private ToolBarController toolBarController;

    /**
     * The navigation manager for the controller.
     */
    private NavigationManager navigationManager;

    /**
     * Initialises the GUI, setting up the the options in the choice box and populates the display list if necessary.
     * Put all initialisation of GUI in this function.
     */
    @FXML
    public final void initialize() {
        navigationManager = new NavigationManager();
        navigationManager.setAppController(this);

        for (ModelType type : ModelType.values()) {
            displayChoiceBox.getItems().add(type);
        }
        displayChoiceBox.getStyleClass().add("no-shadow");
        displayChoiceBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observer, oldValue, newValue) -> updateList());

        displayChoiceBox.getSelectionModel().select(0);
        displayList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                if (editorPane != null && newValue != null) {
                    editorPane.getController().saveChanges();
                }
                updateDisplayListSelection(newValue, oldValue);
            }
        });

        UndoRedoManager.addChangeListener(this);
        updateList();
    }

    /**
     * Updates the currently selected item in the display list.
     * @param newValue The new value
     * @param oldValue The old value
     */
    private void updateDisplayListSelection(final Object newValue, final Object oldValue) {
        if (newValue == null && editorPane != null) {
            editorPane.dispose();
            editorPane = null;
            contentPane.getChildren().clear();

            return;
        }

        navigationManager.navigateTo((Model) newValue);
        toolBarController.updateBackForwardButtons();

        if (oldValue == null) {
            displayList.scrollTo(newValue);
        }
    }

    /**
     * Updates the display list on the left hand side of the screen.
     */
    private void updateList() {
        ModelType type = ModelType.getModelType(displayChoiceBox.getSelectionModel().getSelectedIndex());
        displayList.getSelectionModel().clearSelection();
        Organisation model = PersistenceManager.getCurrent().getCurrentModel();

        if (model == null) {
            return;
        }

        List<? extends Model> arrayList;
        switch (type) {
            case Project: arrayList = model.getProjects(); break;
            case Person: arrayList = model.getPeople(); break;
            case Team: arrayList = model.getTeams(); break;
            case Skill: arrayList = model.getSkills(); break;
            case Release: arrayList = model.getReleases(); break;
            case Story: arrayList = model.getStories(); break;
            case Backlog: arrayList = model.getBacklogs(); break;
            default: throw new UnsupportedOperationException();
        }

        if (arrayList.getClass() == ModelObservableArrayList.class) {
            ModelObservableArrayList<? extends Model> arrList = (ModelObservableArrayList) arrayList;
            arrayList = new SortedList<>(arrList, (Comparator<? super Model>) arrList);
        }
        else {
            System.err.println("This list type does not yet have an ordering specified, "
                    + "please correct this so that the display list is shown correctly.");
        }

        displayList.setItems((ObservableList) arrayList);
        displayList.getSelectionModel().select(0);
    }

    /**
     * Adds a new item from a given model type.
     * @param modelType The type to get the class from.
     */
    private void addNewItem(final ModelType modelType) {
        Class<? extends Model> clazz = ModelType.getTypeFromModel(modelType);
        createNewItem(clazz);
    }

    /**
     * Creates a new model from a class instance.
     * @param clazz the class to create the model instance from.
     */
    private void createNewItem(final Class<? extends Model> clazz) {
        if (clazz != null) {
            try {
                creatorWindow = new CreatorWindowView(clazz.newInstance(),
                        model -> {
                            selectItem(model);
                            if (creatorWindow != null) {
                                creatorWindow.dispose();
                                creatorWindow = null;
                            }
                        },
                        func -> {
                            if (creatorWindow != null) {
                                creatorWindow.dispose();
                                creatorWindow = null;
                            }
                        });
                creatorWindow.show();
            }
            catch (InstantiationException | IllegalAccessException e) {
                ErrorReporter.get().reportError(e, "Initialising a creation window failed");
            }
        }
    }

    /**
     * Function that is called when a new model is created from the plus button.
     * @param event The event of the add button being called
     */
    @FXML
    public final void add(final ActionEvent event) {
        Class<? extends Model> clazz = null;
        if (event != null && event.getSource() instanceof MenuItem) {
            //If pressing a menu item to add a person, team or skill
            String id = ((MenuItem) event.getSource()).getId();
            switch (id) {
                case "addProject":
                    clazz = Project.class;
                    break;
                case "addPerson":
                    clazz = Person.class;
                    break;
                case "addTeam":
                    clazz = Team.class;
                    break;
                case "addSkill":
                    clazz = Skill.class;
                    break;
                case "addRelease":
                    clazz = Release.class;
                    break;
                case "addBacklog":
                    clazz = Backlog.class;
                    break;
                case "addStory":
                    clazz = Story.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Adding has not been implemented.");
            }
        }
        else {
            //If pressing the add button at the bottom of the display list
            ModelType type = ModelType.getModelType(displayChoiceBox.getValue());
            clazz = ModelType.getTypeFromModel(type);
        }
        createNewItem(clazz);
    }

    @Override
    public void back(ActionEvent event) {
        navigationManager.goBack();
    }

    @Override
    public void forward(ActionEvent event) {
        navigationManager.goForward();
    }

    /**
     * Toggles the view of the display list box at the side.
     * @param event The event that triggers the function
     */
    @FXML
    private void toggleItemListView(final ActionEvent event) {
        if (!vBoxSideDisplay.managedProperty().isBound()) {
            vBoxSideDisplay.managedProperty().bind(vBoxSideDisplay.visibleProperty());
        }
        vBoxSideDisplay.setVisible(!vBoxSideDisplay.isVisible());
    }

    /**
     * Switches the state of the story highlighting.
     * @param event The event that is fired when the Highlight Stories menu item is clicked
     */
    @FXML
    private void toggleBacklogStories(final ActionEvent event) {
        BacklogEditor.toggleHighlightState();
    }


    /**
     * Function that is called when you want to delete a model.
     * @param event Event that sends you to the remove clicked function
     */
    @FXML
    public final void remove(final ActionEvent event) {
        Organisation model = PersistenceManager.getCurrent().getCurrentModel();
        if (model == null) {
            return;
        }

        final int selectedIndex = displayList.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        Model selectedItem = (Model) displayList.getSelectionModel().getSelectedItem();

        // Ensures you can't delete Product Owner or Scrum Master
        if (ModelType.getModelType(displayChoiceBox.getSelectionModel().getSelectedIndex()) == ModelType.Skill) {
            if (selectedItem.getShortName().equals("PO") || selectedItem.getShortName().equals("SM")) {
                return;
            }
        }

        Collection<Model> usages = UsageHelper.findUsages(selectedItem);
        //Get the current window. Dion's stuff needs this.
        Window window = App.getWindowManager()
                .getAllWindows()
                .stream()
                .filter(w -> w.getStage() == hBoxMainDisplay.getScene().getWindow())
                .findFirst()
                .orElse(null);

        GenericPopup popup = new GenericPopup(window);
        String message = "Are you sure you want to delete this?";
        if (usages.size() != 0) {
            message += "\nThis ";
            ModelType type = ModelType.getModelType(selectedItem);
            message += type.toString().toLowerCase() + " is used in " + usages.size() + " place(s):";
            for (Model usage : usages) {
                message += "\n" + usage.getShortName();
            }
        }
        popup.setTitleText("Really delete?");
        popup.setMessageText(message);

        popup.addButton("Yes", GenericPopup.Position.RIGHT, GenericPopup.Action.DEFAULT, () -> {
            popup.close();
            navigationManager.clearHistory();
            Model item = (Model) displayList.getSelectionModel().getSelectedItem();
            model.remove(item);
            toolBarController.updateBackForwardButtons();
        }, "danger-will-robinson");
        popup.addButton("No", GenericPopup.Position.RIGHT, GenericPopup.Action.CANCEL, popup::close, "dont-panic");
        popup.show();
    }

    @Override
    public final void selectItem(Model parameter) {
        ModelType type;
        ModelType selectedType = ModelType.getModelType(displayChoiceBox.getSelectionModel().getSelectedIndex());

        if (parameter == null) {
            displayList.getSelectionModel().select(0);
            displayList.scrollTo(0);
            parameter = (Model) displayList.getSelectionModel().getSelectedItem();
        }
        else {
            type = ModelType.getModelType(parameter);
            if (type != selectedType) {
                NavigationManager.setIgnore(true);
                displayChoiceBox.getSelectionModel().select(ModelType.getSelectionType(type));
                NavigationManager.setIgnore(false);
            }
            if (parameter != displayList.getSelectionModel().getSelectedItem()) {
                displayList.getSelectionModel().select(parameter);
                displayList.scrollTo(parameter);
            }

            if (displayList.getSelectionModel().getSelectedIndex() < 0) {
                displayList.scrollTo(parameter);
            }
        }

        // The remove button should be greyed out
        // if no item is selected or built in skills (PO or SM) are selected
        toolBarController.removeButtonDisabled(parameter == null
                || parameter instanceof Skill
                && (((Skill) parameter).getShortName().equals("PO")
                || ((Skill) parameter).getShortName().equals("SM")));

        if (editorPane == null) {
            editorPane = new EditorPane(parameter);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(editorPane.getView());
        }
        else {
            if (editorPane.getModel().getClass() == parameter.getClass()) {
                editorPane.setModel(parameter);
            }
            else {
                editorPane.dispose();
                contentPane.getChildren().clear();
                editorPane = new EditorPane(parameter);
                contentPane.getChildren().add(editorPane.getView());
            }
        }
    }

    /**
     * Gets the toolBar controller.
     * @return The toolBar controller.
     */
    @Override
    public ToolBarController getToolBar() {
        return toolBarController;
    }

    @Override
    public String getTitle() {
        //TODO implement the observable pattern and
        //fire events when we change this
        return "Model View";
    }

    /**
     * The root of the current form
     * @return The root node
     */
    @Override
    public final Parent getRoot() {
        return hBoxMainDisplay;
    }

    @Override
    public Object getController() {
        return null;
    }

    /**
     * Navigates back.
     */
    @FXML
    public final void goBack() {
        if (!NavigationManager.canGoBack()) {
            return;
        }
        displayList.getSelectionModel().clearSelection();
        NavigationManager.goBack();
    }

    @Override
    public boolean canGoForward() {
        return NavigationManager.canGoForward();
    }

    @Override
    public boolean canGoBack() {
        return NavigationManager.canGoBack();
    }

    /**
     * Navigates forward.
     */
    @FXML
    public final void goForward() {
        if (!NavigationManager.canGoForward()) {
            return;
        }
        displayList.getSelectionModel().clearSelection();
        NavigationManager.goForward();
    }

    @Override
    public void undoRedoNotification(ChangeState param) {
        //When something has been updated we may have items added/removed from
        //the display list, so we should update it.
        updateList();
    }

    public void setToolBarController(ToolBarController toolBarController) {
        this.toolBarController = toolBarController;
    }
}
