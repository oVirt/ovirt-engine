package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class VDSGroup_CustomFieldSerializer {
    public static VDSGroup instantiate(SerializationStreamReader reader) throws SerializationException {
        VDSGroup instance = new VDSGroup();
        return instance;
    }

    public static void deserialize(SerializationStreamReader reader, VDSGroup instance) throws SerializationException {
        instance.setcompatibility_version((Version) reader.readObject());
        instance.setcpu_name(reader.readString());
        instance.setcpu_over_commit_duration_minutes(reader.readInt());
        instance.setdescription(reader.readString());
        instance.sethigh_utilization(reader.readInt());
        instance.sethypervisor_type((HypervisorType) reader.readObject());
        instance.setID((Guid) reader.readObject());
        instance.setlow_utilization(reader.readInt());
        instance.setmax_vds_memory_over_commit(reader.readInt());
        instance.setMigrateOnError((MigrateOnErrorOptions) reader.readObject());
        instance.setname(reader.readString());
        instance.setselection_algorithm((VdsSelectionAlgorithm) reader.readObject());
        instance.setstorage_pool_id((NGuid) reader.readObject());
        instance.setTransparentHugepages(reader.readBoolean());
    }

    public static void serialize(SerializationStreamWriter writer, VDSGroup instance) throws SerializationException {
        writer.writeObject(instance.getcompatibility_version());
        writer.writeString(instance.getcpu_name());
        writer.writeInt(instance.getcpu_over_commit_duration_minutes());
        writer.writeString(instance.getdescription());
        writer.writeInt(instance.gethigh_utilization());
        writer.writeObject(instance.gethypervisor_type());
        writer.writeObject(instance.getID());
        writer.writeInt(instance.getlow_utilization());
        writer.writeInt(instance.getmax_vds_memory_over_commit());
        writer.writeObject(instance.getMigrateOnError());
        writer.writeString(instance.getname());
        writer.writeObject(instance.getselection_algorithm());
        writer.writeObject(instance.getstorage_pool_id());
        writer.writeBoolean(instance.getTransparentHugepages());
    }
}
