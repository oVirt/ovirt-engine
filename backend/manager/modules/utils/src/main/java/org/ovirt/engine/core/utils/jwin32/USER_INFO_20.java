package org.ovirt.engine.core.utils.jwin32;

import com.sun.jna.WString;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;


public class USER_INFO_20 extends NetStruct {
    public static class ByReference extends USER_INFO_20 implements Structure.ByReference {
    }

    public WString usri20_name;
    public WString usri20_full_name;
    public WString usri20_comment;
    public int usri20_flags;
    public int usri20_user_id;

    public USER_INFO_20() {
        super();
    }

    public USER_INFO_20(Pointer memory) {
        useMemory(memory);
        read();
    }

    public String getSID() throws ConvertSidException {
        return (getSID(usri20_name));
    }
}
