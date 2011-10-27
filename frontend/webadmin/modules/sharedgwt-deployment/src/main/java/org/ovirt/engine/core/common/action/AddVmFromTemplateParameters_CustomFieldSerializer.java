package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmFromTemplateParameters_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(AddVmFromTemplateParameters.class.getName());

	public static void deserialize(SerializationStreamReader streamReader,
			AddVmFromTemplateParameters instance) throws SerializationException {
	}

	public static AddVmFromTemplateParameters instantiate(SerializationStreamReader streamReader) throws SerializationException {
		logger.severe("Instantiating AddVmFromTemplateParameters via custom serializer.");

        AddVmFromTemplateParameters instance = new AddVmFromTemplateParameters((VmStatic) streamReader.readObject(),
                                                updateDiskImageChildrens(streamReader),
                                                (Guid) streamReader.readObject());
		instance.setMakeCreatorExplicitOwner(streamReader.readBoolean());
		return instance;
	}

    private static HashMap<String, DiskImageBase> updateDiskImageChildrens(SerializationStreamReader streamReader)
            throws SerializationException {
        HashMap<String, DiskImageBase> diskInfoList = (HashMap<String, DiskImageBase>)streamReader.readObject();
        Set<Entry<String, DiskImageBase>> entrySet = diskInfoList.entrySet();
        for (Entry<String, DiskImageBase> entry : entrySet) {
            if (entry.getValue() instanceof DiskImage) {
                DiskImage diskImage = (DiskImage) entry.getValue();
                diskImage.setchildrenId(new Guid[0]);
                diskInfoList.put(entry.getKey(), diskImage);
            }
        }
        return diskInfoList;
    }

	public static void serialize(SerializationStreamWriter streamWriter,
			AddVmFromTemplateParameters instance) throws SerializationException {
		logger.severe("Serializing AddVmFromTemplateParameters.");

		streamWriter.writeObject(instance.getVmStaticData());
		streamWriter.writeObject(instance.getDiskInfoList());
		streamWriter.writeObject(instance.getStorageDomainId());
		streamWriter.writeBoolean(instance.isMakeCreatorExplicitOwner());

	}
}
