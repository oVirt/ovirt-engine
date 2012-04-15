package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map_id;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.vmpools.TimeLeaseVmPoolMapDAOHibernateImpl;
import org.ovirt.engine.core.dao.vmpools.VmPoolMapDAOHibernateImpl;

public class VmPoolDAOHibernateImpl extends BaseDAOHibernateImpl<vm_pools, NGuid> implements VmPoolDAO {
    private VmPoolMapDAOHibernateImpl vmPoolMapDAO = new VmPoolMapDAOHibernateImpl();
    private TimeLeaseVmPoolMapDAOHibernateImpl timeLeaseVmPoolDAO = new TimeLeaseVmPoolMapDAOHibernateImpl();

    public VmPoolDAOHibernateImpl() {
        super(vm_pools.class);
    }

    @Override
    public vm_pools get(NGuid id, Guid userID, boolean isFiltered) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        vmPoolMapDAO.setSession(session);
        timeLeaseVmPoolDAO.setSession(session);
    }

    @Override
    public void removeVmFromVmPool(Guid vm) {
        vmPoolMapDAO.remove(vm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pools> getAllForUser(Guid userid) {
        List<vm_pools> result = new ArrayList<vm_pools>();

        /* we need to get the user, then get the list of ad groups for that user */
        Query query = getSession().createQuery("from DbUser where id = :user_id");

        query.setParameter("user_id", userid);

        DbUser user = (DbUser) query.uniqueResult();

        if (user != null) {
            Criteria criteria =
                    getSession().createCriteria(ad_groups.class).add(Restrictions.in("name",
                            splitApartNames(user.getgroups())));

            List<ad_groups> adElements = criteria.list();
            Guid[] ids = new Guid[adElements.size() + 1];

            ids[0] = userid;
            for (int index = 0; index < adElements.size(); index++) {
                ids[index + 1] = adElements.get(index).getid();
            }

            query = getSession().createQuery("select pool " +
                    "from vm_pools pool, permissions perms, roles role " +
                    "where pool.id = perms.objectId " +
                    "and perms.adElementId in ( :adElements ) " +
                    "and perms.roleId = role.id " +
                    "and role.type = 2");

            query.setParameterList("adElements", ids);

            result = query.list();
        }

        return result;
    }

    private static String[] splitApartNames(String text) {
        List<String> names = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(text, ",", false);

        while (tokens.hasMoreElements()) {
            names.add(tokens.nextToken());
        }

        String[] result = new String[names.size()];

        names.toArray(result);

        return result;
    }

    @Override
    public List<vm_pools> getAllForAdGroup(Guid adGroup) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<vm_pools> getAllWithQuery(String sql) {
        SQLQuery query = getSession().createSQLQuery(sql).addEntity(vm_pools.class);

        return query.list();
    }

    @Override
    public vm_pool_map getVmPoolMapByVmGuid(Guid vmId) {
        return vmPoolMapDAO.getByVmGuid(vmId);
    }

    @Override
    public void addVmToPool(vm_pool_map map) {
        vmPoolMapDAO.save(map);
    }

    @Override
    public List<vm_pool_map> getVmPoolsMapByVmPoolId(NGuid vmPoolId) {
        return vmPoolMapDAO.getVmPoolsMapByVmPoolId(vmPoolId);
    }

    @Override
    public time_lease_vm_pool_map getTimeLeasedVmPoolMapByIdForVmPool(Guid id, NGuid vmPoolId) {
        return timeLeaseVmPoolDAO.get(new time_lease_vm_pool_map_id(id, new Guid(vmPoolId.getUuid())));
    }

    @Override
    public void addTimeLeasedVmPoolMap(time_lease_vm_pool_map map) {
        timeLeaseVmPoolDAO.save(map);
    }

    @Override
    public void updateTimeLeasedVmPoolMap(time_lease_vm_pool_map map) {
        timeLeaseVmPoolDAO.update(map);
    }

    @Override
    public void removeTimeLeasedVmPoolMap(Guid id, Guid vmPoolId) {
        timeLeaseVmPoolDAO.remove(new time_lease_vm_pool_map_id(id, vmPoolId));
    }

    @Override
    public List<time_lease_vm_pool_map> getAllTimeLeasedVmPoolMaps() {
        return timeLeaseVmPoolDAO.getAll();
    }

    @Override
    public List<vm_pool_map> getVmMapsInVmPoolByVmPoolIdAndStatus(NGuid vmPoolId, VMStatus vmStatus) {
        // TODO Auto-generated method stub
        return null;
    }
}
