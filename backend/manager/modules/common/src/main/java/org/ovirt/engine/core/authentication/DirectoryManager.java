package org.ovirt.engine.core.authentication;

import java.util.List;

/**
 * The directory manager is responsible for managing a collection of directory objects, like LDAP directories, for
 * example.
 */
public class DirectoryManager extends Manager<Directory> {
    /**
     * This is a singleton, and this is the instance.
     */
    private static DirectoryManager instance;

    static {
        instance = new DirectoryManager();
    }

    /**
     * Get an instance of the directory manager.
     */
    public static DirectoryManager getInstance() {
        return instance;
    }

    private DirectoryManager() {
        super(DirectoryFactory.class);
    }

    /**
     * Parses a directory configuration file and creates an instance with that configuration.
     *
     * @param config the properties already loaded from the configuration file
     * @return the reference to the loaded directory or {@code null} if something fails while parsing the configuration
     */
    public Directory parseDirectory(Configuration config) {
        return parseObject(config);
    }

    /**
     * Returns an unmodifiable list containing all the directories that have been previously loaded.
     */
    public List<Directory> getDirectories() {
        return getObjects();
    }

    /**
     * Gets the directory for the given name.
     *
     * @param name the name of the directory
     * @return the requested directory instance or {@code null} if no such implementation can be found
     */
    public Directory getDirectory(String name) {
        return getObject(name);
    }

    /**
     * Register a directory.
     *
     * @param id the identifier of the directory
     * @param directory the directory to register
     */
    public void registerDirectory(String id, Directory directory) {
        registerObject(id, directory);
    }
}
