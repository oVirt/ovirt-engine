package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.Map;

public enum VdcObjectType {
    Unknown(-1, "Unknown"),
    // For internal use only. Used to mark the host used for execution of a Step.
    EXECUTION_HOST(-100, "Execution Host"),
    // bottom is an object which all the objects in the system are its parents
    // useful to denote we want all objects when checking for permissions
    Bottom(0, "Bottom"),
    System(1, "System"),
    VM(2, "VM"),
    VDS(3, "Host"),
    VmTemplate(4, "Template"),
    VmPool(5, "VM Pool"),
    AdElements(6, "AdElements"),
    Tags(7, "Tag"),
    Bookmarks(8, "Bookmark"),
    Cluster(9, "Cluster"),
    MultiLevelAdministration(10, "MultiLevelAdministration"),
    Storage(11, "Storage"),
    EventNotification(12, "EventNotification"),
    ImportExport(13, "ImportExport"),
    StoragePool(14, "Data Center"),
    User(15, "User"),
    Role(16, "Role"),
    Quota(17, "Quota"),
    GlusterVolume(18, "Gluster Volume"),
    Disk(19, "Disk"),
    Network(20, "Network"),
    Snapshot(21, "Snapshot"),
    Event(22, "Event"),
    GlusterHook(23, "GlusterHook"),
    PROVIDER(24, "Provider"),
    GlusterService(25, "GlusterService"),
    ExternalTask(26, "ExternalTask"),
    VnicProfile(27, "Vnic Profile"),
    MacPool(28, "MAC Pool"),
    DiskProfile(29, "Disk Profile"),
    CpuProfile(30, "Cpu Profile");


    private int value;
    private String vdcObjectTranslationVal;
    private static final Map<Integer, VdcObjectType> map = new HashMap<>(values().length);
    private static final int INTERNAL_ENTITY_VALUE = -100;

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
        this(val, null);
    }

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
