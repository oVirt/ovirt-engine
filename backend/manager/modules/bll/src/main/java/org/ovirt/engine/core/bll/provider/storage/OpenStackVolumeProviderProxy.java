package org.ovirt.engine.core.bll.provider.storage;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.ConnectionForInitialize;
import com.woorea.openstack.cinder.model.ConnectionInfo;
import com.woorea.openstack.cinder.model.Limits;
import com.woorea.openstack.cinder.model.Snapshot;
import com.woorea.openstack.cinder.model.SnapshotForCreate;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.VolumeForUpdate;
import com.woorea.openstack.cinder.model.VolumeType;
import com.woorea.openstack.cinder.model.VolumeTypes;

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
        if (storagePoolId != null && !storagePoolId.equals(Guid.Empty)) {
            attachStorageDomainToDataCenter(storageDomainId, storagePoolId);
        }
    }

    protected void attachStorageDomainToDataCenter(Guid storageDomainId, Guid storagePoolId) {
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        CINDERStorageHelper.setRunInNewTransaction(false);
        CINDERStorageHelper.attachCinderDomainToPool(storageDomainId, storagePoolId);
        CINDERStorageHelper.activateCinderDomain(storageDomainId, storagePoolId);
    }

    @Override
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

    public String createSnapshot(SnapshotForCreate snapshotForCreate) {
        Snapshot retCinderSnapshot = getClient(getTenantId()).snapshots().create(snapshotForCreate).execute();
        return retCinderSnapshot.getId();
    }

    public void deleteSnapshot(String snapshotId) {
        getClient(getTenantId()).snapshots().delete(snapshotId).execute();
    }

    public String cloneVolumeFromSnapshot(VolumeForCreate volumeForCreate) {
        Volume retCinderVolume = getClient(getTenantId()).volumes().create(volumeForCreate).execute();
        return retCinderVolume.getId();
    }

    public String createVolume(VolumeForCreate volumeForCreate) {
        Volume retCinderVolume = getClient(getTenantId()).volumes().create(volumeForCreate).execute();
        return retCinderVolume.getId();
    }

    public void deleteVolume(String volumeId) {
        getClient(getTenantId()).volumes().delete(volumeId).execute();
    }

    public void updateVolume(String volumeId, VolumeForUpdate volumeForUpdate) {
        getClient(getTenantId()).volumes().update(volumeId, volumeForUpdate).execute();
    }

    public void extendVolume(String volumeId, int newSize) {
        getClient(getTenantId()).volumes().extend(volumeId, newSize).execute();
    }

    public CinderConnectionInfo initializeConnectionForVolume(String volumeId, ConnectionForInitialize connectionForInitialize) {
        ConnectionInfo connectionInfo = getClient(getTenantId()).volumes().initializeConnection(volumeId, connectionForInitialize).execute();
        CinderConnectionInfo cinderConnectionInfo = new CinderConnectionInfo();
        cinderConnectionInfo.setDriverVolumeType(connectionInfo.getDriverVolumeType());
        cinderConnectionInfo.setData(connectionInfo.getData());
        return cinderConnectionInfo;
    }

    public Volume getVolumeById(String id) {
        return getClient(getTenantId()).volumes().show(id).execute();
    }

    public Snapshot getSnapshotById(String id) {
        return getClient(getTenantId()).snapshots().show(id).execute();
    }

    public Limits getLimits() {
        return getClient(getTenantId()).limits().list().execute();
    }

    public List<Volume> getVolumes() {
        return getClient(getTenantId()).volumes().list(true).execute().getList();
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
