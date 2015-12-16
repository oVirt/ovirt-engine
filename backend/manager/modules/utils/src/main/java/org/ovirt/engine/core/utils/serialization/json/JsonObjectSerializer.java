/**
 *
 */
package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationExeption;
import org.ovirt.engine.core.utils.Serializer;

/**
 * {@link Serializer} implementation for deserializing JSON content.
 */
public class JsonObjectSerializer implements Serializer {

    private static final ObjectMapper unformattedMapper = new ObjectMapper();
    private static final ObjectMapper formattedMapper;
    static {
        formattedMapper = new ObjectMapper();
        formattedMapper.getSerializationConfig().addMixInAnnotations(Guid.class, JsonGuidMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VdcActionParametersBase.class,
                JsonVdcActionParametersBaseMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(IVdcQueryable.class, JsonIVdcQueryableMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VM.class, JsonVmMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(AddVmTemplateParameters.class,
                JsonAddVmTemplateParametersMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VmManagementParametersBase.class,
                JsonVmManagementParametersBaseMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VmBase.class, JsonVmBaseMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VmStatic.class, JsonVmStaticMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(VmPayload.class, JsonVmPayloadMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(RunVmParams.class, JsonRunVmParamsMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(EngineFault.class, JsonEngineFaultMixIn.class);
        formattedMapper.getSerializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);

        formattedMapper.configure(Feature.INDENT_OUTPUT, true);
        formattedMapper.enableDefaultTyping();
    }

    @Override
    public String serialize(Object payload) throws SerializationExeption {
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
    public String serializeUnformattedJson(Serializable payload) throws SerializationExeption {
        return writeJsonAsString(payload, unformattedMapper);
    }
}
