package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class ChangeVMClusterParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(ChangeVMClusterParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			ChangeVMClusterParameters instance) throws SerializationException {
	}

	public static ChangeVMClusterParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating ChangeVMClusterParameters via custom serializer.");
		ChangeVMClusterParameters instance = new ChangeVMClusterParameters(
				((Guid) streamReader.readObject()),
				(Guid) streamReader.readObject());
		logger.severe("DESerializing getClusterId():" + instance.getClusterId());
		logger.severe("DESerializing getVmId():" + instance.getVmId());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			ChangeVMClusterParameters instance) throws SerializationException {
		logger.severe("Serializing ChangeVMClusterParameters.");
		
		logger.severe("Serializing getVmId():" + instance.getClusterId());
		streamWriter.writeObject(instance.getClusterId());
		logger.severe("Serializing getImageId():" + instance.getVmId());
		streamWriter.writeObject(instance.getVmId());
	}
}
