package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StorageDomainParametersBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        StorageDomainParametersBase instance) throws SerializationException {
	}

	public static StorageDomainParametersBase instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating StorageDomainParametersBase via custom serializer");

		StorageDomainParametersBase instance = 
		    new StorageDomainParametersBase((Guid)streamReader.readObject());

		instance.setIsInternal(streamReader.readBoolean());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        StorageDomainParametersBase instance) throws SerializationException {
		logger.severe("Serializing StorageDomainParametersBase.");

		streamWriter.writeObject(instance.getStoragePoolId());
		streamWriter.writeBoolean(instance.getIsInternal());
	}
}
