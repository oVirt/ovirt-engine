package org.ovirt.engine.core.sso.utils;

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.KeyDeserializer;
import org.ovirt.engine.api.extensions.ExtKey;

public class JsonExtKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String s, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        String[] tokens = s.split(";");
        try {
            return new ExtKey(tokens[0], Class.forName(tokens[1]), tokens[2]);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
