package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RemoveVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			RemoveVmParameters instance) throws SerializationException {
	}

	public static RemoveVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating RemoveVmParameters via custom serializer");

		RemoveVmParameters instance = new RemoveVmParameters(
				(Guid)streamReader.readObject(),
				streamReader.readBoolean());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			RemoveVmParameters instance) throws SerializationException {
		logger.severe("Serializing RemoveVmParameters.");

		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeBoolean(instance.getForce());
	}
}
