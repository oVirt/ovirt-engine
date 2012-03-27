package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.dao.vds.VdsDynamicDAOHibernateImpl;
import org.ovirt.engine.core.dao.vds.VdsStaticDAOHibernateImpl;
import org.ovirt.engine.core.dao.vds.VdsStatisticsDAOHibernateImpl;

public class VdsDAOWrapperImpl extends BaseDAOWrapperImpl implements VdsDAO {

    private final VdsStaticDAOHibernateImpl vdsStaticDAO = new VdsStaticDAOHibernateImpl();
    private final VdsDynamicDAOHibernateImpl vdsDynamicDAO = new VdsDynamicDAOHibernateImpl();
    private final VdsStatisticsDAOHibernateImpl vdsStatisticsDAO = new VdsStatisticsDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        vdsStaticDAO.setSession(session);
        vdsDynamicDAO.setSession(session);
        vdsStatisticsDAO.setSession(session);
    }

    @Override
    public VDS get(NGuid id) {
        Guid guid = new Guid(id.getUuid());

        VdsStatic staticPart = vdsStaticDAO.get(guid);
        VdsDynamic dynamicPart = vdsDynamicDAO.get(guid);
        VdsStatistics statisticsPart = vdsStatisticsDAO.get(guid);

        // if we didn't find one, then return null
        if (staticPart == null || dynamicPart == null || statisticsPart == null) {
            return null;
        }

        return new VDS(staticPart, dynamicPart, statisticsPart);
    }

    @Override
    public VDS get(NGuid id, Guid userID, boolean isFiltered) {
        throw new NotImplementedException();
    }

    private List<VDS> convertToVdsList(List<VdsStatic> found) {
        List<VDS> result = new ArrayList<VDS>();

        for (VdsStatic vdsStatic : found) {
            VdsDynamic vdsDynamic = vdsDynamicDAO.get(vdsStatic.getId());
            VdsStatistics vdsStatistics = vdsStatisticsDAO.get(vdsStatic.getId());

            result.add(new VDS(vdsStatic, vdsDynamic, vdsStatistics));
        }

        return result;
    }

    @Override
    public List<VDS> getAllWithName(String name) {
        List<VdsStatic> found = vdsStaticDAO.findByCriteria(Restrictions.eq("name", name));

        return convertToVdsList(found);
    }

    @Override
    public List<VDS> getAllForHostname(String hostname) {
        List<VdsStatic> found = vdsStaticDAO.findByCriteria(Restrictions.eq("hostname", hostname));

        return convertToVdsList(found);
    }

    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        List<VdsStatic> found = vdsStaticDAO.findByCriteria(Restrictions.eq("uniqueId", id));

        return convertToVdsList(found);
    }

    @Override
    public List<VDS> getAllOfType(VDSType vds) {
        List<VdsStatic> found = vdsStaticDAO.findByCriteria(Restrictions.eq("vdsType", vds.getValue()));

        return convertToVdsList(found);
    }

    @Override
    public List<VDS> getAllOfTypes(VDSType[] types) {
        Integer[] intTypes = new Integer[types.length];

        for (int index = 0; index < types.length; index++) {
            intTypes[index] = types[index].getValue();
        }

        List<VdsStatic> found = vdsStaticDAO.findByCriteria(Restrictions.in("vdsType", intTypes));

        return convertToVdsList(found);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllForVdsGroupWithoutMigrating(final Guid vdsGroup) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_vds_static_for_vds_group_without_migration");

        query.setParameter("vds_group_id", vdsGroup);

        return convertToVdsList(query.list());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VDS> getAllWithQuery(String sql) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);

        return query.list();
    }

    @Override
    public List<VDS> getAll() {
        List<VdsStatic> found = vdsStaticDAO.getAll();

        return convertToVdsList(found);
    }

    @Override
    public List<VDS> getAllWithIpAddress(String address) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VDS> getAllForVdsGroup(Guid vdsGroup) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VDS> getListForSpmSelection(Guid storagePoolId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VDS> listFailedAutorecoverables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VDS> getAllForVdsGroupWithStatus(Guid vdsGroupId, VDSStatus status) {
        // TODO Auto-generated method stub
        return null;
    }
}
