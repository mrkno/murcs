package sws.murcs.controller.editor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import sws.murcs.debug.errorreporting.ErrorReporter;
import sws.murcs.model.Sprint;
import sws.murcs.model.Story;
import sws.murcs.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Scrum Board controller. This consists of several story
 * controllers.
 */
public class ScrumBoard extends GenericEditor<Sprint> {

    /**
     * The VBox to add the stories to.
     */
    @FXML
    private VBox storiesVBox;

    /**
     * The parent AnchorPane.
     */
    @FXML
    private AnchorPane mainView;

    /**
     * The header grid pane.
     */
    @FXML
    private GridPane header;

    /**
     * The parent SprintContainer of this view.
     */
    private SprintContainer parent;

    /**
     * The task currently being dragged.
     */
    private Task draggingTask;

    /**
     * The story of the currently dragging task.
     */
    private Story draggingStory;

    /**
     * The sprint this scrum board is displaying.
     */
    private Sprint currentSprint;

    /**
     * The list of controllers for the stories on the scrum board.
     */
    private List<ScrumBoardStoryController> scrumBoardStories;

    @Override
    protected void initialize() {
        scrumBoardStories = new ArrayList<>();
        mainView.getStyleClass().add("root");
        mainView.getStyleClass().add("scrumBoard");
        header.getStyleClass().add("scrumBoard-header");
        currentSprint = getModel();
    }

    /**
     * Sets the parent SprintContainer of this view.
     * @param container The parent of this view
     */
    protected void setParent(final SprintContainer container) {
        parent = container;
    }

    @Override
    public void loadObject() {
        if (getModel() != currentSprint) {
            currentSprint = getModel();
            storiesVBox.getChildren().clear();
            for (Story story : getModel().getStories()) {
                insertStory(story);
            }
        }
        scrumBoardStories.stream().forEach(ScrumBoardStoryController::update);
    }

    /**
     * Adds a story to the list.
     * @param story The story to add to the VBox
     */
    private void insertStory(final Story story) {

        FXMLLoader loader = new FXMLLoader(ScrumBoardStoryController.class.getResource("/sws/murcs/ScrumBoardStory.fxml"));
        try {
            Parent root = loader.load();
            ScrumBoardStoryController controller = loader.getController();
            controller.setup(story, parent);
            controller.setStory(story);
            controller.loadStory();
            scrumBoardStories.add(controller);
            storiesVBox.getChildren().add(root);
        } catch (IOException e) {
            ErrorReporter.get().reportError(e, "Failed to load story in scrumBoard");
        }
    }

    /**
     * Adds a double click handler to a node that loads a TaskEditor into a new window.
     * @param node The node to apply the handler to
     * @param story The story to load
     */
    private void addDoubleClickHandler(final Node node, final Story story) {
        node.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                parent.getNavigationManager().navigateToNewTab(story);
            }
        });
    }

    @Override
    protected void saveChangesAndErrors() {
    }

    @Override
    public void dispose() {
    }
}
