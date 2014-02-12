package org.ovirt.engine.core.aaa;

import java.util.HashMap;
import java.util.Map;

/**
 * This class needs to be overriden because it is used by the directory custom field serializer, and thus GWT wants its
 * source, but we can't use the original source because it makes references to things that aren't supported by GWT, like
 * files, configuration, etc. Note that this overriden class doesn't need to implement the same interfaces or methods
 * than the original one, only those methods that are actually used in the GWT projects.
 */
public class DirectoryManager {
    /**
     * This is a singleton and this is the instance.
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

    /**
     * In the UI directories are replaced by stubs that only contain the name of the directory, and we store them here
     * to avoid creating multiple instances.
     */
    private Map<String, Directory> stubs = new HashMap<String, Directory>();

    private DirectoryManager() {
        // Nothing.
    }

    public Directory getDirectory(String name) {
        Directory stub = stubs.get(name);
        if (stub == null) {
            stub = new DirectoryStub(name);
            stubs.put(name, stub);
        }
        return stub;
    }
}
