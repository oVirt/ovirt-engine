/**
 *
 */
package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.collections.KeyValue;

import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.compat.Guid;

/**
 * @author yzaslavs
 *
 */
public class BusinessEntitySnapshotDAOHibernateImpl implements BusinessEntitySnapshotDAO {

    /**
     *
     */
    public BusinessEntitySnapshotDAOHibernateImpl() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO#get(org.ovirt.engine.core.compat.Guid)
     */
       /* (non-Javadoc)
     * @see org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO#getAllForCommandId(org.ovirt.engine.core.compat.Guid)
     */
    @Override
    public List<BusinessEntitySnapshot> getAllForCommandId(Guid commandID) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO#removeAllForCommandId(org.ovirt.engine.core.compat.Guid)
     */
    @Override
    public void removeAllForCommandId(Guid commandID) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO#save(org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot)
     */
    @Override
    public void save(BusinessEntitySnapshot entitySnapshot) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<KeyValue> getAllCommands() {
        // TODO Auto-generated method stub
        return null;
    }

}
