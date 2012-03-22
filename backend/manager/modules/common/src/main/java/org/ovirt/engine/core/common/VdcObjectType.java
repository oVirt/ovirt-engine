package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcObjectType")
public enum VdcObjectType {
    Unknown(-1, "Unknown"),
    System(1, "System"),
    VM(2, "VM"),
    VDS(3, "Host"),
    VmTemplate(4, "Template"),
    VmPool(5, "VM Pool"),
    AdElements(6, "AdElements"),
    Tags(7, "Tag"),
    Bookmarks(8, "Bookmark"),
    VdsGroups(9, "Cluster"),
    MultiLevelAdministration(10, "MultiLevelAdministration"),
    Storage(11, "Storage"),
    EventNotification(12, "EventNotification"),
    ImportExport(13, "ImportExport"),
    StoragePool(14, "Data Center"),
    User(15, "User"),
    Role(16, "Role"),
    Quota(17, "Quota"),
    GlusterVolume(18, "Gluster Volume"),
    Disk(19, "Disk");

    private int value;
    private String vdcObjectTranslationVal;
    private static final Map<Integer, VdcObjectType> map = new HashMap<Integer, VdcObjectType>(values().length);

    static {
        for (VdcObjectType type : values()) {
            map.put(type.getValue(), type);
        }
    }

    private VdcObjectType(int val, String vdcObjectTranslationVal) {
        this.value = val;
        this.vdcObjectTranslationVal = vdcObjectTranslationVal;
    }

    private VdcObjectType(int val) {
        this(val,null);
    }

    @XmlElement(name = "VdcObjectTranslation")
    public String getVdcObjectTranslation() {
        return vdcObjectTranslationVal;
    }

    public int getValue() {
        return this.value;
    }

    public static VdcObjectType forValue(int value) {
        return map.get(value);
    }
}
