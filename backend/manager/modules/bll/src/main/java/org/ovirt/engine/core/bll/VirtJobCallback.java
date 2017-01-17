package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;

public class VirtJobCallback extends HostJobCallback {

    @Override
    protected HostJobType getHostJobType() {
        return HostJobType.virt;
    }

}
