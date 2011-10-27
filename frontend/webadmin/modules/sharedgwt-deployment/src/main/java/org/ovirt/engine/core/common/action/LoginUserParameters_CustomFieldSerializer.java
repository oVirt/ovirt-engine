package org.ovirt.engine.core.common.action;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class LoginUserParameters_CustomFieldSerializer {
	// private static Logger logger = Logger.getLogger(LoginUserParameters.class
	// .getName());

	public static void deserialize(SerializationStreamReader streamReader,
			LoginUserParameters instance) throws SerializationException {
	}

	public static LoginUserParameters instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		// logger.finer("Instantiating LoginUserParameters via custom serializer.");

		String userName = streamReader.readString();
		String password = streamReader.readString();
		String domain = streamReader.readString();
		String os = streamReader.readString();
		String browser = streamReader.readString();
		String clientType = streamReader.readString();
		VdcActionType actionType = (VdcActionType) streamReader.readObject();

		LoginUserParameters instance = new LoginUserParameters(userName,
				password, domain, os, browser, clientType);
		instance.setActionType(actionType);

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			LoginUserParameters instance) throws SerializationException {
		// logger.finer("Serializing LoginUserParameters.");

		streamWriter.writeString(instance.getUserName());
		streamWriter.writeString(instance.getUserPassword());
		streamWriter.writeString(instance.getDomain());
		streamWriter.writeString(instance.getOs());
		streamWriter.writeString(instance.getBrowser());
		streamWriter.writeString(instance.getClientType());
		streamWriter.writeObject(instance.getActionType());
	}
}
