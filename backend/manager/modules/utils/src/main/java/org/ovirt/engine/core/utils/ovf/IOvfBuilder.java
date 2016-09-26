package org.ovirt.engine.core.utils.ovf;

public interface IOvfBuilder {
    // Namespace URIs:
    String OVF_URI = "http://schemas.dmtf.org/ovf/envelope/1/";
    String RASD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData";
    String VSSD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData";
    String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    // Namespace prefixes:
    String OVF_PREFIX = "ovf";
    String RASD_PREFIX = "rasd";
    String VSSD_PREFIX = "vssd";
    String XSI_PREFIX = "xsi";

    void buildReference();

    void buildNetwork();

    void buildDisk();

    void buildVirtualSystem();

    String getStringRepresentation();

    default IOvfBuilder build() {
        buildReference();
        buildNetwork();
        buildDisk();
        buildVirtualSystem();
        return this;
    }
}
