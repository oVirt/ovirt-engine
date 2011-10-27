package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlType(name = "DiskImageList")
@XmlAccessorType(XmlAccessType.NONE)
public class DiskImageList {
    private DiskImage[] diskImages;
    private String csharpworkaround; // without this, C# wsdl processing will
                                     // auto-convert this class to [] and
                                     // then fail

    public DiskImageList() {
    }

    public DiskImageList(List<DiskImage> diskImanages) {
        this.diskImages = diskImanages.toArray(new DiskImage[diskImanages.size()]);
    }

    @XmlElement
    public DiskImage[] getDiskImages() {
        return diskImages;
    }

    @XmlElement
    public String getCsharpworkaround() {
        return csharpworkaround;
    }

    public void setCsharpworkaround(String csharpworkaround) {
        this.csharpworkaround = csharpworkaround;
    }
}
