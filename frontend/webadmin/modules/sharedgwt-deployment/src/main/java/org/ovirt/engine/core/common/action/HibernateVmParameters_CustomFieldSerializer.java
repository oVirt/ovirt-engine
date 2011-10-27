package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class HibernateVmParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(HibernateVmParameters.class
			.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			HibernateVmParameters instance) throws SerializationException {
	}

	public static HibernateVmParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating HibernateVmParameters via custom serializer.");
		HibernateVmParameters instance = new HibernateVmParameters(
				((Guid) streamReader.readObject()));

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			HibernateVmParameters instance) throws SerializationException {
		logger.severe("Serializing HibernateVmParameters.");

		streamWriter.writeObject(instance.getVmId());
	}
}
