package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.common.utils.ExternalId;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * We use a custom serializer for directory users because we want to replace the directory reference with the name of
 * the directory.
 */
@SuppressWarnings("unused")
public class DirectoryUser_CustomFieldSerializer {

    public static DirectoryUser instantiate(SerializationStreamReader reader) throws SerializationException {
        String directoryName = reader.readString();
        ExternalId id = (ExternalId) reader.readObject();
        String name = reader.readString();
        return new DirectoryUser(directoryName, id, name);
    }

    public static void serialize(SerializationStreamWriter writer, DirectoryUser user) throws SerializationException {
        writer.writeString(user.getDirectoryName());
        writer.writeObject(user.getId());
        writer.writeString(user.getName());
        writer.writeBoolean(user.isAdmin());
        writer.writeString(user.getDepartment());
        writer.writeString(user.getEmail());
        writer.writeString(user.getFirstName());
        writer.writeString(user.getLastName());
        writer.writeInt(user.getStatus().getValue());
    }

    public static void deserialize(SerializationStreamReader reader, DirectoryUser user) throws SerializationException {
        user.setAdmin(reader.readBoolean());
        user.setDepartment(reader.readString());
        user.setEmail(reader.readString());
        user.setFirstName(reader.readString());
        user.setLastName(reader.readString());
        user.setStatus(DirectoryEntryStatus.forValue(reader.readInt()));
    }

}
