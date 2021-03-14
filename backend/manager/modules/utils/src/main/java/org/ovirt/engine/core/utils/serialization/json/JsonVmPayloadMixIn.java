package org.ovirt.engine.core.utils.serialization.json;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmPayload;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
public abstract class JsonVmPayloadMixIn extends VmPayload {

    @JsonIgnore
    @Override
    public Map<String, Object> getSpecParams() {
        return Collections.emptyMap();
    }
}
