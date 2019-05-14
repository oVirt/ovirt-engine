package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterHooksListReturn extends StatusReturn {

    private static final String HOOK_STATUS = "status";
    private static final String CONTENT_TYPE = "mimetype";
    private static final String CHECKSUM = "checksum";
    private static final String OLD_CHECKSUM = "md5Sum";
    private static final String LEVEL = "level";
    private static final String COMMAND = "command";
    private static final String NAME = "name";
    private static final String HOOKS_LIST = "hooksList";

    private List<GlusterHookEntity> hooks;

    @SuppressWarnings("unchecked")
    public GlusterHooksListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (getStatus().code != 0) {
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
        String checksum;
        if (map.containsKey(CHECKSUM)) {
            checksum = map.get(CHECKSUM).toString();
        } else {
            //changed the computation from md5sum in 4.3
            checksum = map.get(OLD_CHECKSUM).toString();
        }
        hook.setChecksum(checksum);
        hook.setContentType(GlusterHookContentType.fromMimeType(map.get(CONTENT_TYPE).toString()));
        hook.setStatus(map.get(HOOK_STATUS).toString());
        return hook;
    }

    public List<GlusterHookEntity> getGlusterHooks() {
        return hooks;
    }

}
