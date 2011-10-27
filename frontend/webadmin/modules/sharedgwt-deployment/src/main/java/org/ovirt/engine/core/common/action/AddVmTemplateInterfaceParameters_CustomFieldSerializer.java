package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateInterfaceParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddVmTemplateInterfaceParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader, AddVmTemplateInterfaceParameters instance) throws SerializationException {
	}

	public static AddVmTemplateInterfaceParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating AddVmTemplateInterfaceParameters via custom serializer.");

		AddVmTemplateInterfaceParameters instance = new AddVmTemplateInterfaceParameters((Guid)streamReader.readObject(), (VmNetworkInterface)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, AddVmTemplateInterfaceParameters instance) throws SerializationException {
		logger.severe("Serializing AddVmTemplateInterfaceParameters.");

		streamWriter.writeObject(instance.getVmTemplateId());
		streamWriter.writeObject(instance.getInterface());
	}
}

