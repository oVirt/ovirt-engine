package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CreateVolumeContainerVDSCommand<P extends CreateVolumeVDSCommandParameters> extends
        StorageJobVDSCommand<P> {

    public CreateVolumeContainerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeJobCommand: calling 'createVolumeContainer'");

        status = getBroker().createVolumeContainer(getParameters().getJobId().toString(), prepareVolumeInfo());

        proceedProxyReturnValue();

        setReturnValue(getParameters().getNewImageID().toString());
    }

    private Map<String, Object> prepareVolumeInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("sd_id", getParameters().getStorageDomainId().toString());
        info.put("img_id", getParameters().getNewImageGroupId().toString());
        info.put("vol_id", getParameters().getNewImageID().toString());
        info.put("virtual_size", (Long.valueOf(getParameters().getImageSizeInBytes())).toString());
        info.put("vol_format", getParameters().getVolumeFormat().name().toUpperCase());
        info.put("disk_type", DiskContentType.DATA.name());
        info.put("description", getParameters().getDescription());
        info.put("parent_img_id", getParameters().getSrcImageGroupId() != null ? getParameters().getSrcImageGroupId()
                .toString()
                : Guid.Empty.toString());
        info.put("parent_vol_id", getParameters().getSrcImageId() != null ? getParameters().getSrcImageId().toString()
                : Guid.Empty.toString());
        if (getParameters().getInitialSize() != null) {
            info.put("initial_size", getParameters().getInitialSize());
        }
        return info;
    }
}
