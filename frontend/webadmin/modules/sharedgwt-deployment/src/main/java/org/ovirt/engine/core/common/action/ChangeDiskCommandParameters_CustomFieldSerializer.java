package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskCommandParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(ChangeVMClusterParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			ChangeDiskCommandParameters instance) throws SerializationException {
	}

	public static ChangeDiskCommandParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating ChangeDiskCommandParameters via custom serializer.");
		ChangeDiskCommandParameters instance = new ChangeDiskCommandParameters(((Guid) streamReader.readObject()), streamReader.readString());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, ChangeDiskCommandParameters instance) throws SerializationException {
		logger.severe("Serializing ChangeDiskCommandParameters.");
		
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeString(instance.getCdImagePath());
	}
}