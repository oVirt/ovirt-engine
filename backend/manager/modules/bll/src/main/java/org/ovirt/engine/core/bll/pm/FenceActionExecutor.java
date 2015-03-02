package org.ovirt.engine.core.bll.pm;

import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;

/**
 * Provides method to execute specified fence action
 */
public interface FenceActionExecutor {
    /**
     * Execute fence action
     *
     * @param actionType
     *            fence action type
     * @return result of fence action
     */
    public FenceOperationResult fence(FenceActionType actionType);
}
