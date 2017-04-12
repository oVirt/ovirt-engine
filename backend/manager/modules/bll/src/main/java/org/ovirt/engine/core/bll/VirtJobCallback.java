package org.ovirt.engine.core.bll;

import javax.enterprise.inject.Typed;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;

@Typed(VirtJobCallback.class)
public class VirtJobCallback extends HostJobCallback {

    @Override
    protected HostJobType getHostJobType() {
        return HostJobType.virt;
    }

}
