package org.ovirt.engine.core.utils.ovf;

public interface IOvfBuilder {
    // Namespace URIs:
    public final String OVF_URI = "http://schemas.dmtf.org/ovf/envelope/1/";
    public final String RASD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData";
    public final String VSSD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData";
    public final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    // Namespace prefixes:
    public final String OVF_PREFIX = "ovf";
    public final String RASD_PREFIX = "rasd";
    public final String VSSD_PREFIX = "vssd";
    public final String XSI_PREFIX = "xsi";

    void buildReference();

    void buildNetwork();

    void buildDisk();

    void buildVirtualSystem();

    String getStringRepresentation();
}
