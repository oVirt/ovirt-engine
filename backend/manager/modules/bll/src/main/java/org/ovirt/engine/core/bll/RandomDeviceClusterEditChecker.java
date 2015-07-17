package org.ovirt.engine.core.bll;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

@Singleton
public class RandomDeviceClusterEditChecker extends AbstractRngDeviceClusterEditChecker {

    protected RandomDeviceClusterEditChecker() {
        super(VmRngDevice.Source.RANDOM);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_HOST_DUE_TO_UNSUPPORTED_RANDOM_DEVICE_SOURCE.name();
    }
}
