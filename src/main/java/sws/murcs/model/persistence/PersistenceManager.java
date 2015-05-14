package sws.murcs.model.persistence;

import sws.murcs.model.RelationalModel;
import sws.murcs.model.persistence.loaders.PersistenceLoader;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides methods for managing models.
 */
public final class PersistenceManager {

    /**
     * Static persistence manager, used to keep a persistent manager where ever it is used from.
     */
    public static PersistenceManager Current;

    /**
     * Indicates whether or not there is a current persistence manage.
     * @return The persistence manager
     */
    public static boolean CurrentPersistenceManagerExists() {
        return Current != null;
    }

    /**
     * Currently in use user persistence.
     */
    private RelationalModel currentModel;

    /**
     * Currently in use persistence loader.
     */
    private PersistenceLoader persistenceLoader;

    /**
     * Instantiates a new persistence manager.
     * @param loader Loader to use to read and write persistences
     */
    public PersistenceManager(final PersistenceLoader loader) {
        setPersistenceLoader(loader);
    }

    /**
     * Gets the current in use persistence loader.
     * @return current persistence loader
     */
    public PersistenceLoader getPersistenceLoader() {
        return persistenceLoader;
    }

    /**
     * Sets the currently in use persistence loader.
     * @param loader Persistence loader to use.
     */
    public void setPersistenceLoader(final PersistenceLoader loader) {
        this.persistenceLoader = loader;
    }

    /**
     * Loads a model from the disk.
     * @param persistenceName The name of the model to load
     * @return The loaded persistence. Will be null if the persistence does not exist
     */
    public RelationalModel loadModel(final String persistenceName) {
        // load the persistence using the default directory
        return persistenceLoader.loadModel(persistenceName);
    }

    /**
     * Saves the current model.
     * @param name name to save as
     * @throws Exception when the model fails to save.
     */
    public void saveModel(final String name) throws Exception {
        saveModel(name, getCurrentModel());
    }

    /**
     * Saves a model..
     * @param name name to save as
     * @param persistence Persistence to save.
     * @throws Exception When a model fails to save.
     */
    public void saveModel(final String name, final RelationalModel persistence) throws Exception
    {
        // saves the model using the default directory
        persistenceLoader.saveModel(name, persistence);
    }

    /**
     * Checks to see if a model exists.
     * @param persistenceName The name of the model
     * @return Whether the persistence exists
     */
    public boolean modelExists(final String persistenceName) {
        Collection<String> persistences = getModels();
        return persistences.contains(persistenceName);
    }

    /**
     * Returns a list of models that exist in the default location.
     * @return gets a list of models that exist in the default location.
     */
    public Collection<String> getModels() {
        return persistenceLoader.getModelList();
    }

    /**
     * Deletes the specified model.
     * @param persistenceName The name of the model
     * @return Whether the operation was successful
     */
    public boolean deleteModel(final String persistenceName) {
        return persistenceLoader.deleteModel(persistenceName);
    }

    /**
     * Gets the current model.
     * @return The current model. Null if it has not been set.
     */
    public RelationalModel getCurrentModel() {
        return currentModel;
    }

    /**
     * Sets the current model.
     * @param model The new model
     */
    public void setCurrentModel(final RelationalModel model) {
        currentModel = model;
    }

    /**
     * Gets the current working directory.
     * @return the current working directory.
     * @throws Exception if current working directory is unapplicable to the current PersistenceLoader
     */
    public String getCurrentWorkingDirectory() throws Exception {
        return persistenceLoader.getCurrentWorkingDirectory();
    }

    /**
     * Sets the current working directory.
     * @param directory the new working directory.
     * @throws Exception if current working directory is unapplicable to the current PersistenceLoader
     */
    public void setCurrentWorkingDirectory(final String directory) throws Exception {
        persistenceLoader.setCurrentWorkingDirectory(directory);
    }
}
