package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class VmManagementParametersBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VmManagementParametersBase.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			VmManagementParametersBase instance) throws SerializationException {
	}

	public static VmManagementParametersBase instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating VmManagementParametersBase via custom serializer.");

		VmManagementParametersBase instance = new VmManagementParametersBase(
				(VmStatic) streamReader.readObject());
		instance.setStorageDomainId((Guid) streamReader.readObject());
		instance.setMakeCreatorExplicitOwner((Boolean)streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			VmManagementParametersBase instance) throws SerializationException {
		logger.severe("Serializing VmManagementParametersBase.");

		streamWriter.writeObject(instance.getVmStaticData());
		streamWriter.writeObject(instance.getStorageDomainId());
		streamWriter.writeBoolean(true);
	}
}
