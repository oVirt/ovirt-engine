package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;

public interface EntityPollingCommand {
    HostJobStatus poll();
}
