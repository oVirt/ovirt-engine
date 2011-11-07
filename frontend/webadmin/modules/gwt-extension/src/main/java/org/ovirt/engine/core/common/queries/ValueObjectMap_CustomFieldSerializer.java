package org.ovirt.engine.core.common.queries;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ValueObjectMap_CustomFieldSerializer {

    public static ValueObjectMap instantiate(SerializationStreamReader reader) throws SerializationException {
        return new ValueObjectMap();
    }

    public static void serialize(SerializationStreamWriter writer, ValueObjectMap instance) throws SerializationException {
        // First write the number of pairs:
        ValueObjectPair[] pairs = instance.getValuePairs();
        writer.writeInt(pairs.length);

        // Then, for each pair, write first the key and then the value:
        for (ValueObjectPair pair : pairs) {
            writer.writeObject(pair.getKey());
            writer.writeObject(pair.getValue());
        }
    }

    public static void deserialize(SerializationStreamReader reader, ValueObjectMap instance) throws SerializationException {
        // First read the number of pairs:
        int length = reader.readInt();

        // Then, for each pair, read first the key and then the value:
        ValueObjectPair[] pairs = new ValueObjectPair[length];
        for (int i = 0; i < length; i++) {
            pairs[i] = new ValueObjectPair();
            pairs[i].setKey(reader.readObject());
            pairs[i].setValue(reader.readObject());
        }
        instance.setValuePairs(pairs);
    }

}
