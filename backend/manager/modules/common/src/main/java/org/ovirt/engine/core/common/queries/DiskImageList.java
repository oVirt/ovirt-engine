package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.DiskImage;

public class DiskImageList implements Serializable {
    private DiskImage[] diskImages;
    private String csharpworkaround; // without this, C# wsdl processing will
                                     // auto-convert this class to [] and
                                     // then fail

    public DiskImageList() {
    }

    public DiskImageList(List<DiskImage> diskImanages) {
        this.diskImages = diskImanages.toArray(new DiskImage[diskImanages.size()]);
    }

    @JsonIgnore
    public DiskImage[] getDiskImages() {
        return diskImages;
    }

    @JsonIgnore
    public String getCsharpworkaround() {
        return csharpworkaround;
    }

    public void setCsharpworkaround(String csharpworkaround) {
        this.csharpworkaround = csharpworkaround;
    }
}
