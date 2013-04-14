package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.SerializationExeption;

/**
 * {@link Deserializer} implementation for deserializing JSON content.
 */
public class JsonObjectDeserializer implements Deserializer {

    private static final ObjectMapper formattedMapper;
    static {
        formattedMapper = new ObjectMapper();
        formattedMapper.getDeserializationConfig().addMixInAnnotations(NGuid.class, JsonNGuidMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(Guid.class, JsonNGuidMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VdcActionParametersBase.class,
                JsonVdcActionParametersBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(IVdcQueryable.class,
                JsonIVdcQueryableMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VM.class, JsonVmMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(ValueObjectMap.class,
                JsonValueObjectMapMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VdsStatic.class, JsonVdsStaticMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VDS.class, JsonVDSMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(AddVmTemplateParameters.class,
                JsonAddVmTemplateParametersMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmManagementParametersBase.class,
                JsonVmManagementParametersBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmBase.class, JsonVmBaseMixIn.class);
        formattedMapper.getDeserializationConfig().addMixInAnnotations(VmStatic.class, JsonVmStaticMixIn.class);
        formattedMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        formattedMapper.enableDefaultTyping();
    }

    @Override
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationExeption {
        if (source == null) {
            return null;
        }
        return readJsonString(source, type, formattedMapper);
    }

    private <T> T readJsonString(Object source, Class<T> type, ObjectMapper mapper) {
        try {
            return mapper.readValue(source.toString(), type);
        } catch (JsonParseException e) {
            throw new SerializationException(e);
        } catch (JsonMappingException e) {
            throw new SerializationException(e);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
