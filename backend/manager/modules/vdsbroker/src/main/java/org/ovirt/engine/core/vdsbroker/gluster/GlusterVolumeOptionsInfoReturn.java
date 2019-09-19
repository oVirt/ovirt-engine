package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class GlusterVolumeOptionsInfoReturn extends StatusReturn {
    private static final String VOLUME_OPTIONS_DEFAULT = "volumeSetOptions";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("volumeOptionsDefaults")]
    public Set<GlusterVolumeOptionInfo> optionsHelpSet = new TreeSet<>();

    @SuppressWarnings("unchecked")
    public GlusterVolumeOptionsInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] options = (Object[]) innerMap.get(VOLUME_OPTIONS_DEFAULT);
        if (options != null) {
            for (Object option : options) {
                optionsHelpSet.add(prepareOptionHelpEntity((Map<String, Object>) option));
            }
        }
    }

    private GlusterVolumeOptionInfo prepareOptionHelpEntity(Map<String, Object> map) {
        GlusterVolumeOptionInfo entity = new GlusterVolumeOptionInfo();
        entity.setKey(map.get("name").toString());
        entity.setDefaultValue(map.get("defaultValue").toString());
        entity.setDescription(map.get("description").toString());
        return entity;
    }
}
