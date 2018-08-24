package org.ovirt.engine.core.dao.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
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
        additionalProperties.setUserDomainName("userTomain");
        additionalProperties.setProjectName("browject");
        additionalProperties.setProjectDomainName("browjjectTomain");
        additionalProperties.setPluginType(OpenstackNetworkPluginType.OPEN_VSWITCH.name());
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
        return FixturesTool.PROVIDER_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 3;
    }

    @Test
    public void getByName() {
        assertEquals(FixturesTool.PROVIDER_NAME, dao.getByName(FixturesTool.PROVIDER_NAME).getName());
    }

    @Test
    public void getByNameCaseSensitive() {
        assertNull(dao.getByName(FixturesTool.PROVIDER_NAME.toUpperCase()));
    }

    @Test
    public void getByNameNonExistant() {
        assertNull(dao.getByName(FixturesTool.PROVIDER_NAME + FixturesTool.PROVIDER_NAME));
    }

    @Test
    public void searchQueryByExistentName() {
        assertEquals(FixturesTool.PROVIDER_NAME,
                dao.getAllWithQuery(String.format("SELECT * FROM providers WHERE name = '%s'",
                        FixturesTool.PROVIDER_NAME)).get(0).getName());
    }

    @Test
    public void searchQueryByNonExistentName() {
        assertTrue(dao.getAllWithQuery("SELECT * FROM providers WHERE name = 'foo'").isEmpty());
    }

    @Test
    public void searchQueryByExistentType() {
        assertEquals(FixturesTool.PROVIDER_NAME,
                dao.getAllWithQuery(String.format("SELECT * FROM providers WHERE provider_type = '%s'",
                        FixturesTool.PROVIDER_TYPE.name())).get(0).getName());
    }

    @Test
    public void searchQueryByNonExistentType() {
        assertTrue(dao.getAllWithQuery("SELECT * FROM providers WHERE provider_type = 'foo'").isEmpty());
    }
}
