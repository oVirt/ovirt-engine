package org.ovirt.engine.core.utils.serialization.json;

import org.ovirt.engine.api.extensions.ExtKey;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class JsonExtKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String s, DeserializationContext deserializationContext) {
        String[] tokens = s.split(";");
        try {
            return new ExtKey(tokens[0], Class.forName(tokens[1]), tokens[2]);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
