package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainManagementParameter_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(StorageDomainManagementParameter.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        StorageDomainManagementParameter instance) throws SerializationException {
	}

	public static StorageDomainManagementParameter instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating StorageDomainManagementParameter via custom serializer");

		StorageDomainManagementParameter instance = 
		    new StorageDomainManagementParameter((storage_domain_static)streamReader.readObject());
		
		instance.setVdsId((Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        StorageDomainManagementParameter instance) throws SerializationException {
		logger.severe("Serializing StorageDomainManagementParameter.");

		streamWriter.writeObject(instance.getStorageDomain());
		streamWriter.writeObject(instance.getVdsId());
	}
}
