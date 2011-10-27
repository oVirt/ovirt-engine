package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNetworkInterfaceDAO</code> defines a type for performing CRUD operations on instances of
 * {@link VmNetworkInterface}.
 */
public interface VmNetworkInterfaceDAO extends GenericDao<VmNetworkInterface, Guid> {
    /**
     * Retrieves all interfaces for the given VM id.
     *
     * @param id
     *            the Vm id
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForVm(Guid id);

    /**
     * Retrieves all interfaces for the given template id.
     *
     * @param id
     *            the template id
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForTemplate(Guid id);
}
