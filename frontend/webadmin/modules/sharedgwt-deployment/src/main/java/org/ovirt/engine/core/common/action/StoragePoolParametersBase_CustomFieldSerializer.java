package org.ovirt.engine.core.common.action;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StoragePoolParametersBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        StoragePoolParametersBase instance) throws SerializationException {
	}

	public static StoragePoolParametersBase instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating StoragePoolParametersBase via custom serializer");

		StoragePoolParametersBase instance = 
		    new StoragePoolParametersBase((Guid)streamReader.readObject());

		instance.setVdsId((Guid)streamReader.readObject());
		instance.setSuppressCheck(streamReader.readBoolean());
		instance.setForceDelete(streamReader.readBoolean());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        StoragePoolParametersBase instance) throws SerializationException {
		logger.severe("Serializing StoragePoolParametersBase.");

		streamWriter.writeObject(instance.getStoragePoolId());
		streamWriter.writeObject(instance.getVdsId());    
		streamWriter.writeBoolean(instance.getSuppressCheck());
		streamWriter.writeBoolean(instance.getForceDelete());
	}
}
