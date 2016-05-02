package org.ovirt.engine.core.dao.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class ProviderDaoTest extends BaseGenericDaoTestCase<Guid, Provider<?>, ProviderDao> {

    @Override
    protected Provider<?> generateNewEntity() {
        Provider<AdditionalProperties> provider = new Provider<>();
        provider.setId(generateNonExistingId());
        provider.setName("brovider");
        provider.setUrl("http://brovider.com/");
        provider.setType(ProviderType.OPENSTACK_NETWORK);
        OpenstackNetworkProviderProperties additionalProperties = new OpenstackNetworkProviderProperties();
        additionalProperties.setReadOnly(Boolean.FALSE);
        additionalProperties.setTenantName("10ant");
        additionalProperties.setPluginType(OpenstackNetworkPluginType.LINUX_BRIDGE.name());
        provider.setAdditionalProperties(additionalProperties);
        provider.setAuthUrl("http://keystone-server:35357/v2.0/");
        return provider;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setUrl("abc");
    }

    @Override
    protected Guid getExistingEntityId() {
        return new Guid("1115c1c6-cb15-4832-b2a4-023770607111");
    }

    @Override
    protected ProviderDao prepareDao() {
        return dbFacade.getProviderDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 2;
    }

    @Test
    public void getByName() throws Exception {
        assertEquals(FixturesTool.PROVIDER_NAME, dao.getByName(FixturesTool.PROVIDER_NAME).getName());
    }

    @Test
    public void getByNameCaseSensitive() throws Exception {
        assertNull(dao.getByName(FixturesTool.PROVIDER_NAME.toUpperCase()));
    }

    @Test
    public void getByNameNonExistant() throws Exception {
        assertNull(dao.getByName(FixturesTool.PROVIDER_NAME + FixturesTool.PROVIDER_NAME));
    }

    @Test
    public void searchQueryByExistentName() throws Exception {
        assertEquals(FixturesTool.PROVIDER_NAME,
                dao.getAllWithQuery(String.format("SELECT * FROM providers WHERE name = '%s'",
                        FixturesTool.PROVIDER_NAME)).get(0).getName());
    }

    @Test
    public void searchQueryByNonExistentName() throws Exception {
        assertTrue(dao.getAllWithQuery("SELECT * FROM providers WHERE name = 'foo'").isEmpty());
    }

    @Test
    public void searchQueryByExistentType() throws Exception {
        assertEquals(FixturesTool.PROVIDER_NAME,
                dao.getAllWithQuery(String.format("SELECT * FROM providers WHERE provider_type = '%s'",
                        FixturesTool.PROVIDER_TYPE.name())).get(0).getName());
    }

    @Test
    public void searchQueryByNonExistentType() throws Exception {
        assertTrue(dao.getAllWithQuery("SELECT * FROM providers WHERE provider_type = 'foo'").isEmpty());
    }
}
