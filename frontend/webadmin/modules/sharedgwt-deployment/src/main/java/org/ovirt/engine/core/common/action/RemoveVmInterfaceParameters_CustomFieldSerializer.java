package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmInterfaceParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RemoveVmInterfaceParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			RemoveVmInterfaceParameters instance) throws SerializationException {
	}

	public static RemoveVmInterfaceParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating RemoveVmInterfaceParameters via custom serializer.");
		RemoveVmInterfaceParameters instance = new RemoveVmInterfaceParameters(
				((Guid)streamReader.readObject()),
				(Guid)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			RemoveVmInterfaceParameters instance) throws SerializationException {
		logger.severe("Serializing RemoveVmInterfaceParameters.");

		
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getInterfaceId());		
	}
}
