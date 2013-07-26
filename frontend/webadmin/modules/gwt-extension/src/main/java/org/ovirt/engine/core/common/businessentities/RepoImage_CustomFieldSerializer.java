package org.ovirt.engine.core.common.businessentities;

import java.util.Date;

import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * We need this custom field serializer for {@link RepoImage} because otherwise
 * the GWT compiler will optimize out some of the fields of the entity and that
 * causes exceptions in the standard deserialization mechanism in the server
 * side.
 */
public class RepoImage_CustomFieldSerializer {

    public static RepoImage instantiate(SerializationStreamReader reader) throws SerializationException {
        return new RepoImage();
    }

    public static void serialize(SerializationStreamWriter writer, RepoImage instance) throws SerializationException {
        writer.writeObject(instance.getStoragePoolId());
        writer.writeObject(instance.getStoragePoolStatus());
        writer.writeObject(instance.getVdsStatus());
        writer.writeObject(instance.getRepoDomainId());
        writer.writeObject(instance.getStorageDomainStatus());
        writer.writeString(instance.getRepoImageId());
        writer.writeString(instance.getRepoImageName());
        writer.writeLong(instance.getSize());
        writer.writeObject(instance.getDateCreated());
        writer.writeLong(instance.getLastRefreshed());
        writer.writeObject(instance.getFileType());
    }

    public static void deserialize(SerializationStreamReader reader, RepoImage instance) throws SerializationException {
        instance.setStoragePoolId((Guid) reader.readObject());
        instance.setStoragePoolStatus((StoragePoolStatus) reader.readObject());
        instance.setVdsStatus((VDSStatus) reader.readObject());
        instance.setRepoDomainId((Guid) reader.readObject());
        instance.setStorageDomainStatus((StorageDomainStatus) reader.readObject());
        instance.setRepoImageId(reader.readString());
        instance.setRepoImageName(reader.readString());
        instance.setSize(reader.readLong());
        instance.setDateCreated((Date) reader.readObject());
        instance.setLastRefreshed(reader.readLong());
        instance.setFileType((ImageFileType) reader.readObject());
    }

}
