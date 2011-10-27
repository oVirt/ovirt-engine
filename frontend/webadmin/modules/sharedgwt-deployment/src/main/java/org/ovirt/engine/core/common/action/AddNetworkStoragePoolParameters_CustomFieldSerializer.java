package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class AddNetworkStoragePoolParameters_CustomFieldSerializer {
    public static AddNetworkStoragePoolParameters instantiate(SerializationStreamReader reader) throws SerializationException {
        AddNetworkStoragePoolParameters instance = new AddNetworkStoragePoolParameters(
            (Guid) reader.readObject(),
            (network) reader.readObject()
        );
        return instance;
    }

    public static void deserialize(SerializationStreamReader reader, AddNetworkStoragePoolParameters instance) throws SerializationException {
        // Nothing, everything done in the constructor during instantiation.
    }

    public static void serialize(SerializationStreamWriter writer, AddNetworkStoragePoolParameters instance) throws SerializationException {
        writer.writeObject(instance.getStoragePoolId());
        writer.writeObject(instance.getNetwork());
    }
}
