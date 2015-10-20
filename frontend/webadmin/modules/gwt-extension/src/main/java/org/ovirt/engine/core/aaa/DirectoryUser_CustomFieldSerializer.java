package org.ovirt.engine.core.aaa;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * We use a custom serializer for directory users because we want to replace the directory reference with the name of
 * the directory.
 */
public class DirectoryUser_CustomFieldSerializer {

    public static DirectoryUser instantiate(SerializationStreamReader reader) throws SerializationException {
        String directoryName = reader.readString();
        String namespace =  reader.readString();
        String id =  reader.readString();
        String name = reader.readString();
        String principal = reader.readString();
        String displayName = reader.readString();
        return new DirectoryUser(directoryName, namespace, id, name, principal, displayName);
    }

    public static void serialize(SerializationStreamWriter writer, DirectoryUser user) throws SerializationException {
        writer.writeString(user.getDirectoryName());
        writer.writeObject(user.getNamespace());
        writer.writeObject(user.getId());
        writer.writeString(user.getName());
        writer.writeString(user.getPrincipal());
        writer.writeString(user.getDisplayName());
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
