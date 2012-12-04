package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;

public interface IVmPoolResolutionService
{
    vm_pools ResolveVmPoolById(Guid id);
}
