package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class VmOperationParameterBase_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(VmOperationParameterBase.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			VmOperationParameterBase instance) throws SerializationException {
	}

	public static VmOperationParameterBase instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating VmOperationParameterBase via custom serializer.");

		VmOperationParameterBase instance = new VmOperationParameterBase((Guid) streamReader.readObject());
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, VmOperationParameterBase instance) throws SerializationException {
		logger.severe("Serializing VmOperationParameterBase.");

		streamWriter.writeObject(instance.getVmId());
	}
}
