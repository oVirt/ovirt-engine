package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class GlusterHooksListReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String HOOK_STATUS = "status";
    private static final String CONTENT_TYPE = "mimetype";
    private static final String CHECKSUM = "md5sum";
    private static final String LEVEL = "level";
    private static final String COMMAND = "command";
    private static final String NAME = "name";
    private static final String HOOKS_LIST = "hooksList";

    private List<GlusterHookEntity> hooks;

    @SuppressWarnings("unchecked")
    public GlusterHooksListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (getXmlRpcStatus().code != 0) {
            return;
        }

        Object[] hooksArr = (Object[]) innerMap.get(HOOKS_LIST);
        hooks = new ArrayList<>();

        if (hooksArr != null) {
            for (Object hookMap : hooksArr) {
                hooks.add(getHook((Map<String, Object>)hookMap));
            }
        }
    }

    private GlusterHookEntity getHook(Map<String, Object> map) {
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setName(map.get(NAME).toString());
        hook.setGlusterCommand(map.get(COMMAND).toString());
        hook.setStage(map.get(LEVEL).toString());
        hook.setChecksum(map.get(CHECKSUM).toString());
        hook.setContentType(GlusterHookContentType.fromMimeType(map.get(CONTENT_TYPE).toString()));
        hook.setStatus(map.get(HOOK_STATUS).toString());
        return hook;
    }

    public List<GlusterHookEntity> getGlusterHooks() {
        return hooks;
    }

}
