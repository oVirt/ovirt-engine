package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class TryBackToAllSnapshotsOfVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(TryBackToAllSnapshotsOfVmParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			TryBackToAllSnapshotsOfVmParameters instance) throws SerializationException {
	}

	public static TryBackToAllSnapshotsOfVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating TryBackToAllSnapshotsOfVmParameters via custom serializer.");
		TryBackToAllSnapshotsOfVmParameters instance = 
			new TryBackToAllSnapshotsOfVmParameters((Guid) streamReader.readObject(), (Guid) streamReader.readObject());			
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			TryBackToAllSnapshotsOfVmParameters instance) throws SerializationException {
		logger.severe("Serializing TryBackToAllSnapshotsOfVmParameters.");				
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getDstSnapshotId());
	}
}
