package org.ovirt.engine.core.bll.provider.storage;

import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.TenantProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackClient;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;

public abstract class AbstractOpenStackStorageProviderProxy<C extends OpenStackClient, T extends TenantProviderProperties, V extends ProviderValidator> implements ProviderProxy<V> {

    protected C client;

    protected Provider<T> provider;

    protected OpenStackTokenProvider tokenProvider;

    protected KeystoneTokenProvider keystoneTokenProvider;

    protected StorageDomain storageDomain;

    protected V providerValidator;

    private static Logger log = LoggerFactory.getLogger(AbstractOpenStackStorageProviderProxy.class);

    @Override
    public void testConnection() {
        try {
            getClient().execute(new OpenStackRequest<>(getClient(), HttpMethod.GET, "", null, null));
        } catch (OpenStackResponseException e) {
            log.error("{} (OpenStack response error code: {})", e.getMessage(), e.getStatus());
            log.debug("Exception", e);
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        } catch (RuntimeException e) {
            log.debug("Exception", e);
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    protected abstract C getClient();

    protected Provider getProvider() {
        return provider;
    }

    protected OpenStackTokenProvider getTokenProvider() {
        if (tokenProvider == null && getProvider().isRequiringAuthentication()) {
            String tenantName = provider.getAdditionalProperties().getTenantName();
            tokenProvider = getKeystoneTokenProvider().getProviderByTenant(tenantName);
        }
        return tokenProvider;
    }

    protected KeystoneTokenProvider getKeystoneTokenProvider() {
        if (keystoneTokenProvider == null) {
            keystoneTokenProvider = new KeystoneTokenProvider(getProvider().getAuthUrl(),
                    getProvider().getUsername(), getProvider().getPassword());
        }
        return keystoneTokenProvider;
    }

    protected Access getAccess() {
        String tenantName = provider.getAdditionalProperties().getTenantName();
        return getKeystoneTokenProvider().getAccessByTenant(tenantName);
    }

    protected String getTenantId() {
        return getAccess().getToken().getTenant().getId();
    }

    protected Guid addStorageDomain(StorageType storageType, StorageDomainType storageDomainType) {
        // Storage domain static
        StorageDomainStatic domainStaticEntry = new StorageDomainStatic();
        domainStaticEntry.setId(Guid.newGuid());
        domainStaticEntry.setStorage(provider.getId().toString());
        domainStaticEntry.setStorageName(provider.getName());
        domainStaticEntry.setDescription(provider.getDescription());
        domainStaticEntry.setStorageFormat(StorageFormatType.V1);
        domainStaticEntry.setStorageType(storageType);
        domainStaticEntry.setStorageDomainType(storageDomainType);
        domainStaticEntry.setWipeAfterDelete(false);
        domainStaticEntry.setDiscardAfterDelete(false);
        Injector.get(StorageDomainStaticDao.class).save(domainStaticEntry);
        // Storage domain dynamic
        StorageDomainDynamic domainDynamicEntry = new StorageDomainDynamic();
        domainDynamicEntry.setId(domainStaticEntry.getId());
        Injector.get(StorageDomainDynamicDao.class).save(domainDynamicEntry);
        return domainStaticEntry.getId();
    }

    @Override
    public void onModification() {
        // updating storage domain information
        Guid storageDomainId = getProviderStorageDomain().getId();
        StorageDomainStatic domainStaticEntry = Injector.get(StorageDomainStaticDao.class).get(storageDomainId);
        domainStaticEntry.setStorageName(provider.getName());
        domainStaticEntry.setDescription(provider.getDescription());
        Injector.get(StorageDomainStaticDao.class).update(domainStaticEntry);
    }

    @Override
    public void onRemoval() {
        List<StorageDomain> storageDomains =
                Injector.get(StorageDomainDao.class).getAllByConnectionId(provider.getId());

        // removing the static and dynamic storage domain entries
        StorageDomain storageDomainEntry = storageDomains.get(0);
        Injector.get(StorageDomainDynamicDao.class).remove(storageDomainEntry.getId());
        Injector.get(StorageDomainStaticDao.class).remove(storageDomainEntry.getId());
    }

    protected StorageDomain getProviderStorageDomain() {
        if (storageDomain == null) {
            List<StorageDomain> storageDomains =
                    Injector.get(StorageDomainDao.class).getAllByConnectionId(provider.getId());
            storageDomain = storageDomains.get(0);
        }
        return storageDomain;
    }
}
