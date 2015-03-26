package org.ovirt.engine.core.bll.provider.storage;

import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.Limits;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.VolumeType;
import com.woorea.openstack.cinder.model.VolumeTypes;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.storage.CINDERStorageHelper;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

public class OpenStackVolumeProviderProxy extends AbstractOpenStackStorageProviderProxy<Cinder, OpenStackVolumeProviderProperties, CinderProviderValidator> {

    private static final String API_VERSION = "/v2/";

    public OpenStackVolumeProviderProxy(Provider<OpenStackVolumeProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        return null;
    }

    @Override
    public void onAddition() {
        Guid storageDomainId = addStorageDomain(StorageType.CINDER, StorageDomainType.Volume);
        Guid storagePoolId = provider.getAdditionalProperties().getStoragePoolId();
        if (!storagePoolId.equals(Guid.Empty)) {
            attachStorageDomainToDataCenter(storageDomainId, storagePoolId);
        }
    }

    protected void attachStorageDomainToDataCenter(Guid storageDomainId, Guid storagePoolId) {
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        CINDERStorageHelper.setRunInNewTransaction(false);
        CINDERStorageHelper.attachCinderDomainToPool(storageDomainId, storagePoolId);
        CINDERStorageHelper.activateCinderDomain(storageDomainId, storagePoolId);
    }

    protected Cinder getClient() {
        return getClient("");
    }

    protected Cinder getClient(String tenantId) {
        if (client == null) {
            client = new Cinder(getProvider().getUrl().concat(API_VERSION).concat(tenantId));
            client.setTokenProvider(getTokenProvider());
        }
        return client;
    }

    public String createVolume(VolumeForCreate volumeForCreate) {
        Volume retCinderVolume = getClient(getTenantId()).volumes().create(volumeForCreate).execute();
        return retCinderVolume.getId();
    }

    public Volume getVolumeById(String id) {
        return getClient(getTenantId()).volumes().show(id).execute();
    }

    public Limits getLimits() {
        return getClient(getTenantId()).limits().list().execute();
    }

    public List<CinderVolumeType> getVolumeTypes() {
        ArrayList<CinderVolumeType> cinderVolumeTypes = new ArrayList<>();
        OpenStackRequest<VolumeTypes> listRequest = getClient(getTenantId()).volumeTypes().list();

        VolumeTypes volumeTypes = listRequest.execute();
        for (VolumeType volumeType : volumeTypes) {
            CinderVolumeType cinderVolumeType = new CinderVolumeType(
                    volumeType.getId(), volumeType.getName(), volumeType.getExtraSpecs());
            cinderVolumeTypes.add(cinderVolumeType);
        }
        return cinderVolumeTypes;
    }

    @Override
    public void onRemoval() {
        List<StorageDomain> storageDomains = getDbFacade().getStorageDomainDao().getAllByConnectionId(provider.getId());

        // Removing the static and dynamic storage domain entries
        StorageDomain storageDomainEntry = storageDomains.get(0);
        getDbFacade().getStorageDomainDao().remove(storageDomainEntry.getId());
    }

    public static OpenStackVolumeProviderProxy getFromStorageDomainId(Guid storageDomainId) {
        StorageDomainStatic storageDomainStatic = getDbFacade().getStorageDomainStaticDao().get(storageDomainId);
        if (storageDomainStatic != null) {
            return getProviderFromStorageDomainStatic(storageDomainStatic);
        }
        return null;
    }

    public static OpenStackVolumeProviderProxy getFromStorageDomainId(Guid storageDomainId, Guid userID, boolean isFiltered) {
        StorageDomain storageDomain = getDbFacade().getStorageDomainDao().get(storageDomainId, userID, isFiltered);
        if (storageDomain != null) {
            Provider provider = getDbFacade().getProviderDao().get(new Guid(storageDomain.getStorage()));
            return ProviderProxyFactory.getInstance().create(provider);
        }
        return null;
    }

    private static OpenStackVolumeProviderProxy getProviderFromStorageDomainStatic(StorageDomainStatic storageDomainStatic) {
        Provider provider = getDbFacade().getProviderDao().get(new Guid(storageDomainStatic.getStorage()));
        return ProviderProxyFactory.getInstance().create(provider);
    }

    @Override
    public CinderProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = new CinderProviderValidator(provider);
        }
        return providerValidator;
    }
}
