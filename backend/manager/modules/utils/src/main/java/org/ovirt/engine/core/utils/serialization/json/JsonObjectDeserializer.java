package org.ovirt.engine.core.utils.serialization.json;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.SerializationException;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * {@link Deserializer} implementation for deserializing JSON content.
 */
public class JsonObjectDeserializer implements Deserializer {
    private static final Logger log = LoggerFactory.getLogger(JsonObjectDeserializer.class);

    private static final ObjectMapper unformattedMapper = new ObjectMapper();
    private static final ObjectMapper formattedMapper;
    static {
        formattedMapper = new ObjectMapper();
        formattedMapper.addMixIn(Guid.class, JsonGuidMixIn.class);
        formattedMapper.addMixIn(ActionParametersBase.class,
                JsonActionParametersBaseMixIn.class);
        formattedMapper.addMixIn(Queryable.class,
                JsonQueryableMixIn.class);
        formattedMapper.addMixIn(VM.class, JsonVmMixIn.class);
        formattedMapper.addMixIn(AddVmTemplateParameters.class,
                JsonAddVmTemplateParametersMixIn.class);
        formattedMapper.addMixIn(VmManagementParametersBase.class,
                JsonVmManagementParametersBaseMixIn.class);
        formattedMapper.addMixIn(VmBase.class, JsonVmBaseMixIn.class);
        formattedMapper.addMixIn(VmStatic.class, JsonVmStaticMixIn.class);
        formattedMapper.addMixIn(RunVmParams.class, JsonRunVmParamsMixIn.class);
        formattedMapper.addMixIn(EngineFault.class, JsonEngineFaultMixIn.class);
        formattedMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        formattedMapper.configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        formattedMapper.configure(READ_ENUMS_USING_TO_STRING, true);

        formattedMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance);
    }

    @Override
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationException {
        if (source == null) {
            return null;
        }
        return readJsonString(source, type, formattedMapper);
    }

    /**
     * Converts JSON string to instance of specified class. If {@code value} is {@code null} or empty, tries to create
     * new instance of specified class. If it fails returns {@code null}
     *
     * @param value
     *            JSON string
     * @param clazz
     *            specified class
     * @return new instance or {@code null} if a new instance cannot be created
     */
    public <T extends Serializable> T deserializeOrCreateNew(String value, Class<T> clazz) {
        if (StringUtils.isEmpty(value)) {
            T instance;
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                instance = null;
            }
            return instance;
        } else {
            return SerializationFactory.getDeserializer().deserialize(value, clazz);
        }
    }


    /**
     * Deserialize unformatted Json content.
     *
     * @param source - The object which supposed to be deserialize.
     * @return The serialized object.
     */
    public <T extends Serializable> T deserializeUnformattedJson(Object source, Class<T> type) throws SerializationException {
        return readJsonString(source, type, unformattedMapper);
    }

    public <T extends Serializable> List<T> deserializeUnformattedList(String source, Class<T> contentType) {
        try {
            CollectionType type = unformattedMapper.getTypeFactory().constructCollectionType(List.class, contentType);
            return unformattedMapper.readValue(source, type);
        } catch (IOException e) {
            log.error("Cannot deserialize unformatted list {} because of {}",
                    source,
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Cannot deserialize unformatted list {}. Details {}",
                    source,
                    ExceptionUtils.getFullStackTrace(e));
            throw new SerializationException(e);
        }
    }

    private <T> T readJsonString(Object source, Class<T> type, ObjectMapper mapper) {
        try {
            return mapper.readValue(source.toString(), type);
        } catch (IOException e) {
            log.error("Cannot deserialize {} because of {}", source, ExceptionUtils.getRootCauseMessage(e));
            log.debug("Cannot deserialize {}. Details {}", source, ExceptionUtils.getFullStackTrace(e));
            throw new SerializationException(e);
        }
    }
}
