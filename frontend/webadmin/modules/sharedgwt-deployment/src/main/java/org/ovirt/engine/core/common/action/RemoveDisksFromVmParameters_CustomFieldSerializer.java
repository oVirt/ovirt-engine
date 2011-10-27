package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class RemoveDisksFromVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RemoveDisksFromVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			RemoveDisksFromVmParameters instance) throws SerializationException {
	}

	public static RemoveDisksFromVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating RemoveDisksFromVmParameters via custom serializer.");
		RemoveDisksFromVmParameters instance = new RemoveDisksFromVmParameters(
				((Guid) streamReader.readObject()),
				(ArrayList<Guid>) streamReader.readObject());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			RemoveDisksFromVmParameters instance) throws SerializationException {
		logger.severe("Serializing RemoveDisksFromVmParameters.");

		
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getImageIds());
	}
}
