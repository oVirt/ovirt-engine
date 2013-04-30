package org.ovirt.engine.core.common.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.compat.Guid;

public class VdcReturnValueBase_CustomFieldSerializer {
	public static void deserialize(SerializationStreamReader streamReader,
			VdcReturnValueBase instance) throws SerializationException {
		// Handled in instantiate
	}

	public static VdcReturnValueBase instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		// occur first

		VdcReturnValueBase vrvb = new VdcReturnValueBase();
		vrvb.setCanDoAction(streamReader.readBoolean());
		java.util.ArrayList<String> canDoActionMessages = (java.util.ArrayList<String>) streamReader
				.readObject();
		vrvb.setCanDoActionMessages(canDoActionMessages);
		vrvb.setSucceeded(streamReader.readBoolean());
		vrvb.setIsSyncronious(streamReader.readBoolean());
		vrvb.setActionReturnValue(streamReader.readObject());
		vrvb.setDescription(streamReader.readString());
                java.util.ArrayList<Guid> asyncTaskIdList = (java.util.ArrayList<Guid>) streamReader
				.readObject();
		vrvb.setTaskPlaceHolderIdList(asyncTaskIdList);
		java.util.ArrayList<Guid> taskIdList = (java.util.ArrayList<Guid>) streamReader
				.readObject();
		vrvb.setTaskIdList(taskIdList);
		vrvb.setEndActionTryAgain(streamReader.readBoolean());
		vrvb.setFault((VdcFault)streamReader.readObject());

		return vrvb;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			VdcReturnValueBase instance) throws SerializationException {

		streamWriter.writeBoolean(instance.getCanDoAction());
		streamWriter.writeObject(instance.getCanDoActionMessages());
		streamWriter.writeBoolean(instance.getSucceeded());
		streamWriter.writeBoolean(instance.getIsSyncronious());
		streamWriter.writeObject(instance.getActionReturnValue());
		streamWriter.writeString(instance.getDescription());
                streamWriter.writeObject(instance.getTaskPlaceHolderIdList());
		streamWriter.writeObject(instance.getTaskIdList());
		streamWriter.writeBoolean(instance.getEndActionTryAgain());
		streamWriter.writeObject(instance.getFault());
	}
}
