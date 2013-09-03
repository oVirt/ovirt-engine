package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.VmPayload;

import java.util.Collections;
import java.util.Map;

public abstract class JsonVmPayloadMixIn extends VmPayload {

    @JsonIgnore
    @Override
    public Map<String, Object> getSpecParams() {
        return Collections.emptyMap();
    }
}
