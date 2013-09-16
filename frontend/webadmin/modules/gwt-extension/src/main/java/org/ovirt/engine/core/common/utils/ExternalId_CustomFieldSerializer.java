package org.ovirt.engine.core.common.utils;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * The RPC mechanism used by GWT doesn't work well with arrays of bytes so we
 * need to serialize them manually.
 */
@SuppressWarnings("unused")
public class ExternalId_CustomFieldSerializer {

    public static ExternalId instantiate(SerializationStreamReader reader) throws SerializationException {
        int length = reader.readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = reader.readByte();
        }
        return new ExternalId(bytes);
    }

    public static void serialize(SerializationStreamWriter writer, ExternalId instance) throws SerializationException {
        byte[] bytes = instance.getBytes();
        writer.writeInt(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            writer.writeByte(bytes[i]);
        }
    }

    public static void deserialize(SerializationStreamReader reader, ExternalId instance) throws SerializationException {
        // Nothing to do, the deserialization has already been performed during
        // instantiation.
    }

}
