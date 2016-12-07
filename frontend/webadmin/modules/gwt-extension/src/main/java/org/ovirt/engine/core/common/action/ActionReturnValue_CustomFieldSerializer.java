package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.extension.ObjectSerializer;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ActionReturnValue_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            ActionReturnValue instance) throws SerializationException {
        instance.setValid(streamReader.readBoolean());
        instance.setValidationMessages((ArrayList<String>) streamReader.readObject());
        instance.setExecuteFailedMessages((ArrayList<String>) streamReader.readObject());
        instance.setSucceeded(streamReader.readBoolean());
        instance.setIsSynchronous(streamReader.readBoolean());
        instance.setDescription(streamReader.readString());
        instance.setTaskPlaceHolderIdList((ArrayList<Guid>) streamReader.readObject());
        instance.setVdsmTaskIdList((ArrayList<Guid>) streamReader.readObject());
        instance.setEndActionTryAgain(streamReader.readBoolean());
        instance.setFault((EngineFault) streamReader.readObject());
        instance.setActionReturnValue(ObjectSerializer.deserialize(streamReader));
    }

    public static ActionReturnValue instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new ActionReturnValue();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            ActionReturnValue instance) throws SerializationException {
        streamWriter.writeBoolean(instance.isValid());
        streamWriter.writeObject(instance.getValidationMessages());
        streamWriter.writeObject(instance.getExecuteFailedMessages());
        streamWriter.writeBoolean(instance.getSucceeded());
        streamWriter.writeBoolean(instance.getIsSynchronous());
        streamWriter.writeString(instance.getDescription());
        streamWriter.writeObject(instance.getTaskPlaceHolderIdList());
        streamWriter.writeObject(instance.getVdsmTaskIdList());
        streamWriter.writeBoolean(instance.getEndActionTryAgain());
        streamWriter.writeObject(instance.getFault());
        ObjectSerializer.serialize(streamWriter, instance.getActionReturnValue());
    }

}
