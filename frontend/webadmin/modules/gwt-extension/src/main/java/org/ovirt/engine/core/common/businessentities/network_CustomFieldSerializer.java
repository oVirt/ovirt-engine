package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class network_CustomFieldSerializer {

    public static network instantiate(SerializationStreamReader reader) throws SerializationException {
        // If we are running in the server side we must check the id provided by
        // the client, if it is null then we need to create it before passing it
        // to the server:
        Guid id = (Guid) reader.readObject();
        if (!GWT.isClient() && id == null) {
            id = Guid.NewGuid();
        }

        // Create the instance, note that some fields have to be populated later, 
        // outside the constructor:
        network instance = new network(
            reader.readString(), // addr
            reader.readString(), // description
            id, // id
            reader.readString(), // name
            reader.readString(), // subnet
            reader.readString(), // gateway
            (Integer) reader.readObject(), // type
            (Integer) reader.readObject(), // vlan_id
            reader.readBoolean() // stp
        );

        return instance;
    }

    public static void deserialize(SerializationStreamReader reader, network instance) throws SerializationException {
        // The rest of the fields are handled by the constructor in the instantiate method:
        instance.setCluster((network_cluster) reader.readObject());
        instance.setis_display((Boolean) reader.readObject());
        instance.setStatus((NetworkStatus) reader.readObject());
        instance.setstorage_pool_id((NGuid) reader.readObject());
    }

    public static void serialize(SerializationStreamWriter writer, network instance) throws SerializationException {
        // First the id, must be the first, but nothing special to do:
        writer.writeObject(instance.getId());
        
        // Then the fields corresponding to the constructor:
        writer.writeString(instance.getaddr());
        writer.writeString(instance.getdescription());
        writer.writeString(instance.getname());
        writer.writeString(instance.getsubnet());
        writer.writeString(instance.getgateway());
        writer.writeObject(instance.gettype());
        writer.writeObject(instance.getvlan_id());
        writer.writeBoolean(instance.getstp());
        
        // The rest of the fields:
        writer.writeObject(instance.getCluster());
        writer.writeObject(instance.getis_display());
        writer.writeObject(instance.getStatus());
        writer.writeObject(instance.getstorage_pool_id());
    }

}
