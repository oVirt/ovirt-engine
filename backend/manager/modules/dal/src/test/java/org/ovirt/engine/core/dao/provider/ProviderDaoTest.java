package org.ovirt.engine.core.dao.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;

public class ProviderDaoTest extends BaseGenericDaoTestCase<Guid, Provider, ProviderDao> {

    @Override
    protected Provider generateNewEntity() {
        Provider provider = new Provider();
        provider.setId(generateNonExistingId());
        provider.setName("brovider");
        provider.setUrl("http://brovider.com/");
        provider.setType(ProviderType.OPENSTACK_NETWORK);
        return provider;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setUrl("abc");
    }

    @Override
    protected Guid getExistingEntityId() {
        return Guid.createGuidFromString("1115c1c6-cb15-4832-b2a4-023770607111");
    }

    @Override
    protected ProviderDao prepareDao() {
        return dbFacade.getProviderDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.NewGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 1;
    }
}
