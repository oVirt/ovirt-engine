package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.DiskImage;

public class DiskImageList implements Serializable {
    private DiskImage[] diskImages;

    public DiskImageList() {
    }

    public DiskImageList(List<DiskImage> diskImanages) {
        this.diskImages = diskImanages.toArray(new DiskImage[diskImanages.size()]);
    }

    @JsonIgnore
    public DiskImage[] getDiskImages() {
        return diskImages;
    }

}
