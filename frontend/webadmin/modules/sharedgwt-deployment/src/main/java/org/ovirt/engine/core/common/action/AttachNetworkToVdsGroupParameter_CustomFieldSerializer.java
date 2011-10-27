package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class AttachNetworkToVdsGroupParameter_CustomFieldSerializer {
    public static AttachNetworkToVdsGroupParameter instantiate(SerializationStreamReader reader) throws SerializationException {
        AttachNetworkToVdsGroupParameter instance = new AttachNetworkToVdsGroupParameter(
            (VDSGroup) reader.readObject(),
            (network) reader.readObject()
        );
        return instance;
    }

    public static void deserialize(SerializationStreamReader reader, AttachNetworkToVdsGroupParameter instance) throws SerializationException {
        // Nothing, everything done in the constructor during instantiation.
    }

    public static void serialize(SerializationStreamWriter writer, AttachNetworkToVdsGroupParameter instance) throws SerializationException {
        writer.writeObject(instance.getVdsGroup());
        writer.writeObject(instance.getNetwork());
    }
}
