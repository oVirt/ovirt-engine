package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDiskParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(UpdateVmDiskParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			UpdateVmDiskParameters instance) throws SerializationException {
	}

	public static UpdateVmDiskParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating UpdateVmDiskParameters via custom serializer.");
		UpdateVmDiskParameters instance = new UpdateVmDiskParameters(
				((Guid) streamReader.readObject()),
				(Guid)streamReader.readObject(),
				(DiskImageBase) streamReader.readObject());
		logger.severe("DESerializing getVmId():" + instance.getVmId());
		logger.severe("DESerializing getImageId():" + instance.getImageId());
		logger.severe("DESerializing getDiskInfo():" + instance.getDiskInfo());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			UpdateVmDiskParameters instance) throws SerializationException {
		logger.severe("Serializing UpdateVmDiskParameters.");

		
		logger.severe("Serializing getVmId():" + instance.getVmId());
		streamWriter.writeObject(instance.getVmId());
		logger.severe("Serializing getImageId():" + instance.getImageId());
		streamWriter.writeObject(instance.getImageId());
		logger.severe("Serializing getDiskInfo():" + instance.getDiskInfo());
		streamWriter.writeObject(instance.getDiskInfo());
	}
}
