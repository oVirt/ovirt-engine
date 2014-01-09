package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVdcUserMixIn extends VdcUser {

    @JsonSerialize(using = JsonPasswordSerializer.class)
    @Override
    public abstract String getPassword();

    @JsonDeserialize(using = JsonPasswordDeserializer.class)
    @Override
    public abstract void setPassword(String value);

    public static class JsonPasswordSerializer extends JsonSerializer<String> {

        public JsonPasswordSerializer() {}

        @Override
        public void serialize(String passwd, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            try {
                jsonGenerator.writeString(EngineEncryptionUtils.encrypt(passwd));
            } catch(GeneralSecurityException gse) {
                throw new IOException(gse);
            }
        }
    }

    public static class JsonPasswordDeserializer extends JsonDeserializer<String> {

        public JsonPasswordDeserializer() {}

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            try {
                return  EngineEncryptionUtils.decrypt(jsonParser.getText());
            } catch(GeneralSecurityException gse) {
                throw new IOException(gse);
            }
        }
    }
}
