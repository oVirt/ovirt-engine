/**
 *
 */
package org.ovirt.engine.core.utils.serialization.json;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationException;
import org.ovirt.engine.core.utils.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

/**
 * {@link Serializer} implementation for deserializing JSON content.
 */
public class JsonObjectSerializer implements Serializer {

    private static final Logger log = LoggerFactory.getLogger(JsonObjectSerializer.class);
    private static final ObjectMapper unformattedMapper = new ObjectMapper();
    private static final ObjectMapper formattedMapper;
    static {
        formattedMapper = new ObjectMapper();
        formattedMapper.addMixIn(Guid.class, JsonGuidMixIn.class);
        formattedMapper.addMixIn(ActionParametersBase.class,
                JsonActionParametersBaseMixIn.class);
        formattedMapper.addMixIn(Queryable.class, JsonQueryableMixIn.class);
        formattedMapper.addMixIn(VM.class, JsonVmMixIn.class);
        formattedMapper.addMixIn(AddVmTemplateParameters.class,
                JsonAddVmTemplateParametersMixIn.class);
        formattedMapper.addMixIn(VmManagementParametersBase.class,
                JsonVmManagementParametersBaseMixIn.class);
        formattedMapper.addMixIn(VmBase.class, JsonVmBaseMixIn.class);
        formattedMapper.addMixIn(VmStatic.class, JsonVmStaticMixIn.class);
        formattedMapper.addMixIn(VmPayload.class, JsonVmPayloadMixIn.class);
        formattedMapper.addMixIn(RunVmParams.class, JsonRunVmParamsMixIn.class);
        formattedMapper.addMixIn(EngineFault.class, JsonEngineFaultMixIn.class);
        formattedMapper.addMixIn(Collection.class, JsonCollectionMixIn.class);
        formattedMapper.addMixIn(Map.class, JsonMapMixIn.class);
        formattedMapper.addMixIn(Cluster.class, JsonClusterMixIn.class);
        formattedMapper.addMixIn(VdsDynamic.class, JsonVdsDynamicMixIn.class);
        formattedMapper.addMixIn(DestroyImageParameters.class, JsonDestroyImageParametersMixIn.class);
        formattedMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance);
        formattedMapper.configure(INDENT_OUTPUT, true);
    }

    @Override
    public String serialize(Object payload) throws SerializationException {
        if (payload == null) {
            return null;
        } else {
            return writeJsonAsString(payload, formattedMapper);
        }
    }

    /**
     * Use the ObjectMapper to parse the payload to String.
     *
     * @param payload
     *            - The payload to be returned.
     * @param mapper
     *            - The ObjectMapper.
     * @return Parsed string of the serialized object.
     */
    private String writeJsonAsString(Object payload, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (IOException e) {
            log.error("Cannot serialize {} because {}", payload, ExceptionUtils.getRootCauseMessage(e));
            log.debug("Cannot serialize {}. Details {}", payload, ExceptionUtils.getFullStackTrace(e));
            throw new SerializationException(e);
        }
    }

    /**
     * Parse the serialized content with unformatted Json.
     *
     * @param payload
     *            - The serialized Object.
     * @return The string value of the serialized object.
     */
    public String serializeUnformattedJson(Serializable payload) throws SerializationException {
        return writeJsonAsString(payload, unformattedMapper);
    }

    public String serializeUnformattedJson(JsonNode payload) throws SerializationException {
        return writeJsonAsString(payload, unformattedMapper);
    }
}
