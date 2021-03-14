package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * Tests for {@link JsonObjectSerializer} and {@link JsonObjectDeserializer} with unmodifiable and immutable
 * collections.
 */
public class JsonImmutableSerializeDeserializeTest {

    @Test
    public void serializeCollectionsSingleton() {
        ManageNetworkClustersParameters params = new ManageNetworkClustersParameters(
                Collections.singleton(new NetworkCluster(
                        Guid.createGuidFromString("f455686a-79cc-11e6-8c65-54ee755c6cc7"),
                        Guid.createGuidFromString("f970c5f6-79cc-11e6-bc8f-54ee755c6cc7"),
                        NetworkStatus.NON_OPERATIONAL,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false)));

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        ManageNetworkClustersParameters deserializedParams = new JsonObjectDeserializer().deserialize(
                json,
                ManageNetworkClustersParameters.class);

        assertEquals(params.getExecutionReason(), deserializedParams.getExecutionReason());
    }

    @Test
    public void serializeCollectionsSingletonList() {
        ManageNetworkClustersParameters params = new ManageNetworkClustersParameters(
                Collections.singletonList(new NetworkCluster(
                        Guid.newGuid(),
                        Guid.newGuid(),
                        NetworkStatus.NON_OPERATIONAL,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false)));

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        ManageNetworkClustersParameters deserializedParams = new JsonObjectDeserializer().deserialize(
                json,
                ManageNetworkClustersParameters.class);

        assertEquals(params.getExecutionReason(), deserializedParams.getExecutionReason());
    }

    @Test
    public void serializeCollectionsSingletonMap() {
        MoveOrCopyParameters params = new MoveOrCopyParameters(Guid.newGuid(), Guid.newGuid());
        params.setImageToDestinationDomainMap(
                Collections.singletonMap(Guid.createGuidFromString("9edb6526-7ab5-11e6-b829-54ee755c6cc7"),
                        Guid.createGuidFromString("c097a788-7ab5-11e6-849b-54ee755c6cc7")));

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);
        assertTrue(json.length() > 0);

        MoveOrCopyParameters deserializedParams = new JsonObjectDeserializer().deserialize(
                json,
                MoveOrCopyParameters.class);

        assertEquals(params.getContainerId(), deserializedParams.getContainerId());
        assertEquals(params.getExecutionReason(), deserializedParams.getExecutionReason());
    }

    @Test
    public void serializeCollections() {
        TestCollectionsParams params = new TestCollectionsParams();

        HashSet<String> concreteSet = new HashSet<>();
        concreteSet.add("value1");
        params.setConcreteSet(concreteSet);

        Set<String> nonConcreteSet = new HashSet<>();
        nonConcreteSet.add("value2");
        params.setNonConcreteSet(nonConcreteSet);

        HashMap<String, String> concreteMap = new HashMap<>();
        concreteMap.put("key1", "value1");
        params.setConcreteMap(concreteMap);

        Map<String, String> nonConcreteMap = new HashMap<>();
        nonConcreteMap.put("key2", "value2");
        params.setNonConcreteMap(nonConcreteMap);
        params.setSingletonSet(Collections.singleton("singletonSetValue"));
        params.setSingletonList(Collections.singletonList("singletonListValue"));
        params.setSingletonMap(Collections.singletonMap("singletonMapKey", "singletonMapValue"));
        params.setArraysAsList(Arrays.asList("arraysAsList1", "arraysAsList2"));
        params.setUnmodifiableSet(Collections.singleton("unmodifiableSetValue"));
        params.setUnmodifiableList(Collections.singletonList("unmodifiableListValue"));
        params.setUnmodifiableMap(Collections.singletonMap("unmodifiableMapKey", "unmodifiableMapValue"));

        JsonObjectSerializer serializer = new JsonObjectSerializer();
        String json = serializer.serialize(params);

        TestCollectionsParams deserializedParams = new JsonObjectDeserializer().deserialize(
                json,
                TestCollectionsParams.class);
        assertNotNull(deserializedParams.getConcreteSet());
        assertEquals(params.getConcreteSet(), deserializedParams.getConcreteSet());
        assertNotNull(deserializedParams.getNonConcreteSet());
        assertEquals(params.getNonConcreteSet(), deserializedParams.getNonConcreteSet());
        assertNotNull(deserializedParams.getConcreteMap());
        assertEquals(params.getConcreteMap().get("key1"), deserializedParams.getConcreteMap().get("key1"));
        assertNotNull(deserializedParams.getSingletonList());
        assertEquals(params.getSingletonList().get(0), deserializedParams.getSingletonList().get(0));
        assertNotNull(deserializedParams.getArraysAsList());
        assertEquals(params.getArraysAsList(), deserializedParams.getArraysAsList());

        String jsonFromDeserializedParams = serializer.serialize(deserializedParams);
        final TestCollectionsParams deserialized2run = new JsonObjectDeserializer().deserialize(jsonFromDeserializedParams, TestCollectionsParams.class);
        String normalizedJsonFromSerialization  = serializer.serialize(deserialized2run);

        assertEquals(jsonFromDeserializedParams, normalizedJsonFromSerialization);
    }
}
