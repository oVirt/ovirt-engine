package org.ovirt.engine.core.common.action;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class VdcLoginReturnValueBase_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            VdcLoginReturnValueBase instance) throws SerializationException {
        VdcReturnValueBase_CustomFieldSerializer.deserialize(streamReader, instance);
        instance.setLoginResult((LoginResult) streamReader.readObject());
    }

    public static VdcLoginReturnValueBase instantiate(
            SerializationStreamReader streamReader)
            throws SerializationException {
        return new VdcLoginReturnValueBase();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            VdcLoginReturnValueBase instance) throws SerializationException {
        VdcReturnValueBase_CustomFieldSerializer.serialize(streamWriter, instance);
        streamWriter.writeObject(instance.getLoginResult());
    }

}
