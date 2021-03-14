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
import java.util.stream.Stream;

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
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.SerializationException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Tests for {@link JsonObjectSerializer}.
 */
public class JsonObjectSerializerTest {

     public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
         return Stream.of(
                  MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
         );
     }


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
        params.setLockProperties(LockProperties.create(Scope.None).withWaitForever());
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(params).length() > 0);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void serializeParametersMap() {
        Map<String, Serializable> data = new HashMap<>();
        data.put("NEXT_COMMAND_TYPE", ActionType.DestroyImage);
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        assertTrue(serializer.serialize(data).length() > 0);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void serializeCreateSnapshotForVmParametersMap() {
        Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
        diskImagesMap.put(Guid.newGuid(), new DiskImage());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                new TreeSet<>(diskImagesMap.keySet()));
        params.setDiskImagesMap(diskImagesMap);

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        CreateSnapshotForVmParameters deserializedParams =
                deserializer.deserialize(json, CreateSnapshotForVmParameters.class);
        assertEquals(params.getDiskIds(), deserializedParams.getDiskIds());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void serializeCreateSnapshotForVmParametersMapFailure() {
        Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
        diskImagesMap.put(Guid.newGuid(), new DiskImage());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                diskImagesMap.keySet());
        params.setDiskImagesMap(diskImagesMap);

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        JsonObjectDeserializer deserializer = new JsonObjectDeserializer();
        assertThrows(SerializationException.class, () -> deserializer.deserialize(json, CreateSnapshotForVmParameters.class));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void objectMapperSerializeCreateSnapshotForVmParametersMapFailure() {
        Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
        diskImagesMap.put(Guid.newGuid(), new DiskImage());
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(
                Guid.newGuid(),
                "Test",
                false,
                diskImagesMap.keySet());
        params.setDiskImagesMap(diskImagesMap);

        try {
            String json = serialize(params);
            assertTrue(json.length() > 0);
            assertThrows(JsonMappingException.class, () -> deserialize(json, CreateSnapshotForVmParameters.class));
        } catch (Exception ex) {
            // ignore
        }

    }

    @Test
    @MockedConfig("mockConfiguration")
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
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator())
                .addMixIn(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.writeValueAsString(obj);
    }

    private <T> T deserialize(String json, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator())
                .addMixIn(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.readValue(json, type);
    }
}
