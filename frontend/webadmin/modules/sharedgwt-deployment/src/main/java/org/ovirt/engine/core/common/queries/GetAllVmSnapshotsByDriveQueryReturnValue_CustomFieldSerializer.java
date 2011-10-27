package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.compat.Guid;

public class GetAllVmSnapshotsByDriveQueryReturnValue_CustomFieldSerializer {
	
	public static GetAllVmSnapshotsByDriveQueryReturnValue instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		
		GetAllVmSnapshotsByDriveQueryReturnValue instance = new GetAllVmSnapshotsByDriveQueryReturnValue();					
		return instance;
	}
	
	public static void deserialize(SerializationStreamReader streamReader,
			GetAllVmSnapshotsByDriveQueryReturnValue instance) throws SerializationException {		
		
		instance.setSucceeded(streamReader.readBoolean());
		instance.setExceptionString(streamReader.readString());
		
		instance.setReturnValue((ArrayList) streamReader.readObject());
		instance.setTryingImage((Guid) streamReader.readObject());
	}


	public static void serialize(SerializationStreamWriter streamWriter,
			GetAllVmSnapshotsByDriveQueryReturnValue instance)
			throws SerializationException {
		
		streamWriter.writeBoolean(instance.getSucceeded());
		streamWriter.writeString(instance.getExceptionString());
		
		streamWriter.writeObject((ArrayList) instance.getReturnValue());
		streamWriter.writeObject((Guid) instance.getTryingImage());				
	}
}