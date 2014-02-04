package org.ovirt.engine.core.authentication;

import org.ovirt.engine.core.common.utils.ExternalId;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * We use a custom serializer for directory groups because we want to avoid having no args constructor.
 */
@SuppressWarnings("unused")
public class DirectoryGroup_CustomFieldSerializer {

    public static DirectoryGroup instantiate(SerializationStreamReader reader) throws SerializationException {
        Directory directory = DirectoryManager.getInstance().getDirectory(reader.readString());
        ExternalId id = (ExternalId) reader.readObject();
        String name = reader.readString();
        return new DirectoryGroup(directory, id, name);
    }

    public static void serialize(SerializationStreamWriter writer, DirectoryGroup group) throws SerializationException {
        writer.writeObject(group.getDirectory().getName());
        writer.writeObject(group.getId());
        writer.writeString(group.getName());
    }

    public static void deserialize(SerializationStreamReader reader, DirectoryGroup group) throws SerializationException {
        // No additional attributes, all have been handled during instantiation.
    }

}
