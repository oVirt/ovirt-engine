package org.ovirt.engine.core.common.queries;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import java.util.ArrayList;

public class ValueObjectMap_CustomFieldSerializer {

	public static void deserialize(SerializationStreamReader streamReader,
			ValueObjectMap instance) throws SerializationException {
		instance.setValuePairs((ArrayList<ValueObjectPair>)streamReader.readObject());
	}

	public static ValueObjectMap instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		ValueObjectMap instance = new ValueObjectMap();
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			ValueObjectMap instance) throws SerializationException {
		streamWriter.writeObject(instance.getValuePairs());
	}

}
