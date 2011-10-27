package org.ovirt.engine.core.common.action;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class SetVmTicketParameters_CustomFieldSerializer {
	public static void deserialize(SerializationStreamReader streamReader,
			SetVmTicketParameters instance) throws SerializationException {
	}

	public static SetVmTicketParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		SetVmTicketParameters instance = new SetVmTicketParameters(
				((Guid) streamReader.readObject()), streamReader.readString(),
				streamReader.readInt(), streamReader.readString());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			SetVmTicketParameters instance) throws SerializationException {
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeString(instance.getTicket());
		streamWriter.writeInt(instance.getValidTime());
		streamWriter.writeString(instance.getClientIp());
	}
}