package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmTemplateInterfaceParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RemoveVmTemplateInterfaceParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        RemoveVmTemplateInterfaceParameters instance) throws SerializationException {
	}

	public static RemoveVmTemplateInterfaceParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating RemoveVmTemplateInterfaceParameters via custom serializer.");
		
		RemoveVmTemplateInterfaceParameters instance = 
		        new RemoveVmTemplateInterfaceParameters((Guid)streamReader.readObject(), (Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, RemoveVmTemplateInterfaceParameters instance) throws SerializationException {
		logger.severe("Serializing RemoveVmTemplateInterfaceParameters.");
		
		streamWriter.writeObject(instance.getVmTemplateId());
		streamWriter.writeObject(instance.getInterface() != null ? instance.getInterface().getId() : null);		
	}
}
