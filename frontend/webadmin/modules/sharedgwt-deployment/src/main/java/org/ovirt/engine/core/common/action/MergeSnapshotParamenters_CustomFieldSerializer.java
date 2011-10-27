package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class MergeSnapshotParamenters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(MergeSnapshotParamenters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			MergeSnapshotParamenters instance) throws SerializationException {
	}

	public static MergeSnapshotParamenters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating MergeSnapshotParamenters via custom serializer.");
		MergeSnapshotParamenters instance = 
			new MergeSnapshotParamenters((Guid) streamReader.readObject(), (NGuid) streamReader.readObject(), (Guid) streamReader.readObject());			
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			MergeSnapshotParamenters instance) throws SerializationException {
		logger.severe("Serializing MergeSnapshotParamenters.");				
		streamWriter.writeObject(instance.getSourceVmSnapshotId());
		streamWriter.writeObject(instance.getDestVmSnapshotId());
		streamWriter.writeObject(instance.getVmId());
	}
}
