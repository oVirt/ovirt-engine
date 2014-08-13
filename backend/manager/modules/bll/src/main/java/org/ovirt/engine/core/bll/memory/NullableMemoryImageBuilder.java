package org.ovirt.engine.core.bll.memory;

import org.apache.commons.lang.StringUtils;

/**
 * This builder is used when no memory image should be created
 */
public class NullableMemoryImageBuilder implements MemoryImageBuilder {

    public void build() {
        //no op
    }

    public String getVolumeStringRepresentation() {
        return StringUtils.EMPTY;
    }

    public boolean isCreateTasks() {
        return false;
    }
}
