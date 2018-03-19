package org.ovirt.engine.core.bll.memory;

import org.ovirt.engine.core.compat.Guid;

public interface MemoryImageBuilder {
    /**
     * Create the images
     */
    void build();

    /**
     * Return whether this builder creates tasks in {@link #build()}
     * @return true if tasks are created in {@link #build()}, false otherwise
     */
    boolean isCreateTasks();

    /**
     * Return the id of the disk that holds the memory dump
     * @return Guid represents the id of the disk that holds the memory dump
     */
    Guid getMemoryDiskId();

    /**
     * Return the id of the disk that holds the VM configuration
     * @return Guid represents the id of the disk that holds the VM configuration
     */
    Guid getMetadataDiskId();
}
