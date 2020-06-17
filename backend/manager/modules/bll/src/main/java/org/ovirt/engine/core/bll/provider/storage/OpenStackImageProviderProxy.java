package org.ovirt.engine.core.bll.provider.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.openstack.CustomizedRESTEasyConnector;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.Injector;

import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.glance.model.v2.Image;
import com.woorea.openstack.glance.model.v2.ImageDownload;
import com.woorea.openstack.glance.model.v2.Images;
import com.woorea.openstack.glance.v2.Glance;

public class OpenStackImageProviderProxy extends AbstractOpenStackStorageProviderProxy<Glance, OpenStackImageProviderProperties, GlanceProviderValidator> {

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

    private static final String API_VERSION_V1 = "/v1";

    private static final String API_VERSION_V2 = "/v2";

    private static final String API_VERSION = API_VERSION_V2;

    private static final int QCOW2_SIGNATURE = 0x514649fb;

    private static final int QCOW2_SIZE_OFFSET = 24;

    public OpenStackImageProviderProxy(Provider<OpenStackImageProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    protected String getTestUrlPath() {
        return "/images";
    }

    @Override
    public void onAddition() {
        addStorageDomain(StorageType.GLANCE, StorageDomainType.Image);
    }

    public static OpenStackImageProviderProxy getFromStorageDomainId(Guid storageDomainId,
            ProviderProxyFactory providerProxyFactory) {
        StorageDomainStatic storageDomainStatic = Injector.get(StorageDomainStaticDao.class).get(storageDomainId);
        if (storageDomainStatic != null) {
            Provider<?> provider = Injector.get(ProviderDao.class).get(new Guid(storageDomainStatic.getStorage()));
            return providerProxyFactory.create(provider);
        }
        return null;
    }

    @Override
    protected Glance getClient() {
        if (client == null) {
            client = new Glance(getProvider().getUrl() + API_VERSION,  new CustomizedRESTEasyConnector());
            if (getProvider().isRequiringAuthentication()) {
                setClientTokenProvider(client);
            }
        }
        return client;
    }

    protected void validateContainerFormat(Image glanceImage) {
        if (!glanceImage.getContainerFormat().equals(GlanceImageContainer.BARE.getValue())) {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNSUPPORTED_CONTAINER_FORMAT,
                    "Unsupported container format: " + glanceImage.getContainerFormat());
        }
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

    public List<RepoImage> getAllImagesAsRepoImages(Integer listSize, Integer totalListSize) {
        ArrayList<RepoImage> repoImages = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        Images images = null;

        do {
            OpenStackRequest<Images> listRequest = getClient().images()
                    .list(true)
                    .queryParam("limit", listSize)
                    .queryParam("sort_key", "name")
                    .queryParam("sort_dir", "asc");

            if (images != null) {
                listRequest.queryParam("marker",
                        images.getList().get(images.getList().size() - 1).getId());
            }

            images = listRequest.execute();

            for (Image glanceImage : images) {
                RepoImage repoImage = imageToRepoImage(glanceImage);
                repoImage.setLastRefreshed(currentTime);
                repoImages.add(repoImage);
            }
        } while((images.getList().size() >= listSize) &&
                (totalListSize != null && repoImages.size() < totalListSize));

        return repoImages;
    }

    public DiskImage getImageAsDiskImage(String id) {
        DiskImage diskImage = new DiskImage();
        Image glanceImage;
        try {
            glanceImage = getClient().images().show(id).execute();
        } catch (OpenStackResponseException e) {
            log.debug("Exception:", e);
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.IMAGE_NOT_FOUND,
                    "Cannot find the specified image.");
        } catch (RuntimeException rte) {
            log.error("Exception:", rte);
            throw new RuntimeException("Failed to import from the repository.");
        }

        validateContainerFormat(glanceImage);

        String shortHash = glanceImage.getId().substring(0, 7);
        if (glanceImage.getName() != null) {
            diskImage.setDiskDescription(glanceImage.getName() + " (" + shortHash + ")");
        } else {
            diskImage.setDiskDescription("Glance disk: " + shortHash);
        }

        setDiskAttributes(diskImage, glanceImage);

        if (glanceImage.getDiskFormat().equals(GlanceImageFormat.RAW.getValue())) {
            diskImage.setVolumeFormat(VolumeFormat.RAW);
        } else if (glanceImage.getDiskFormat().equals(GlanceImageFormat.COW.getValue())) {
            diskImage.setVolumeFormat(VolumeFormat.COW);
        } else {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNSUPPORTED_DISK_FORMAT,
                    "Unknown disk format: " + glanceImage.getDiskFormat());
        }

