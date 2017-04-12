package org.ovirt.engine.core.bll;

import javax.enterprise.inject.Typed;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobType;

@Typed(StorageJobCallback.class)
public class StorageJobCallback extends HostJobCallback {

    @Override
    protected HostJobType getHostJobType() {
        return HostJobType.storage;
    }

}
