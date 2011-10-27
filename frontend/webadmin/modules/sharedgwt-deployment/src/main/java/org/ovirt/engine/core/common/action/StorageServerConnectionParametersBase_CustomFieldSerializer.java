package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StorageServerConnectionParametersBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        StorageServerConnectionParametersBase instance) throws SerializationException {
	}

	public static StorageServerConnectionParametersBase instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating StorageServerConnectionParametersBase via custom serializer");

		StorageServerConnectionParametersBase instance = 
		    new StorageServerConnectionParametersBase((storage_server_connections)streamReader.readObject(), (Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        StorageServerConnectionParametersBase instance) throws SerializationException {
		logger.severe("Serializing StorageServerConnectionParametersBase.");

		streamWriter.writeObject(instance.getStorageServerConnection());
		streamWriter.writeObject(instance.getVdsId());
	}
}
