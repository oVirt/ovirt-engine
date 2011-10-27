package org.ovirt.engine.core.common.action;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class VmTemplateParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VmTemplateParametersBase.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
	        VmTemplateParametersBase instance) throws SerializationException {
	}

	public static VmTemplateParametersBase instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating VmTemplateParametersBase via custom serializer");

		VmTemplateParametersBase instance = 
		    new VmTemplateParametersBase((Guid)streamReader.readObject());

		instance.setCheckDisksExists(streamReader.readBoolean());
		instance.setStorageDomainsList((List<Guid>) streamReader.readObject());
		instance.setRemoveTemplateFromDb(streamReader.readBoolean());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        VmTemplateParametersBase instance) throws SerializationException {
		logger.severe("Serializing VmTemplateParametersBase.");

		streamWriter.writeObject(instance.getVmTemplateId());
		streamWriter.writeBoolean(instance.getCheckDisksExists());
		streamWriter.writeObject(instance.getStorageDomainsList());
		streamWriter.writeBoolean(instance.isRemoveTemplateFromDb());
	}
}
