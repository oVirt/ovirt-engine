package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllSnapshotsFromVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(CreateAllSnapshotsFromVmParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			CreateAllSnapshotsFromVmParameters instance) throws SerializationException {
	}

	public static CreateAllSnapshotsFromVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating CreateAllSnapshotsFromVmParameters via custom serializer.");
		CreateAllSnapshotsFromVmParameters instance = 
			new CreateAllSnapshotsFromVmParameters(((Guid) streamReader.readObject()),	streamReader.readString());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			CreateAllSnapshotsFromVmParameters instance) throws SerializationException {
		logger.severe("Serializing CreateAllSnapshotsFromVmParameters.");				
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeString(instance.getDescription());
	}
}
