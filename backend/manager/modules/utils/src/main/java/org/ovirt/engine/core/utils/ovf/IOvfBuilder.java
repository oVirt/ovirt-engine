package org.ovirt.engine.core.utils.ovf;

public interface IOvfBuilder {
    // Namespace URIs:
    public static final String OVF_URI = "http://schemas.dmtf.org/ovf/envelope/1/";
    public static final String RASD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData";
    public static final String VSSD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData";
    public static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    // Namespace prefixes:
    public static final String OVF_PREFIX = "ovf";
    public static final String RASD_PREFIX = "rasd";
    public static final String VSSD_PREFIX = "vssd";
    public static final String XSI_PREFIX = "xsi";

    void buildReference();

    void buildNetwork();

    void buildDisk();

    void buildVirtualSystem();

    String getStringRepresentation();
}
