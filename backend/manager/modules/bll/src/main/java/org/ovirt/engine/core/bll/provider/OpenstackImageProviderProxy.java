package org.ovirt.engine.core.bll.provider;

import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.glance.Glance;
import com.woorea.openstack.glance.model.Image;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.OpenstackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;


public class OpenstackImageProviderProxy implements ProviderProxy {

    enum GlanceImageFormat {
        RAW("raw"),
        ISO("iso"),
        COW("qcow2");

        private String value;

        GlanceImageFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum GlanceImageContainer {
        BARE("bare");

        private String value;

        GlanceImageContainer(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final String API_VERSION = "/v1";

    private Provider<OpenstackImageProviderProperties> provider;

    private OpenStackTokenProvider tokenProvider;

    private Glance client;

    public OpenstackImageProviderProxy(Provider<OpenstackImageProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public void testConnection() {
        try {
            getClient().execute(new OpenStackRequest<>(getClient(), HttpMethod.GET, "", null, null));
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        return null;
    }

    private static DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    @Override
    public void onAddition() {
        // Storage domain static
        StorageDomainStatic domainStaticEntry = new StorageDomainStatic();
        domainStaticEntry.setId(Guid.newGuid());
        domainStaticEntry.setStorage(provider.getId().toString());
        domainStaticEntry.setStorageName(provider.getName());
        domainStaticEntry.setDescription(provider.getDescription());
        domainStaticEntry.setStorageFormat(StorageFormatType.V1);
        domainStaticEntry.setStorageType(StorageType.GLANCE);
        domainStaticEntry.setStorageDomainType(StorageDomainType.Image);
        getDbFacade().getStorageDomainStaticDao().save(domainStaticEntry);
        // Storage domain dynamic
        StorageDomainDynamic domainDynamicEntry = new StorageDomainDynamic();
        domainDynamicEntry.setId(domainStaticEntry.getId());
        domainDynamicEntry.setAvailableDiskSize(0);
        domainDynamicEntry.setUsedDiskSize(0);
        getDbFacade().getStorageDomainDynamicDao().save(domainDynamicEntry);
    }

    @Override
    public void onRemoval() {
        List<StorageDomain> storageDomains = getDbFacade()
                .getStorageDomainDao().getAllByConnectionId(provider.getId());

        // removing the static and dynamic storage domain entries
        for (StorageDomain storageDomainEntry : storageDomains) {
            getDbFacade().getStorageDomainDynamicDao().remove(storageDomainEntry.getId());
            getDbFacade().getStorageDomainStaticDao().remove(storageDomainEntry.getId());
        }
    }

    private Provider getProvider() {
        return provider;
    }

    private OpenStackTokenProvider getTokenProvider() {
        if (tokenProvider == null && getProvider().isRequiringAuthentication()) {
            String tenantName = provider.getAdditionalProperties().getTenantName();
            tokenProvider = new KeystoneTokenProvider(Config.<String> GetValue(ConfigValues.KeystoneAuthUrl),
                    getProvider().getUsername(), getProvider().getPassword()).getProviderByTenant(tenantName);
        }
        return tokenProvider;
    }

    private Glance getClient() {
        if (client == null) {
            client = new Glance(getProvider().getUrl() + API_VERSION);
            client.setTokenProvider(getTokenProvider());
        }
        return client;
    }

    private static RepoImage imageToRepoImage(Image glanceImage) {
        RepoImage repoImage = new RepoImage();

        repoImage.setSize(glanceImage.getSize());
        repoImage.setDateCreated(null);
        repoImage.setRepoImageId(glanceImage.getId());
        repoImage.setRepoImageName(glanceImage.getName());

        if (glanceImage.getContainerFormat() == null || glanceImage.getDiskFormat() == null
                || !glanceImage.getContainerFormat().equals(GlanceImageContainer.BARE.getValue())) {
            repoImage.setFileType(ImageFileType.Unknown);
        } else {
            if (glanceImage.getDiskFormat().equals(GlanceImageFormat.RAW.getValue())
                    || glanceImage.getDiskFormat().equals(GlanceImageFormat.COW.getValue())) {
                repoImage.setFileType(ImageFileType.Disk);
            } else if (glanceImage.getDiskFormat().equals(GlanceImageFormat.ISO.getValue())) {
                repoImage.setFileType(ImageFileType.ISO);
            } else {
                repoImage.setFileType(ImageFileType.Unknown);
            }
        }

        return repoImage;
    }

    public List<RepoImage> getAllImagesAsRepoImages() {
        ArrayList<RepoImage> repoImages = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        for (Image glanceImage : getClient().images().list(true).execute()) {
            RepoImage repoImage = imageToRepoImage(glanceImage);
            repoImage.setLastRefreshed(currentTime);
            repoImages.add(repoImage);
        }

        return repoImages;
    }

}
