package sws.murcs.controller.windowManagement;

import javafx.stage.Stage;
import sws.murcs.listeners.GenericCallback;
import sws.murcs.view.App;

/**
 * A Wrapper class for linking stages and controllers together, so that they can be managed.
 */
public class Window {

    /**
     * The stage of the window.
     */
   protected Stage stage;

    /**
     * The controller for the window.
     */
    protected Object controller;

    /**
     * Creates a new window containing a stage and a controller.
     * @param pStage The stage.
     * @param pController The controller.
     */
    public Window(final  Stage pStage, final Object pController) {
        stage = pStage;
        controller = pController;
    }


    /**
     * Gets the stage of the window.
     * @return The stage.
     */
    public final Stage getStage() {
        return stage;
    }

    /**
     * Gets the controller of the window.
     * @return The controller.
     */
    public final Object getController() {
        return controller;
    }

    /**
     * Override for when we don't want to pass a callback.
     */
    public final void close() {
        close(() -> { });
    }

    /**
     * Ensures that the window is closed properly.
     * @param callback A function to call after the stage has closed.
     */
    public final void close(final GenericCallback callback) {
        App.getWindowManager().removeWindow(this);
        stage.close();
        callback.call();
    }

    /**
     * Registers a window with the window manager.
     * And ensures that the default on closed request is handled by the window manager.
     */
    public final void register() {
        App.getWindowManager().addWindow(this);
        stage.setOnCloseRequest((event -> {
            App.getWindowManager().removeWindow(this);
        }));
    }

    /**
     * Adds global shortcuts to window.
     */
    public final void addGlobalShortcutsToWindow() {
        App.getShortcutManager().addAllShortcutsToWindow(this);
    }

    /**
     * Shows a stage.
     */
    public final void show() {
        stage.show();
    }
}
