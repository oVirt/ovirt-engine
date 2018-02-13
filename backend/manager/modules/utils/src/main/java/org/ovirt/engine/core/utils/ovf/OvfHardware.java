package org.ovirt.engine.core.utils.ovf;

public final class OvfHardware {
    public static final String OTHER = "0";
    public static final String CPU = "3";
    public static final String Memory = "4";
    public static final String Network = "10";
    public static final String CD = "15";
    public static final String DiskImage = "17";
    /** The OVF specification does not include Monitor devices,
     *  so we use a constant that is supposed to be unused */
    public static final String Monitor = "32768";
    /** We must keep using '20' for oVirt's OVFs for backward/forward compatibility */
    public static final String OVIRT_Monitor = "20";
    public static final String USB = "23";
    public static final String Graphics = "24";
    /** In the OVF specification 24 should be used for Graphic devices, but we
     *  must keep using '26' for oVirt's OVFs for backward/forward compatibility */
    public static final String OVIRT_Graphics = "26";
}
