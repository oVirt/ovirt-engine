package com.google.gwt.user.client.rpc.core.java.util;

import java.util.UUID;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class UUID_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            UUID instance) throws SerializationException {
    }

    public static UUID instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        long l = streamReader.readLong();
        long m = streamReader.readLong();
        return new UUID(m, l);
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            UUID instance) throws SerializationException {
        streamWriter.writeLong(instance.getLeastSignificantBits());
        streamWriter.writeLong(instance.getMostSignificantBits());
    }

}
