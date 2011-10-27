package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmFromScratchParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddVmFromScratchParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			AddVmFromScratchParameters instance) throws SerializationException {
	}

	public static AddVmFromScratchParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating AddVmFromScratchParameters via custom serializer.");

		AddVmFromScratchParameters instance = new AddVmFromScratchParameters(
				(VmStatic) streamReader.readObject(), 
				(ArrayList<DiskImageBase>) streamReader.readObject(),
				Guid.Empty);
		instance.setMakeCreatorExplicitOwner(true);
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			AddVmFromScratchParameters instance) throws SerializationException {
		logger.severe("Serializing AddVmFromScratchParameters.");

		streamWriter.writeObject(instance.getVmStaticData());
		streamWriter.writeObject(instance.getDiskInfoList());
	}
}
