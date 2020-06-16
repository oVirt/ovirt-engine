package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProperties;

public class GetVolumeInfoVDSCommand<P extends GetVolumeInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    private VolumeInfoReturn result;

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
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

    private static Date makeDTFromCTime(long ctime) {
        return new Date(ctime * 1000L);
    }

    public DiskImage buildImageEntity(Map<String, Object> struct) {
        DiskImage newImage = new DiskImage();
        try {
            newImage.setImageId(new Guid((String) struct.get(IrsProperties.uuid)));

            newImage.setParentId(new Guid((String) struct.get(IrsProperties.parent)));
            newImage.setDescription((String) struct.get(IrsProperties.description));
            newImage.setImageStatus(EnumUtils.valueOf(ImageStatus.class,
                    (String) struct.get(IrsProperties.ImageStatus), true));
            if (struct.containsKey(IrsProperties.size)) {
                newImage.setSize(Long.parseLong(struct.get(IrsProperties.size).toString()) * 512);
            }
            if (struct.containsKey("capacity")) {
                newImage.setSize(Long.parseLong(struct.get("capacity").toString()));
            }
            if (struct.containsKey("truesize")) {
                newImage.setActualSizeInBytes(Long.parseLong(struct.get("truesize").toString()));
            }
            if (struct.containsKey("apparentsize")) {
                newImage.setApparentSizeInBytes(Long.parseLong(struct.get("apparentsize").toString()));
            }
            if (struct.containsKey("ctime")) {
                long secsSinceEpoch = Long.parseLong(struct.get("ctime").toString());
                newImage.setCreationDate(makeDTFromCTime(secsSinceEpoch));
            }
            if (struct.containsKey("domain")) {
                newImage.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(struct.get("domain").toString()))));
            }
            if (struct.containsKey("image")) {
                newImage.setId(new Guid(struct.get("image").toString()));
            }
            if (struct.containsKey("type")) {
                newImage.setVolumeType(EnumUtils.valueOf(VolumeType.class, struct.get("type").toString(),
                        true));
            }
            if (struct.containsKey("format")) {
                newImage.setVolumeFormat(EnumUtils.valueOf(VolumeFormat.class, struct.get("format")
                        .toString(), true));
            }
            if (struct.containsKey("disktype")) {
                newImage.setContentType(DiskContentType.forStorageValue((String) struct.get("disktype")));
            }

            if (struct.containsKey("lease") && struct.get("lease") != null) {
                Map<String, Object> leaseStatus = (Map<String, Object>) struct.get("lease");
                newImage.getImage().setLeaseStatus(vdsBrokerObjectsBuilder.buildLeaseStatus(leaseStatus));
            }

            if (struct.containsKey("generation")) {
                newImage.getImage().setGeneration(Integer.valueOf(struct.get("generation").toString()));
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
