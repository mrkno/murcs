package sws.project.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sws.project.view.App;

import java.util.concurrent.Callable;

/**
 * Created by James on 12/03/2015.
 */
public class GenericPopup extends AnchorPane {


    /***
     * Enum for specifying which side of the dialog you want the button to appear on.
     */
    public enum Position {
        LEFT,
        RIGHT
    }

    private @FXML Text messageText;
    private @FXML Text messageTitle;
    private @FXML ImageView messageImage;
    private @FXML HBox titleImageHBox;
    //Contains left aligned buttons
    private @FXML HBox hBoxLeft;
    //Contains right align buttons
    private @FXML HBox hBoxRight;

    private Stage popupStage;
    private Scene popupScene;

    /***
     * Constructs a new Generic Popup. In order to use you need to at least set the message and add at least 1 button
     * some examples of how to use this include:
     *
     *      GenericPopup controller = new GenericPopup();
     *      controller.setMessageText("Test message");
     *      controller.addButton("Cancel", GenericPopup.Position.RIGHT, () -RIGHTARROW {controller.close(); return null;});
     *      controller.addButton("OK", GenericPopup.Position.RIGHT, () -RIGHTARROW {controller.close(); return null;});
     *      controller.addButton("Thingy", GenericPopup.Position.LEFT, () -RIGHTARROW {controller.close(); return null;});
     *      controller.show();
     *
     * There are extra features, like you can add and image and title, change the window title as well
     *
     * NOTE: All lambdas passed into addButton need to include a return null; at the end (don't ask me why it's black
     * magic I tell you)
     */
    public GenericPopup() {
        this(null);
    }

    /***
     * Constructs a dialog from an exception with an ok option that closes the dialog. Shows the exception message in
     * the dialog.
     * @param exception The exception that you want to feed in to show the exception message.
     */
    public GenericPopup(Exception exception) {
        popupStage = new Stage();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sws/project/GenericPopup.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO catch this nicely
        }
        popupScene = new Scene(this, 400, 200);
        popupStage.initOwner(App.stage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(popupScene);

        if (exception != null) {
            setMessageText(exception.getMessage());
            addOkButton(() -> {this.close(); return null;});
        }
    }


    /***
     * Adds a new button to the dialog. You must specify the text to go on the button, it's location on the dialog
     * (either the left hand side or the right hand side) and the function to call when it is clicked (this must be a
     * function that implements callable or just a lambda function with return null; at the end of it).
     *
     * NOTE: Buttons stack on the left and right sides, therefore if you add two buttons on the left the first one added
     * will be the one closest to the left hand side, so keep that in mind.
     * @param buttonText The text on the button.
     * @param position The positioning of the button.
     * @param func The function to call when the button is clicked.
     */
    public void addButton(String buttonText, Position position, Callable<Void> func) {
        Button button = new Button(buttonText);
        button.setPrefSize(70, 25);
        button.setOnAction((a) -> {
            try {
                func.call();
            } catch (Exception e) {
                //Todo catch this
            }
        });
        switch (position) {
            case LEFT:
                hBoxLeft.getChildren().add(button);
                break;
            case RIGHT:
                hBoxRight.getChildren().add(button);
                break;
        }
    }

    /***
     * Shows the dialog, this should be the last thing you call after setting up your dialog. If you have not set up a
     * title the dialog will automatically remove it and resize.
     */
    public void show() {
        if (messageTitle.getText().equals("Title")) {
            titleImageHBox.managedProperty().bind(titleImageHBox.visibleProperty());
            titleImageHBox.setVisible(false);
            popupStage.setHeight(150);
        }
        popupStage.show();
    }

    /***
     * Closes the dialog.
     *
     * Note: You may want to set up one of your buttons to call this, although if you use the addOkCancelButtons() with
     * only one lambda expression then the cancel button is automatically set to call this.
     */
    public void close() {
        popupStage.close();
    }

    /***
     * Set the message you want the the dialog to show, text will wrap but the dialog does not resize currently so don't
     * make it too long.
     * @param message The message you want to show on the dialog.
     */
    public void setMessageText(String message) {
        messageText.setText(message);
    }

    /***
     * Sets the title of the window (the bit that appears in the bar at the top)
     * @param title The window title.
     */
    public void setWindowTitle(String title) {
        popupStage.setTitle(title);
    }

    /***
     * Sets the title of the message (appears alongside the title image)
     * @param titleText The title of the message.
     */
    public void setTitleText(String titleText) {
        if (titleText == null) return;
        messageTitle.setText(titleText);
    }

    /***
     * Sets the image to appear beside the title.
     *
     * NOTE: If you don't set the title text (not the window text) then this won't appear.
     * @param image image to set.
     */
    public void setTitleImage(Image image) {
        messageImage.setImage(image);
        messageImage.setFitWidth(50);
        messageImage.setFitHeight(50);
    }

    /***
     * Adds default OK Cancel Buttons, you specify what is supposed to happen for the ok button and the cancel button
     * remains it's default (closes the dialog)
     * @param okFunction The function you want to call on the ok button being clicked.
     */
    public void addOkCancelButtons(Callable<Void> okFunction) {
        addOkCancelButtons(okFunction, () -> {this.close(); return null;});
    }

    /***
     * Adds default OK Cancel Buttons, you specify the functions for both the ok and cancel buttons when clicked
     * @param okFunction The function you want to call on ok button click
     * @param cancelFunction The function you want to call on cancel button click
     */
    public void addOkCancelButtons(Callable<Void> okFunction, Callable<Void> cancelFunction) {
        addButton("Cancel", Position.RIGHT, cancelFunction);
        addButton("OK", Position.RIGHT, okFunction);
    }

    /***
     * Adds the default OK button with a specified function to call on it being clicked.
     * @param okFunction Function to call on ok button being clicked.
     */
    public void addOkButton(Callable<Void> okFunction) {
        addButton("OK", Position.RIGHT, okFunction);
    }
}
