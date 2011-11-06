package org.ovirt.engine.core.common.queries;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import java.util.ArrayList;

public class ValueObjectMap_CustomFieldSerializer {
	public static void deserialize(SerializationStreamReader streamReader,
			ValueObjectMap instance) throws SerializationException {
		// Handled in instantiate
	}

	public static ValueObjectMap instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		// occur first
		ValueObjectMap r = new ValueObjectMap();
		r.setValuePairs((ArrayList<ValueObjectPair>)streamReader.readObject());

		return r;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			ValueObjectMap instance) throws SerializationException {
		streamWriter.writeObject(instance.getValuePairs());
	}
}
