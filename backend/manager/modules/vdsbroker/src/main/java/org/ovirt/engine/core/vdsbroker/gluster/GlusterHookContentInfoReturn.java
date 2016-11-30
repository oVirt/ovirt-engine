package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class GlusterHookContentInfoReturn extends StatusReturn {

    private static final String CONTENT = "content";

    private final String hookContent;

    public String getHookcontent() {
        return hookContent;
    }

    public GlusterHookContentInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        hookContent = (String) innerMap.get(CONTENT);
    }

}
