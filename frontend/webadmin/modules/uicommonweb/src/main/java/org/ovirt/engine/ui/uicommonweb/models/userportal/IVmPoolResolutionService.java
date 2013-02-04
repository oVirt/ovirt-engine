package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;

public interface IVmPoolResolutionService
{
    VmPool ResolveVmPoolById(Guid id);
}