        return diskImage;
    }

    public String createImageFromDiskImage(DiskImage diskImage) {
        Image glanceImage = new Image();

        glanceImage.setName(diskImage.getDiskAlias());

        if (diskImage.getVolumeFormat() == VolumeFormat.RAW) {
            glanceImage.setDiskFormat(GlanceImageFormat.RAW.getValue());
        } else if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
            glanceImage.setDiskFormat(GlanceImageFormat.COW.getValue());
        } else {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNSUPPORTED_DISK_FORMAT,
                    "Unknown disk format: " + diskImage.getVolumeFormat());
        }

        glanceImage.setContainerFormat(GlanceImageContainer.BARE.getValue());

        Image retGlanceImage = getClient().images().create(glanceImage).execute();

        return retGlanceImage.getId();
    }

    private void setCowVirtualSizeAndQcowCompat(DiskImage diskImage, Image glanceImage) {
        // For the qcow2 format we need to download the image header and read the virtual size from there
        byte[] imgContent = new byte[72];
        ImageDownload downloadImage = getClient().images().download(glanceImage.getId()).execute();

        try (InputStream inputStream = downloadImage.getInputStream()) {
            int bytesRead = inputStream.read(imgContent, 0, imgContent.length);
            if (bytesRead != imgContent.length) {
                throw new OpenStackImageException(
                        OpenStackImageException.ErrorType.UNABLE_TO_DOWNLOAD_IMAGE,
                        "Unable to read image header: " + bytesRead);
            }
        } catch (IOException e) {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNABLE_TO_DOWNLOAD_IMAGE,
                    "Unable to download image");
        }

        ByteBuffer b = ByteBuffer.wrap(imgContent);

        int qcow2Signature = b.getInt();
        int qcow2Version = b.getInt();
        QcowCompat qcowCompat = QcowCompat.forQcowHeaderVersion(qcow2Version);
        if (qcow2Signature == QCOW2_SIGNATURE && qcowCompat != null && qcowCompat != QcowCompat.Undefined) {
            b.position(QCOW2_SIZE_OFFSET);
            diskImage.setSize(b.getLong());
            diskImage.setQcowCompat(qcowCompat);
        } else {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNRECOGNIZED_IMAGE_FORMAT,
                    "Unable to recognize QCOW2 format");
        }
    }

    protected void setDiskAttributes(DiskImage diskImage, Image glanceImage) {
        setImageVirtualSizeAndCompat(diskImage, glanceImage);
        diskImage.setActualSizeInBytes(glanceImage.getSize());
    }

    protected void setImageVirtualSizeAndCompat(DiskImage diskImage, Image glanceImage) {
        validateContainerFormat(glanceImage);

        if (glanceImage.getDiskFormat().equals(GlanceImageFormat.RAW.getValue())
                || glanceImage.getDiskFormat().equals(GlanceImageFormat.ISO.getValue())) {
            diskImage.setSize(glanceImage.getSize());
        } else if (glanceImage.getDiskFormat().equals(GlanceImageFormat.COW.getValue())) {
            setCowVirtualSizeAndQcowCompat(diskImage, glanceImage);
        } else {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNSUPPORTED_DISK_FORMAT,
                    "Unknown disk format: " + glanceImage.getDiskFormat());
        }
    }

    public Map<String, String> getDownloadHeaders() {
        Map<String, String> httpHeaders = new HashMap<>();

        if (getTokenProvider() != null) {
            httpHeaders.put("X-Auth-Token", getTokenProvider().getToken());
        }

        return httpHeaders;
    }

    public Map<String, String> getUploadHeaders() {
        Map<String, String> httpHeaders = getDownloadHeaders();
        httpHeaders.put("Content-Type", "application/octet-stream");
        return httpHeaders;
    }

    public String getImageUrl(String id) {
        return getImageUrl(API_VERSION_V1, id);
    }

    public String getImageUrl(Version compatibilityVersion, String id) {
        return getImageUrl(getOpenStackImageServiceApiVersion(compatibilityVersion), id);
    }

    private String getImageUrl(String apiVersion, String id) {
        return removeTrailingSlash(getProvider().getUrl()) + apiVersion  + "/images/" + id;
    }

    private String removeTrailingSlash(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    private String getOpenStackImageServiceApiVersion(Version compatibilityVersion) {
        if (FeatureSupported.isOpenStackImageServiceApiV2Supported(compatibilityVersion)) {
            return API_VERSION_V2;
        }
        return API_VERSION_V1;
    }

    @Override
    public GlanceProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = Injector.injectMembers(new GlanceProviderValidator(provider));
        }
        return providerValidator;
    }
}
