package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddVmTemplateParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			AddVmTemplateParameters instance) throws SerializationException {
	}

	public static AddVmTemplateParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating AddVmTemplateParameters via custom serializer.");
		AddVmTemplateParameters instance = new AddVmTemplateParameters((VM)streamReader.readObject(), streamReader.readString(), streamReader.readString());
		instance.setVmTemplateID((Guid)streamReader.readObject());
		instance.setPublicUse(streamReader.readBoolean());
		instance.setDestinationStorageDomainId((Guid)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			AddVmTemplateParameters instance) throws SerializationException {
		logger.severe("Serializing AddVmTemplateParameters.");
		streamWriter.writeObject(instance.getVm());
		streamWriter.writeString(instance.getName());
		streamWriter.writeString(instance.getDescription());
		streamWriter.writeObject(instance.getVmTemplateId());
		streamWriter.writeBoolean(instance.isPublicUse());
		streamWriter.writeObject(instance.getDestinationStorageDomainId());
	}
}
