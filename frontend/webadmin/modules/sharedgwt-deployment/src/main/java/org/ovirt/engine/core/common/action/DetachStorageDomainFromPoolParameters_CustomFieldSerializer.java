package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class DetachStorageDomainFromPoolParameters_CustomFieldSerializer {
    private static Logger logger = Logger.getLogger(DetachStorageDomainFromPoolParameters.class
            .getName());

    public static void deserialize(SerializationStreamReader streamReader,
            DetachStorageDomainFromPoolParameters instance) throws SerializationException {
    }

    public static DetachStorageDomainFromPoolParameters instantiate(
            SerializationStreamReader streamReader)
            throws SerializationException {
        logger.severe("Instantiating DetachStorageDomainFromPoolParameters via custom serializer.");

        DetachStorageDomainFromPoolParameters instance =
                new DetachStorageDomainFromPoolParameters((Guid) streamReader.readObject(),
                        (Guid) streamReader.readObject());
        instance.setRemoveLast(streamReader.readBoolean());
        instance.setDestroyingPool(streamReader.readBoolean());
        return instance;
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            DetachStorageDomainFromPoolParameters instance) throws SerializationException {
        logger.severe("Serializing DetachStorageDomainFromPoolParameters.");

        streamWriter.writeObject(instance.getStorageDomainId());
        streamWriter.writeObject(instance.getStoragePoolId());
        streamWriter.writeBoolean(instance.getRemoveLast());
        streamWriter.writeBoolean(instance.getDestroyingPool());
    }
}
