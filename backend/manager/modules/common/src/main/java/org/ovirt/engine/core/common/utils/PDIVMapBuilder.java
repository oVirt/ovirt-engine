package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class PDIVMapBuilder {

    private Guid poolId;
    private Guid domainId;
    private Guid imageGroupId;
    private Guid volumeId;

    private PDIVMapBuilder() {
    }

    public static PDIVMapBuilder create() {
        return new PDIVMapBuilder();
    }

    public Map<String, String> build() {
        if (poolId == null || domainId == null || imageGroupId == null || volumeId== null) {
            throw new IllegalArgumentException("One or more of the PDIV IDs is null");
        }
        Map<String, String> pdivMap = new HashMap<>(8);
        pdivMap.put("poolID", poolId.toString());
        pdivMap.put("domainID", domainId.toString());
        pdivMap.put("imageID", imageGroupId.toString());
        pdivMap.put("volumeID", volumeId.toString());
        return pdivMap;
    }

    public PDIVMapBuilder setPoolId(Guid poolId) {
        this.poolId = poolId;
        return this;
    }

    public PDIVMapBuilder setDomainId(Guid domainId) {
        this.domainId = domainId;
        return this;
    }

    public PDIVMapBuilder setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
        return this;
    }

    public PDIVMapBuilder setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
        return this;
    }
}
