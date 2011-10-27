package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class VmPoolUserParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VmPoolUserParameters.class.getName());
	
	public static void deserialize(SerializationStreamReader streamReader, VmPoolUserParameters instance) throws SerializationException {
	}

	public static VmPoolUserParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating VmPoolUserParameters via custom serializer.");

		VmPoolUserParameters instance = new VmPoolUserParameters((Guid)streamReader.readObject(), (VdcUser)streamReader.readObject(), streamReader.readBoolean());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, VmPoolUserParameters instance) throws SerializationException {
		logger.severe("Serializing VmPoolUserParameters.");

		streamWriter.writeObject(instance.getVmPoolId());
		streamWriter.writeObject(instance.getVdcUserData());
		streamWriter.writeBoolean(instance.getIsInternal());
		
	}
}
