package org.ovirt.engine.core.common.action;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.compat.Guid;

public class RunVmOnceParams_CustomFieldSerializer {
	private static Logger logger = Logger.getLogger(RunVmOnceParams.class.getName());
	
	public static void deserialize(SerializationStreamReader streamReader,
	        RunVmOnceParams instance) throws SerializationException {
	}

	public static RunVmOnceParams instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		logger.severe("Instantiating RunVmOnceParams via custom serializer.");

		RunVmOnceParams instance = new RunVmOnceParams((Guid)streamReader.readObject());
		instance.setBootSequence((BootSequence)streamReader.readObject());
        instance.setDiskPath(streamReader.readString());
        instance.setFloppyPath(streamReader.readString());
        instance.setKvmEnable(streamReader.readBoolean());
        instance.setRunAndPause(streamReader.readBoolean());
        instance.setAcpiEnable(streamReader.readBoolean());
        instance.setRunAsStateless((Boolean)streamReader.readObject());
        instance.setReinitialize(streamReader.readBoolean());
        instance.setCustomProperties(streamReader.readString());
        instance.setUseVnc((Boolean)streamReader.readObject());     
        instance.setkernel_url((String)streamReader.readObject());
        instance.setkernel_params((String)streamReader.readObject());
        instance.setinitrd_url((String)streamReader.readObject());
		instance.setSysPrepDomainName(streamReader.readString());
		instance.setSysPrepUserName(streamReader.readString());
		instance.setSysPrepPassword(streamReader.readString());
		
		return instance;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
	        RunVmOnceParams instance) throws SerializationException {
		logger.severe("Serializing RunVmOnceParams.");
		
		streamWriter.writeObject(instance.getVmId());
		streamWriter.writeObject(instance.getBootSequence());
        streamWriter.writeString(instance.getDiskPath());
        streamWriter.writeString(instance.getFloppyPath());
        streamWriter.writeBoolean(instance.getKvmEnable());
        streamWriter.writeBoolean(instance.getRunAndPause());
        streamWriter.writeBoolean(instance.getAcpiEnable());
        streamWriter.writeObject(instance.getRunAsStateless());
        streamWriter.writeBoolean(instance.getReinitialize());
        streamWriter.writeString(instance.getCustomProperties());   
        streamWriter.writeObject(instance.getUseVnc());
        streamWriter.writeObject(instance.getkernel_url());
        streamWriter.writeObject(instance.getkernel_params());
        streamWriter.writeObject(instance.getinitrd_url());
		streamWriter.writeString(instance.getSysPrepDomainName());
		streamWriter.writeString(instance.getSysPrepUserName());
		streamWriter.writeString(instance.getSysPrepPassword());
	}
}
