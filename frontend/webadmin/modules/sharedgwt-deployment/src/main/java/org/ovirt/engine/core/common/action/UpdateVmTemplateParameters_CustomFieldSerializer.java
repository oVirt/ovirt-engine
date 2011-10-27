package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class UpdateVmTemplateParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(UpdateVmTemplateParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader, UpdateVmTemplateParameters instance) throws SerializationException {
	}

	public static UpdateVmTemplateParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating UpdateVmTemplateParameters via custom serializer.");

		UpdateVmTemplateParameters instance = new UpdateVmTemplateParameters((VmTemplate)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, UpdateVmTemplateParameters instance) throws SerializationException {
		logger.severe("Serializing UpdateVmTemplateParameters.");

		streamWriter.writeObject(instance.getVmTemplateData());
	}
}
