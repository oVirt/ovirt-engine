package org.ovirt.engine.core.utils.serialization.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;


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
        Assert.assertTrue(serializer.serialize(net).length() > 0);
    }

    @Test
    public void serializeVdsNetworkInterface() {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        Assert.assertTrue(serializer.serialize(nic).length() > 0);
    }

    @Test
    public void serializeVdsActionParameters() {
        VdcActionParametersBase params = new VdcActionParametersBase();
        params.setLockProperties(LockProperties.create(Scope.None).withWait(true));
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        Assert.assertTrue(serializer.serialize(params).length() > 0);
    }

    @Test
    public void serializeParametersMap() {
        Map<String, Serializable> data = new HashMap<>();
        data.put("NEXT_COMMAND_TYPE", VdcActionType.DestroyImage);
        JsonObjectSerializer serializer = new JsonObjectSerializer();
        Assert.assertTrue(serializer.serialize(data).length() > 0);
    }
}
