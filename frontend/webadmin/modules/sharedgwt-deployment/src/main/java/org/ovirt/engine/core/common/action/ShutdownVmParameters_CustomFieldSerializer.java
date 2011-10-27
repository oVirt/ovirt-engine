package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class ShutdownVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(ShutdownVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			ShutdownVmParameters instance) throws SerializationException {
	}

	public static ShutdownVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating ShutdownVmParameters via custom serializer.");
		ShutdownVmParameters instance = new ShutdownVmParameters(
				((Guid) streamReader.readObject()), streamReader.readBoolean());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			ShutdownVmParameters instance) throws SerializationException {
		logger.severe("Serializing ShutdownVmParameters.");

		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeBoolean(instance.getWaitBeforeShutdown());
	}
}
