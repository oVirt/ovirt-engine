package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LeaseState;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProperties;

public class GetVolumeInfoVDSCommand<P extends GetVolumeInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VolumeInfoReturnForXmlRpc result;

    public GetVolumeInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GetVolumeInfoVDSCommandParameters params = getParameters();
        result = getBroker().getVolumeInfo(
                params.getStorageDomainId().toString(),
                params.getStoragePoolId().toString(),
                params.getImageGroupId().toString(),
                params.getImageId().toString());
        proceedProxyReturnValue();
        setReturnValue(buildImageEntity(result.getVolumeInfo()));
        getVDSReturnValue().setSucceeded(getReturnValue() != null);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

    private static Date makeDTFromCTime(long ctime) {
        return new Date(ctime * 1000L);
    }

    public DiskImage buildImageEntity(Map<String, Object> xmlRpcStruct) {
        DiskImage newImage = new DiskImage();
        try {
            newImage.setImageId(new Guid((String) xmlRpcStruct.get(IrsProperties.uuid)));

            newImage.setParentId(new Guid((String) xmlRpcStruct.get(IrsProperties.parent)));
            newImage.setDescription((String) xmlRpcStruct.get(IrsProperties.description));
            newImage.setImageStatus(EnumUtils.valueOf(ImageStatus.class,
                    (String) xmlRpcStruct.get(IrsProperties.ImageStatus), true));
            if (xmlRpcStruct.containsKey(IrsProperties.size)) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.get(IrsProperties.size).toString()) * 512);
            }
            if (xmlRpcStruct.containsKey("capacity")) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.get("capacity").toString()));
            }
            if (xmlRpcStruct.containsKey("truesize")) {
                newImage.setActualSizeInBytes(Long.parseLong(xmlRpcStruct.get("truesize").toString()));
            }
            if (xmlRpcStruct.containsKey("ctime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.get("ctime").toString());
                newImage.setCreationDate(makeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.containsKey("mtime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.get("mtime").toString());
                newImage.setLastModifiedDate(makeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.containsKey("domain")) {
                newImage.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(xmlRpcStruct.get("domain").toString()))));
            }
            if (xmlRpcStruct.containsKey("image")) {
                newImage.setId(new Guid(xmlRpcStruct.get("image").toString()));
            }
            if (xmlRpcStruct.containsKey("type")) {
                newImage.setVolumeType(EnumUtils.valueOf(VolumeType.class, xmlRpcStruct.get("type").toString(),
                        true));
            }
            if (xmlRpcStruct.containsKey("format")) {
                newImage.setVolumeFormat(EnumUtils.valueOf(VolumeFormat.class, xmlRpcStruct.get("format")
                        .toString(), true));
            }
            // TODO: change as leaseState is added to vdsm.
            if (xmlRpcStruct.containsKey("leaseStateTemp")) {
                newImage.getImage().setLeaseState(EnumUtils.valueOf(LeaseState.class, xmlRpcStruct.get("leaseState")
                        .toString(), true));
            }

            if (xmlRpcStruct.containsKey("generation")) {
                newImage.getImage().setGeneration(Integer.valueOf(xmlRpcStruct.get("generation").toString()));
            }
        } catch (RuntimeException ex) {
            log.error("Failed building DiskImage: {}", ex.getMessage());
            printReturnValue();
            log.debug("Exception", ex);
            newImage = null;
        }

        return newImage;
    }

}
