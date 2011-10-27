package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.VDSGroup;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class VdsGroupOperationParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VdsGroupOperationParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			VdsGroupOperationParameters instance) throws SerializationException {
	}

	public static VdsGroupOperationParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating VdsGroupOperationParameters via custom serializer");

		VdsGroupOperationParameters instance = 
		    new VdsGroupOperationParameters((VDSGroup)streamReader.readObject());

		instance.setIsInternalCommand(streamReader.readBoolean());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			VdsGroupOperationParameters instance) throws SerializationException {
		logger.severe("Serializing VmTemplateParametersBase.");

		streamWriter.writeObject(instance.getVdsGroup());
		streamWriter.writeBoolean(instance.getIsInternalCommand());
	}
}
