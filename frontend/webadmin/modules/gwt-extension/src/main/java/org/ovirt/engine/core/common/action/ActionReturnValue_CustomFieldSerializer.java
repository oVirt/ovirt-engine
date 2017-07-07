package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ActionReturnValue_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            ActionReturnValue instance) throws SerializationException {
        instance.setValid(streamReader.readBoolean());
        java.util.ArrayList<String> validationMessages = (java.util.ArrayList<String>) streamReader.readObject();
        instance.setValidationMessages(validationMessages);
        java.util.ArrayList<String> executeFailedMessages = (java.util.ArrayList<String>) streamReader.readObject();
        instance.setExecuteFailedMessages(executeFailedMessages);
        instance.setSucceeded(streamReader.readBoolean());
        instance.setIsSynchronous(streamReader.readBoolean());
        instance.setActionReturnValue(streamReader.readObject());
        instance.setDescription(streamReader.readString());
        java.util.ArrayList<Guid> asyncTaskIdList = (java.util.ArrayList<Guid>) streamReader.readObject();
        instance.setTaskPlaceHolderIdList(asyncTaskIdList);
        java.util.ArrayList<Guid> taskIdList = (java.util.ArrayList<Guid>) streamReader.readObject();
        instance.setVdsmTaskIdList(taskIdList);
        instance.setEndActionTryAgain(streamReader.readBoolean());
        instance.setFault((EngineFault) streamReader.readObject());
    }

    public static ActionReturnValue instantiate(
            SerializationStreamReader streamReader)
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
        streamWriter.writeObject(instance.getActionReturnValue());
        streamWriter.writeString(instance.getDescription());
        streamWriter.writeObject(instance.getTaskPlaceHolderIdList());
        streamWriter.writeObject(instance.getVdsmTaskIdList());
        streamWriter.writeBoolean(instance.getEndActionTryAgain());
        streamWriter.writeObject(instance.getFault());
    }

}
