package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class GlusterHookContentInfoReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String CONTENT = "content";

    private final String hookContent;

    public String getHookcontent() {
        return hookContent;
    }

    public GlusterHookContentInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        hookContent = (String) innerMap.get(CONTENT);
    }

}
