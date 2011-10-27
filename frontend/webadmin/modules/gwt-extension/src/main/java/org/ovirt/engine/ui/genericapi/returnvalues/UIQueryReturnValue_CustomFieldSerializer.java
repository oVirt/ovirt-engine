package org.ovirt.engine.ui.genericapi.returnvalues;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public class UIQueryReturnValue_CustomFieldSerializer {
	public static void deserialize(SerializationStreamReader streamReader,
			UIQueryReturnValue instance) throws SerializationException {
		GWT.log("deserialize UIQueryReturnValue");
		// Handled in instantiate
	}

	public static UIQueryReturnValue instantiate(
			SerializationStreamReader streamReader)
			throws SerializationException {
		// occur first
		GWT.log("instantiate UIQueryReturnValue");

		UIQueryReturnValue r = new UIQueryReturnValue();
		r.setSucceeded(streamReader.readBoolean());
		r.setReturnValue(streamReader.readObject());

		return r;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			UIQueryReturnValue instance) throws SerializationException {
		streamWriter.writeBoolean(instance.getSucceeded());
		streamWriter.writeObject(instance.getReturnValue());
	}
}
