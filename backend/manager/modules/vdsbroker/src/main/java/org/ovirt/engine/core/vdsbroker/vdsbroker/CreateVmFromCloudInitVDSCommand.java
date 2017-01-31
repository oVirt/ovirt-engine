package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;

public class CreateVmFromCloudInitVDSCommand<P extends CreateVDSCommandParameters>
        extends CreateBrokerVDSCommand<P> {
    public CreateVmFromCloudInitVDSCommand(P parameters) throws Exception {
        super(parameters);

        CloudInitHandler cloudInitHandler = new CloudInitHandler(parameters.getVm().getVmInit());
        Map<String, byte[]> cloudInitContent;
        try {
            cloudInitContent = cloudInitHandler.getFileData();
        } catch (Exception e) {
            throw new Exception("Failed to build cloud-init data:", e);
        }

        if (cloudInitContent != null && !cloudInitContent.isEmpty()) {
            builder.buildCloudInitVmPayload(cloudInitContent);
        }
    }
}
