package sws.murcs.controller.windowManagement;


import javafx.collections.FXCollections;
import sws.murcs.controller.AppController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manages the windows visible to the viewer, keeps the order of what windows are on top of each other.
 */
public class WindowManager {

    /**
     * Contains a list of windows on the screen.
     */
    private List<Window> windows;

    /**
     * Creates a new window manager.
     */
    public WindowManager() {
        windows = FXCollections.observableArrayList();
    }

    /**
     * Brings the window on top of all other windows.
     * @param window The window to bring to the top
     */
    public final void bringToTop(final Window window) {
        System.out.println("bring to top");
        windows.remove(window);
        windows.add(0, window);
        //window.stage.toFront();
    }

    /**
     * Sends a window to behind all other windows.
     * @param window The window to move to the back
     */
    public final void sendToBottom(final Window window) {
        int newIndex = windows.size() - 1;
        windows.remove(window);
        windows.add(newIndex, window);
        reOrderWindows();
    }

    /**
     * Sends a window one position back.
     * @param window The window to move.
     */
    public final void sendBackwards(final Window window) {
        sendBackwards(window, 1);
    }

    /**
     * Sends a window back a given number of positions.
     * @param window The window to move.
     * @param howFar The number of positions to move the window.
     */
    public final void sendBackwards(final Window window, final int howFar) {
        windows.remove(window);
        if (windows.indexOf(window) + howFar > windows.size()) {
            windows.add(windows.size() - 1, window);
        }
        else {
            windows.add(windows.indexOf(window) + howFar, window);
        }
        reOrderWindows();
    }

    /**
     * Sends the window forward one position.
     * @param window The window to move.
     */
    public final void sendForwards(final Window window) {
        sendForwards(window, 1);
    }

    /**
     * Sends a window forward a given number of positions.
     * @param window The window to move.
     * @param howFar The number of positions to move the window.
     */
    public final void sendForwards(final Window window, final int howFar) {
        windows.remove(window);
        if (windows.indexOf(window) - howFar < 0) {
            windows.add(0, window);
        }
        else {
            windows.add(windows.indexOf(window) - howFar, window);
        }
        reOrderWindows();
    }

    /**
     * Gets the front most window.
     * @return The window at the front.
     */
    public final Window getTop() {
         return windows.get(0);
    }

    /**
     * Removes a window from the manager.
     * @param window The window to remove
     */
    public final void removeWindow(final Window window) {
        System.out.println("removing window");
        windows.remove(window);
        reOrderWindows();
    }

    /**
     * Adds a window to the top most position in windows.
     * @param window new window to register.
     */
    public final void addWindow(final Window window) {
        System.out.println("adding window");
        windows.add(0, window);
        window.stage.toFront();
    }

    /**
     * Adds a window behind all other windows.
     * @param window The window to add.
     */
    public final void addWindowToBack(final Window window) {
        windows.add(windows.size(), window);
        reOrderWindows();
    }

    /**
     * Adds a window with a given position.
     * @param window The window to add.
     * @param pos The position to place it.
     */
    public final void addWindowWithPos(final Window window, final int pos) {
        windows.add(window);
        setWindowPosition(window, pos);
    }

    /**
     * Gets the position of the given window.
     * @param window The Window to get the position for.
     * @return The position.
     */
    public final int getWindowPosition(final Window window) {
        return windows.indexOf(window);
    }

    /**
     * Sets the position of a window.
     * @param window The window to change the position of.
     * @param newPosition The new position to set the window to.
     */
    public final void setWindowPosition(final Window window, final int newPosition) {
        windows.remove(window);
        if (newPosition <= 0) {
            windows.add(0, window);
        }
        else if (newPosition < windows.size()) {
            windows.add(newPosition, window);
        }
        else {
            windows.add(windows.size() - 1, window);
        }
        reOrderWindows();
    }

    /**
     * Reorders how the list of windows is displayed, according to the order of the list.
     * With the first index of the list being the window shown at the top.
     */
    private void reOrderWindows() {
        System.out.println("reordering");
        for (int i = windows.size() - 1; i > -1; i--) {
            windows.get(i).getStage().toFront();
        }
    }

    /**
     * Gets a collection of unmodifiable windows.
     * @return windows.
     */
    public final Collection<Window> getAllWindows() {
        return Collections.unmodifiableCollection(windows);
    }

    /**
     * Gets the instance of the controller for a given window.
     * @param window The window to get the controller for.
     * @return the controller object.
     */
    public final Object getController(final Window window) {
        return window.controller;
    }

    /**
     * Cleans up all open windows which are not the main controller.
     */
    public final void cleanUp() {
        System.out.println("cleaning up");
        for (int i = 0; i < windows.size(); i++) {
            if (windows.get(i).getController().getClass() != AppController.class) {
                windows.get(i).close();
                i--;
            }
        }
    }
}
