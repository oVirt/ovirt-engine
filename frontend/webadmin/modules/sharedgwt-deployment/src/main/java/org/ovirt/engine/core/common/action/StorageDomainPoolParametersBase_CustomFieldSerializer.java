package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainPoolParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StorageDomainPoolParametersBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        StorageDomainPoolParametersBase instance) throws SerializationException {
	}

	public static StorageDomainPoolParametersBase instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating StorageDomainPoolParametersBase via custom serializer");

		StorageDomainPoolParametersBase instance = 
		    new StorageDomainPoolParametersBase((Guid)streamReader.readObject(), (Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        StorageDomainPoolParametersBase instance) throws SerializationException {
		logger.severe("Serializing StorageDomainPoolParametersBase.");

		streamWriter.writeObject(instance.getStorageDomainId());
		streamWriter.writeObject(instance.getStoragePoolId());
	}
}
