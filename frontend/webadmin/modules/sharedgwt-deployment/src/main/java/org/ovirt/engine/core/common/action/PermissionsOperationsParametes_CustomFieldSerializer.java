package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class PermissionsOperationsParametes_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(PermissionsOperationsParametes.class.getName());

	public static void deserialize(SerializationStreamReader streamReader, PermissionsOperationsParametes instance) throws SerializationException {
	}

	public static PermissionsOperationsParametes instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating PermissionsOperationsParametes via custom serializer.");

		PermissionsOperationsParametes instance = new PermissionsOperationsParametes();

		instance.setPermission((permissions)streamReader.readObject());
		instance.setVdcUser((VdcUser)streamReader.readObject());
		instance.setAdGroup((ad_groups)streamReader.readObject());

		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, PermissionsOperationsParametes instance) throws SerializationException {
		logger.severe("Serializing PermissionsOperationsParametes.");

		
		streamWriter.writeObject(instance.getPermission());
		streamWriter.writeObject(instance.getVdcUser());
		streamWriter.writeObject(instance.getAdGroup());
	}
}
