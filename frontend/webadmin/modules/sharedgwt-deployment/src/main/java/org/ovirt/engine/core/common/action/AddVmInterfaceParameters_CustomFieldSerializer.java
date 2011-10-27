package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmInterfaceParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddVmInterfaceParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			AddVmInterfaceParameters instance) throws SerializationException {
	}

	public static AddVmInterfaceParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating AddVmInterfaceParameters via custom serializer");
		Guid vmGuid = (Guid) streamReader.readObject();
		VmNetworkInterface tempNIC = (VmNetworkInterface) streamReader.readObject();
		logger.severe("Network id:" + tempNIC.getId());

		//TODO: Temporary work around until we'll provide a JS Guid generator
		//if (tempNIC.getid() == null || tempNIC.getid().getUuid() == null) {
		//	logger.severe("Instantiated Interface arrived with a nul GUID, generating a new GUID");
		//	tempNIC.setid(Guid.NewGuid());
		//}

		AddVmInterfaceParameters instance = new AddVmInterfaceParameters(
				vmGuid,
				tempNIC);

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			AddVmInterfaceParameters instance) throws SerializationException {
		logger.severe("Serializing AddVmInterfaceParameters.");

		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getInterface());
	}
}
