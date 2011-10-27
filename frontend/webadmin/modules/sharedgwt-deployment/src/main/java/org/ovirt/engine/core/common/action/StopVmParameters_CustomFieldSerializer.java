package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class StopVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StopVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			StopVmParameters instance) throws SerializationException {
	}

	public static StopVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating StopVmParameters via custom serializer.");
		StopVmParameters instance = new StopVmParameters(
				((Guid) streamReader.readObject()),
				(StopVmTypeEnum) streamReader.readObject());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			StopVmParameters instance) throws SerializationException {
		logger.severe("Serializing StopVmParameters.");

		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getStopVmType());
	}
}
