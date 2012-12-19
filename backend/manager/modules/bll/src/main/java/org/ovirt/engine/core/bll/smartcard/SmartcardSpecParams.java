package org.ovirt.engine.core.bll.smartcard;

import java.util.HashMap;

public class SmartcardSpecParams extends HashMap<String, Object> {

    private static final long serialVersionUID = 4548406604400880935L;

    public SmartcardSpecParams() {
        put("mode", "passthrough");
        put("type", "spicevmc");
    }

}
