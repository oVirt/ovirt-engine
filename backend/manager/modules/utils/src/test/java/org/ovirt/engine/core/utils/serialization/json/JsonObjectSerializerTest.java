package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.SerializationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;


/**
 * Tests for {@link JsonObjectSerializer}.
 */
public class JsonObjectSerializerTest {

    @Test
    public void testSerialize() {
        JsonSerializablePojo serializablePojo = new JsonSerializablePojo();

        assertEquals(serializablePojo.toJsonForm(true),
                new JsonObjectSerializer().serialize(serializablePojo).replaceAll("\\s", ""));
    }

    @Test
    public void serializeNetwork() {
        Network net = new Network();
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(net).length() > 0);
    }

    @Test
    public void serializeVdsNetworkInterface() {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(nic).length() > 0);
    }

    @Test
    public void serializeVdsActionParameters() {
        ActionParametersBase params = new ActionParametersBase();
        params.setLockProperties(LockProperties.create(Scope.None).withWait(true));
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(params).length() > 0);
    }

    @Test
    public void serializeParametersMap() {
        Map<String, Serializable> data = new HashMap<>();
        data.put("NEXT_COMMAND_TYPE", ActionType.DestroyImage);
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(data).length() > 0);
    }

    @Test
    public void serializeCreateSnapshotForVmParametersMap() {
        Map<Guid, Guid> diskToImageIds = new HashMap<>();
        diskToImageIds.put(Guid.newGuid(), Guid.newGuid());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                new TreeSet<>(diskToImageIds.keySet()));
        params.setDiskToImageIds(diskToImageIds);

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        CreateSnapshotForVmParameters deserializedParams =
                deserializer.deserialize(json, CreateSnapshotForVmParameters.class);
        assertEquals(params.getDiskIds(), deserializedParams.getDiskIds());
    }

    @Test
    public void serializeCreateSnapshotForVmParametersMapFailure() {
        Map<Guid, Guid> diskToImageIds = new HashMap<>();
        diskToImageIds.put(Guid.newGuid(), Guid.newGuid());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                diskToImageIds.keySet());
        params.setDiskToImageIds(diskToImageIds);

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        assertThrows(SerializationException.class, () -> deserializer.deserialize(json, CreateSnapshotForVmParameters.class));
    }

    @Test
    public void objectMapperSerializeCreateSnapshotForVmParametersMapFailure() {
        Map<Guid, Guid> diskToImageIds = new HashMap<>();
        diskToImageIds.put(Guid.newGuid(), Guid.newGuid());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                diskToImageIds.keySet());
        params.setDiskToImageIds(diskToImageIds);

        try {
            String json = serialize(params);
            assertTrue(json.length() > 0);
            assertThrows(JsonMappingException.class, () -> deserialize(json, CreateSnapshotForVmParameters.class));
        } catch (Exception ex) {
            // ignore
        }

    }

    @Test
    public void serializeDestroyImageParameters() {
        List<Guid> guids = new ArrayList<>(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        DestroyImageParameters destroyImageParameters = new DestroyImageParameters(Guid.newGuid(),
                Guid.newGuid(),
                Guid.newGuid(),
                Guid.newGuid(),
                Guid.newGuid(),
                guids,
                true,
                true);
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(destroyImageParameters);
        assertTrue(json.length() > 0);
    }

    private String serialize(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getSerializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.writeValueAsString(obj);
    }

    private <T> T deserialize(String json, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.readValue(json, type);
    }
}
