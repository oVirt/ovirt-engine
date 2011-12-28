package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsFencingOptions;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.Deserializer;
import org.ovirt.engine.core.utils.SerializationExeption;

/**
 * {@link Deserializer} implementation for deserializing JSON content.
 */
public class JsonObjectDeserializer implements Deserializer {

    @Override
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationExeption {
        if (source == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().addMixInAnnotations(NGuid.class, JsonNGuidMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Guid.class, JsonNGuidMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VdcActionParametersBase.class, JsonVdcActionParametersBaseMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(IVdcQueryable.class, JsonIVdcQueryableMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VM.class, JsonVmMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ValueObjectMap.class, JsonValueObjectMapMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VdsStatic.class, JsonVdsStaticMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VdsFencingOptions.class, JsonVdsFencingOptionsMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VDS.class, JsonVDSMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(VmTemplate.class, JsonVmTemplateMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(AddVmFromTemplateParameters.class, JsonAddVmFromTemplateParametersMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(MoveOrCopyParameters.class, JsonMoveOrCopyParametersMixIn.class);
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping();
        return readJsonString(source, type, mapper);
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

    /**
     * Deserialize unformatted Json content.
     *
     * @param source
     *            - The object which supposed to be deserialize.
     * @return The serialized object.
     * @throws SerializationExeption
     */
    public <T extends Serializable> T deserializeUnformattedJson(Object source, Class<T> type) throws SerializationExeption {
        ObjectMapper mapper = new ObjectMapper();
        return readJsonString(source, type, mapper);
    }
}
