package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.QemuVolumeFormat;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;

public class GetQemuImageInfoVDSCommand<P extends GetVolumeInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private QemuImageInfoReturnForXmlRpc result;

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
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

    public QemuImageInfo buildImageEntity(Map<String, Object> xmlRpcStruct) {
        QemuImageInfo qemuImageInfo = new QemuImageInfo();
        try {
            qemuImageInfo.setImageId(getParameters().getImageId());
            qemuImageInfo.setImageGroupId(getParameters().getImageGroupId());
            qemuImageInfo.setStorageDomainId(getParameters().getStorageDomainId());
            qemuImageInfo.setStoragePoolId(getParameters().getStoragePoolId());
            if (xmlRpcStruct.containsKey("compat")) {
                qemuImageInfo.setQcowCompat(QcowCompat.forCompatValue(xmlRpcStruct.get("compat").toString()));
            }
            if (xmlRpcStruct.containsKey("format")) {
                qemuImageInfo.setQemuVolumeFormat(EnumUtils.valueOf(QemuVolumeFormat.class, xmlRpcStruct.get("format")
                        .toString(), true));
            }
            if (xmlRpcStruct.containsKey("backingfile")) {
                qemuImageInfo.setBackingFile(xmlRpcStruct.get("backingfile").toString());
            }
            if (xmlRpcStruct.containsKey("virtualsize")) {
                qemuImageInfo.setSize(Long.parseLong(xmlRpcStruct.get("virtualsize").toString()));
            }
            if (xmlRpcStruct.containsKey("clustersize")) {
                qemuImageInfo.setClusterSize(Long.parseLong(xmlRpcStruct.get("clustersize").toString()));
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
