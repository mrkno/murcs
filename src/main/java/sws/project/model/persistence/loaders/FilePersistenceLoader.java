package sws.project.model.persistence.loaders;

import sws.project.model.Model;

import java.io.*;
import java.util.ArrayList;

/**
 * Manages loading persistent data from the local HD using binary serialization.
 */
public class FilePersistenceLoader implements PersistenceLoader {

    private String workingDirectory = System.getProperty("user.dir");

    /**
     * Instantiates a new FilePersistenceLoader.
     * Blank constructor for default usage.
     */
    public FilePersistenceLoader(){}

    /**
     * Instantiates a new FilePersistenceLoader.
     * @param directory Directory to use persistent data in.
     */
    public FilePersistenceLoader(String directory)
    {
        this.workingDirectory = directory;
    }

    /**
     * Gets the storage directory of persistent data.
     * @return The location the persistent data that is stored on the HD.
     */
    private String getDirectory()
    {
        // return the current working directory
        return workingDirectory;
    }

    /**
     * Loads model from the disk.
     * @param persistenceName The name of the persistent file to load
     * @return The loaded model.
     */
    @Override
    public Model loadModel(String persistenceName)
    {
        // load the persistent file using the default directory
        return loadModel(persistenceName, getDirectory());
    }

    /**
     * Loads a model from the disk.
     * @param persistenceName The name of the persistent file to load
     * @param directory The directory to load the persistent file from.
     * @return The loaded model.
     */
    public Model loadModel(String persistenceName, String directory){
        try
        {
            // Open the model file
            String persistentFileLocation = directory + File.separator + persistenceName + ".project";
            // Create an object reading stream
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(persistentFileLocation));
            // Input and case to correct type
            Model userIn = (Model)in.readObject();
            // Close input stream
            in.close();
            return userIn;
        }
        catch (Exception e)
        {
            // What the hell happened?
            System.err.println("An error occured while loading the persistent file:\n" + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a model out to a file in the default directory.
     * @param persistent Model to save.
     * @throws Exception When a model fails to save.
     */
    @Override
    public void saveModel(Model persistent) throws Exception
    {
        // saves the model using the default directory
        saveModel(persistent, getDirectory());
    }

    /**
     * Saves a model out to a file.
     * @param persistent Model to save.
     * @param directory Directory to save the model in.
     */
    public void saveModel(Model persistent, String directory) throws Exception
    {
        try
        {
            // Open the persistent file
            String persistenceFileLocation = directory + File.separator + persistent.longName + ".project";
            // Open object stream to file
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(persistenceFileLocation));
            // Write the object out to the file
            out.writeObject(persistent);
            // close the stream
            out.close();
        }
        catch (Exception e)
        {
            // What the hell happened?
            System.err.println("An error occured while saving the persistent file:\n" + e.getMessage());
            throw new Exception("Persistent file not loaded.", e);
        }
    }

    /**
     * Gets a list of models that exist.
     * @return List of models.
     */
    @Override
    public ArrayList<String> getModelList()
    {
        return getModelList(getDirectory());
    }

    /**
     * Returns a list of models that exist in a directory.
     * @param directory Directory to search
     * @return list of models
     */
    public static ArrayList<String> getModelList(String directory)
    {
        ArrayList<String> persistentList = new ArrayList<String>();
        File dir = new File(directory); // create handle to directory
        for (File f : dir.listFiles())
        {
            String name = f.getName();
            if (name.endsWith(".project")) // check if it ends with the correct ext
            {
                // if it does add
                persistentList.add(name.substring(0, name.indexOf(".project")));
            }
        }
        return persistentList;
    }

    /**
     * Deletes the specified persistent file
     * @param persistenceName The name of the persistent file
     * @return Whether the operation was successful
     */
    @Override
    public boolean deleteModel(String persistenceName)
    {
        return deletePersistence(persistenceName, getDirectory());
    }

    /**
     * Deletes the specified persistent file from the directory
     * @param persistentName The name of the persistent file
     * @param directory The Directory to search
     * @return Whether the operation was successful
     */
    public boolean deletePersistence(String persistentName, String directory)
    {
        try
        {
            File persistenceFile = new File(directory + File.separator + persistentName + ".project");

            if(persistenceFile.delete())
            {
                return true;
            }
            else
            {
                throw new Exception("File delete failed");
            }
        }
        catch (Exception e)
        {
            System.err.println("Deleting persistent data failed with error:\n" + e.getMessage());
            return false;
        }
    }
}

