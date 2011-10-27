package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import org.ovirt.engine.core.compat.Guid;

public class RemoveStorageDomainParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RemoveStorageDomainParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        RemoveStorageDomainParameters instance) throws SerializationException {
	}

	public static RemoveStorageDomainParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating RemoveStorageDomainParameters via custom serializer");

		RemoveStorageDomainParameters instance = 
		    new RemoveStorageDomainParameters((Guid)streamReader.readObject());
		
		instance.setDoFormat(streamReader.readBoolean());
		instance.setDestroyingPool(streamReader.readBoolean());
		instance.setVdsId((Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        RemoveStorageDomainParameters instance) throws SerializationException {
		logger.severe("Serializing RemoveStorageDomainParameters.");

		streamWriter.writeObject(instance.getStorageDomainId());
		streamWriter.writeBoolean(instance.getDoFormat());
		streamWriter.writeBoolean(instance.getDestroyingPool());
		streamWriter.writeObject(instance.getVdsId());
	}
}
