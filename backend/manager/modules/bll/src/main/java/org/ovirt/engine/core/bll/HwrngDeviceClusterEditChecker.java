package org.ovirt.engine.core.bll;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

@Singleton
public class HwrngDeviceClusterEditChecker extends AbstractRngDeviceClusterEditChecker {

    protected HwrngDeviceClusterEditChecker() {
        super(VmRngDevice.Source.HWRNG);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_HOST_DUE_TO_UNSUPPORTED_HWRNG_SOURCE.name();
    }
}
