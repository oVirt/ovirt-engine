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
		// occur first
		long l = streamReader.readLong();
		long m = streamReader.readLong();

		UUID instance = new UUID(m, l);

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			UUID instance) throws SerializationException {

		streamWriter.writeLong(instance.getLeastSignificantBits());
		streamWriter.writeLong(instance.getMostSignificantBits());
	}
}
