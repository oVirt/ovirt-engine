package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ValueObjectMap_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            ValueObjectMap instance) throws SerializationException {
        instance.setValuePairs((ArrayList<ValueObjectPair>) streamReader.readObject());
    }

    public static ValueObjectMap instantiate(
            SerializationStreamReader streamReader)
            throws SerializationException {
        return new ValueObjectMap();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            ValueObjectMap instance) throws SerializationException {
        streamWriter.writeObject(instance.getValuePairs());
    }

}
