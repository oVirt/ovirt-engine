package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskToVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddDiskToVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			AddDiskToVmParameters instance) throws SerializationException {
	}

	public static AddDiskToVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating AddDiskToVmParameters via custom serializer.");
		AddDiskToVmParameters instance = new AddDiskToVmParameters(
				((Guid) streamReader.readObject()),
				(DiskImageBase) streamReader.readObject());
		instance.setStorageDomainId((Guid)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			AddDiskToVmParameters instance) throws SerializationException {
		logger.severe("Serializing AddDiskToVmParameters.");

		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getDiskInfo());
		streamWriter.writeObject(instance.getStorageDomainId());
	}
}
