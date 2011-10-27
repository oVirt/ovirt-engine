package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import org.ovirt.engine.core.compat.Guid;

public class VdsGroupParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VdsGroupParametersBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			VdsGroupParametersBase instance) throws SerializationException {
	}

	public static VdsGroupParametersBase instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating VdsGroupParametersBase via custom serializer");

		VdsGroupParametersBase instance = 
		    new VdsGroupParametersBase((Guid)streamReader.readObject());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			VdsGroupParametersBase instance) throws SerializationException {
		logger.severe("Serializing VmTemplateParametersBase.");

		streamWriter.writeObject(instance.getVdsGroupId());
	}
}
