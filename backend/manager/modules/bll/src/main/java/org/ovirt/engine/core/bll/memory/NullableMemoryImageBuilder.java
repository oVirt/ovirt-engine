package org.ovirt.engine.core.bll.memory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;

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

    public List<DiskImage> getDisksToBeCreated() {
        return Collections.emptyList();
    }
}
