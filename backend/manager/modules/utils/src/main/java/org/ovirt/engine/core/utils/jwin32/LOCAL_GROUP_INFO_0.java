package org.ovirt.engine.core.utils.jwin32;


import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.Structure;


public class LOCAL_GROUP_INFO_0 extends NetStruct {
    public static class ByReference extends LOCAL_GROUP_INFO_0 implements Structure.ByReference {
    }

    public LOCAL_GROUP_INFO_0() {
        super();
    }

    public LOCAL_GROUP_INFO_0(Pointer memory) {
        useMemory(memory);
        read();
    }

    public WString lgrpi0_name;

    public String getSID() throws ConvertSidException {
        return (getSID(lgrpi0_name));
    }
}
