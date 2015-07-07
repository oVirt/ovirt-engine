package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.ovirt.engine.core.common.action.VdcActionType;

public class VdcActionTypeDeserializer extends JsonDeserializer<VdcActionType> {
    private static HashMap<String, VdcActionType> mappings = new HashMap<>();

    static {
        for (VdcActionType action : VdcActionType.values()) {
            mappings.put(action.name(), action);
        }
    }

    @Override
    public VdcActionType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String jsonValue = jsonParser.getText();
        VdcActionType actionType = mappings.get(jsonValue);
        if (actionType == null) {
            actionType = VdcActionType.Unknown;
        }
        return actionType;
    }
}
