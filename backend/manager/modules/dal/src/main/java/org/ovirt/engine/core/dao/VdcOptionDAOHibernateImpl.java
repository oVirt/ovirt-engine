package org.ovirt.engine.core.dao;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.VdcOption;

public class VdcOptionDAOHibernateImpl extends BaseDAOHibernateImpl<VdcOption, Integer> implements VdcOptionDAO {
    public VdcOptionDAOHibernateImpl() {
        super(VdcOption.class);
    }

    @Override
    public VdcOption getByNameAndVersion(String name, String version) {
        return findOneByCriteria(Restrictions.eq("name", name),
                Restrictions.eq("version", version));
    }

    @Override
    public VdcOption get(int id) {
        return super.get(Integer.valueOf(id));
    }

    @Override
    public void remove(int id) {
        super.remove(Integer.valueOf(id));
    }

}
