package org.ovirt.engine.core.common.action;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class AuditLog_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AuditLog.class.getName());
	
	public static void deserialize(SerializationStreamReader streamReader, AuditLog instance) throws SerializationException {
	}

	public static AuditLog instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating AuditLog via custom serializer.");

		AuditLog instance = new AuditLog(streamReader.readLong(),
										 (Date)streamReader.readObject(),
										 streamReader.readInt(),
										 streamReader.readInt(),
										 streamReader.readString(),
										 (NGuid)streamReader.readObject(),
										 streamReader.readString(),
										 (NGuid)streamReader.readObject(),
										 streamReader.readString(),
										 (NGuid)streamReader.readObject(),
										 streamReader.readString(),
										 (NGuid)streamReader.readObject(),
										 streamReader.readString());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter, AuditLog instance) throws SerializationException {
		logger.severe("Serializing AuditLog.");

		streamWriter.writeLong(instance.getaudit_log_id());
		streamWriter.writeObject(instance.getlog_time());
		streamWriter.writeInt(instance.getlog_type().getValue());
		streamWriter.writeInt(instance.getseverity().getValue());
		streamWriter.writeString(instance.getmessage());
		streamWriter.writeObject(instance.getuser_id());
		streamWriter.writeString(instance.getuser_name());
		streamWriter.writeObject(instance.getvds_id());
		streamWriter.writeString(instance.getvds_name());
		streamWriter.writeObject(instance.getvm_id());
		streamWriter.writeString(instance.getvm_name());
		streamWriter.writeObject(instance.getvm_template_id());
		streamWriter.writeString(instance.getvm_template_name());
		
	}
}

