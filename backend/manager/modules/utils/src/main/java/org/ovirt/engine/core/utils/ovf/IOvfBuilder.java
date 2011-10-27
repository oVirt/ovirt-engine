package org.ovirt.engine.core.utils.ovf;

public interface IOvfBuilder {
    public final String OVF_URI = "http://schemas.dmtf.org/ovf/envelope/1/";
    public final String RASD_URI =
            "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData";
    public final String VSSD_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData";
    public final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    void BuildReference();

    void BuildNetwork();

    void BuildDisk();

    void BuildVirtualSystem();
}
