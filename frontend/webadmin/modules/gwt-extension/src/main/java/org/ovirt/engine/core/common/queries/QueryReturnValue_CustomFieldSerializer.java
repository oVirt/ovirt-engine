package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.ui.extension.ObjectSerializer;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class QueryReturnValue_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            QueryReturnValue instance) throws SerializationException {
        instance.setSucceeded(streamReader.readBoolean());
        instance.setExceptionString(streamReader.readString());
        instance.setReturnValue(ObjectSerializer.deserialize(streamReader));
    }

    public static QueryReturnValue instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new QueryReturnValue();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            QueryReturnValue instance) throws SerializationException {
        streamWriter.writeBoolean(instance.getSucceeded());
        streamWriter.writeString(instance.getExceptionString());
        ObjectSerializer.serialize(streamWriter, instance.getReturnValue());
    }

}
