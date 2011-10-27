package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class DisplayNetworkToVdsGroupParameters_CustomFieldSerializer {
    public static DisplayNetworkToVdsGroupParameters instantiate(SerializationStreamReader reader) throws SerializationException {
        DisplayNetworkToVdsGroupParameters instance = new DisplayNetworkToVdsGroupParameters(
            (VDSGroup) reader.readObject(),
            (network) reader.readObject(),
            reader.readBoolean()
        );
        return instance;
    }

    public static void deserialize(SerializationStreamReader reader, DisplayNetworkToVdsGroupParameters instance) throws SerializationException {
        // Nothing, everything done in the constructor during instantiation.
    }

    public static void serialize(SerializationStreamWriter writer, DisplayNetworkToVdsGroupParameters instance) throws SerializationException {
        writer.writeObject(instance.getVdsGroup());
        writer.writeObject(instance.getNetwork());
        writer.writeBoolean(instance.getIsDisplay());
    }
}
