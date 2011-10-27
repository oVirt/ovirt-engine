package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllSnapshotsParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RestoreAllSnapshotsParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			RestoreAllSnapshotsParameters instance) throws SerializationException {
	}

	public static RestoreAllSnapshotsParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating RestoreAllSnapshotsParameters via custom serializer.");
		RestoreAllSnapshotsParameters instance = 
			new RestoreAllSnapshotsParameters((Guid) streamReader.readObject(), (Guid) streamReader.readObject());			
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			RestoreAllSnapshotsParameters instance) throws SerializationException {
		logger.severe("Serializing RestoreAllSnapshotsParameters.");				
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getDstSnapshotId());
	}
}
