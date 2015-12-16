package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class VdcReturnValueBase_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            VdcReturnValueBase instance) throws SerializationException {
        instance.setValid(streamReader.readBoolean());
        java.util.ArrayList<String> validationMessages = (java.util.ArrayList<String>) streamReader.readObject();
        instance.setValidationMessages(validationMessages);
        java.util.ArrayList<String> executeFailedMessages = (java.util.ArrayList<String>) streamReader.readObject();
        instance.setExecuteFailedMessages(executeFailedMessages);
        instance.setSucceeded(streamReader.readBoolean());
        instance.setIsSyncronious(streamReader.readBoolean());
        instance.setActionReturnValue(streamReader.readObject());
        instance.setDescription(streamReader.readString());
        java.util.ArrayList<Guid> asyncTaskIdList = (java.util.ArrayList<Guid>) streamReader.readObject();
        instance.setTaskPlaceHolderIdList(asyncTaskIdList);
        java.util.ArrayList<Guid> taskIdList = (java.util.ArrayList<Guid>) streamReader.readObject();
        instance.setVdsmTaskIdList(taskIdList);
        instance.setEndActionTryAgain(streamReader.readBoolean());
        instance.setFault((EngineFault) streamReader.readObject());
    }

    public static VdcReturnValueBase instantiate(
            SerializationStreamReader streamReader)
            throws SerializationException {
        return new VdcReturnValueBase();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            VdcReturnValueBase instance) throws SerializationException {
        streamWriter.writeBoolean(instance.isValid());
        streamWriter.writeObject(instance.getValidationMessages());
        streamWriter.writeObject(instance.getExecuteFailedMessages());
        streamWriter.writeBoolean(instance.getSucceeded());
        streamWriter.writeBoolean(instance.getIsSyncronious());
        streamWriter.writeObject(instance.getActionReturnValue());
        streamWriter.writeString(instance.getDescription());
        streamWriter.writeObject(instance.getTaskPlaceHolderIdList());
        streamWriter.writeObject(instance.getVdsmTaskIdList());
        streamWriter.writeBoolean(instance.getEndActionTryAgain());
        streamWriter.writeObject(instance.getFault());
    }

}
