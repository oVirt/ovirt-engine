package org.ovirt.engine.core.aaa;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * We use a custom serializer for directory groups because we want to avoid having no args constructor.
 */
public class DirectoryGroup_CustomFieldSerializer {

    public static DirectoryGroup instantiate(SerializationStreamReader reader) throws SerializationException {
        String directoryName = reader.readString();
        String namespace = reader.readString();
        String id = reader.readString();
        String name = reader.readString();
        String displayName = reader.readString();
        return new DirectoryGroup(directoryName, namespace, id, name, displayName);
    }

    public static void serialize(SerializationStreamWriter writer, DirectoryGroup group) throws SerializationException {
        writer.writeObject(group.getDirectoryName());
        writer.writeObject(group.getNamespace());
        writer.writeObject(group.getId());
        writer.writeString(group.getName());
        writer.writeString(group.getDisplayName());
    }

    public static void deserialize(SerializationStreamReader reader, DirectoryGroup group) throws SerializationException {
        // No additional attributes, all have been handled during instantiation.
    }

}
