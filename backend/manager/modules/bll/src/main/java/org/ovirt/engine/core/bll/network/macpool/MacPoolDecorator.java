package org.ovirt.engine.core.bll.network.macpool;

import org.ovirt.engine.core.compat.Guid;

public interface MacPoolDecorator extends MacPool {
    void setMacPool(MacPool macPool);

    void setMacPoolId(Guid macPoolId);
}
