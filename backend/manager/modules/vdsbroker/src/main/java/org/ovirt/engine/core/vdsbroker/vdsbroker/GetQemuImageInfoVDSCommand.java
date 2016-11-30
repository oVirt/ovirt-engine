package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.QemuVolumeFormat;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;

public class GetQemuImageInfoVDSCommand<P extends GetVolumeInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private QemuImageInfoReturn result;

    public GetQemuImageInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GetVolumeInfoVDSCommandParameters params = getParameters();
        result = getBroker().getQemuImageInfo(
                params.getStorageDomainId().toString(),
                params.getStoragePoolId().toString(),
                params.getImageGroupId().toString(),
                params.getImageId().toString());
        proceedProxyReturnValue();
        setReturnValue(buildImageEntity(result.getQemuImageInfo()));
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

    public QemuImageInfo buildImageEntity(Map<String, Object> struct) {
        QemuImageInfo qemuImageInfo = new QemuImageInfo();
        try {
            qemuImageInfo.setImageId(getParameters().getImageId());
            qemuImageInfo.setImageGroupId(getParameters().getImageGroupId());
            qemuImageInfo.setStorageDomainId(getParameters().getStorageDomainId());
            qemuImageInfo.setStoragePoolId(getParameters().getStoragePoolId());
            if (struct.containsKey("compat")) {
                qemuImageInfo.setQcowCompat(QcowCompat.forCompatValue(struct.get("compat").toString()));
            }
            if (struct.containsKey("format")) {
                qemuImageInfo.setQemuVolumeFormat(EnumUtils.valueOf(QemuVolumeFormat.class, struct.get("format")
                        .toString(), true));
            }
            if (struct.containsKey("backingfile")) {
                qemuImageInfo.setBackingFile(struct.get("backingfile").toString());
            }
            if (struct.containsKey("virtualsize")) {
                qemuImageInfo.setSize(Long.parseLong(struct.get("virtualsize").toString()));
            }
            if (struct.containsKey("clustersize")) {
                qemuImageInfo.setClusterSize(Long.parseLong(struct.get("clustersize").toString()));
            }
        } catch (RuntimeException ex) {
            log.error("Failed building Qemu image: {}", ex.getMessage());
            printReturnValue();
            log.debug("Exception", ex);
            qemuImageInfo = null;
        }
        return qemuImageInfo;
    }

}
