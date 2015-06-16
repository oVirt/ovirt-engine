package org.ovirt.engine.core.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class VdsStaticDAODbFacadeImpl extends AbstractJpaDao<VdsStatic, Guid> implements VdsStaticDAO {


    public VdsStaticDAODbFacadeImpl() {
        super(VdsStatic.class);
    }

    @Override
    public VdsStatic getByHostName(String host) {
        return singleResult(entityManager.createNamedQuery("VdsStatic.getByHostName", VdsStatic.class)
                .setParameter("hostName", host));
    }

    @Override
    public List<VdsStatic> getAllWithIpAddress(String address) {
        return multipleResults(entityManager.createNativeQuery("select * from GetVdsStaticByIp(:ip)",
                VdsStatic.class)
                .setParameter("ip", address));
    }

    @Override
    public List<VdsStatic> getAllForVdsGroup(Guid vdsGroup) {
        return multipleResults(entityManager.createNamedQuery("VdsStatic.getAllForVdsGroup",
                VdsStatic.class)
                .setParameter("vdsGroupId", vdsGroup));
    }

    @Override
    public void save(VdsStatic vds) {
        Guid id = vds.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            vds.setId(id);
        }
        super.save(vds);
    }

    @Override
    public VdsStatic getByVdsName(String vdsName) {
        return singleResult(entityManager.createNamedQuery("VdsStatic.getByVdsName",
                VdsStatic.class)
                .setParameter("name", vdsName));
    }
}
