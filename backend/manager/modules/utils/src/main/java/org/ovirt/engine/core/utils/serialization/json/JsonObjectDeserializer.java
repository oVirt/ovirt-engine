package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.SerializationExeption;
import org.ovirt.engine.core.utils.SerializationFactory;

/**
 * {@link Deserializer} implementation for deserializing JSON content.
 */
public class JsonObjectDeserializer implements Deserializer {

    private static final ObjectMapper unformattedMapper = new ObjectMapper();
    private static final ObjectMapper formattedMapper;
    static {
        formattedMapper = new ObjectMapper();
        formattedMapper.getDeserializationConfig().addMixInAnnotations(Guid.class, JsonGuidMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VdcActionParametersBase.class,
                JsonVdcActionParametersBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(IVdcQueryable.class,
                JsonIVdcQueryableMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VM.class, JsonVmMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(AddVmTemplateParameters.class,
                JsonAddVmTemplateParametersMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmManagementParametersBase.class,
                JsonVmManagementParametersBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmBase.class, JsonVmBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmStatic.class, JsonVmStaticMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(RunVmParams.class, JsonRunVmParamsMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(EngineFault.class, JsonEngineFaultMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        formattedMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        formattedMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        formattedMapper.enableDefaultTyping();
        formattedMapper.setDeserializerProvider(new JsonObjectDeserializerProvider());
    }

    @Override
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationExeption {
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
                instance = clazz.newInstance();
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
    public <T extends Serializable> T deserializeUnformattedJson(Object source, Class<T> type) throws SerializationExeption {
        return readJsonString(source, type, unformattedMapper);
    }


    private <T> T readJsonString(Object source, Class<T> type, ObjectMapper mapper) {
        try {
            return mapper.readValue(source.toString(), type);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
