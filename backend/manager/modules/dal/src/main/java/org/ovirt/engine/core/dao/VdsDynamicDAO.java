package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsDynamicDAO</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
 */
public interface VdsDynamicDAO extends GenericDao<VdsDynamic, Guid>, StatusAwareDao<Guid, VDSStatus> {
}
