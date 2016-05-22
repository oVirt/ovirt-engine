package org.ovirt.engine.core.bll.provider.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.glance.Glance;
import com.woorea.openstack.glance.model.Image;
import com.woorea.openstack.glance.model.ImageDownload;
import com.woorea.openstack.glance.model.Images;

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

    private static final String API_VERSION = "/v1";

    private static final int QCOW2_SIGNATURE = 0x514649fb;

    private static final int QCOW2_VERSION = 2;

    private static final int QCOW2_SIZE_OFFSET = 24;

    public OpenStackImageProviderProxy(Provider<OpenStackImageProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        return null;
    }

    @Override
    public void onAddition() {
        addStorageDomain(StorageType.GLANCE, StorageDomainType.Image);
    }

    public static OpenStackImageProviderProxy getFromStorageDomainId(Guid storageDomainId) {
        StorageDomainStatic storageDomainStatic = getDbFacade().getStorageDomainStaticDao().get(storageDomainId);
        if (storageDomainStatic != null) {
            Provider<?> provider = getDbFacade().getProviderDao().get(new Guid(storageDomainStatic.getStorage()));
            return ProviderProxyFactory.getInstance().create(provider);
        }
        return null;
    }

    @Override
    protected Glance getClient() {
        if (client == null) {
            client = new Glance(getProvider().getUrl() + API_VERSION);
            client.setTokenProvider(getTokenProvider());
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
        Image glanceImage = getClient().images().show(id).execute();

        validateContainerFormat(glanceImage);

        String shortHash = glanceImage.getId().substring(0, 7);
        if (glanceImage.getName() != null) {
            diskImage.setDiskDescription(glanceImage.getName() + " (" + shortHash + ")");
        } else {
            diskImage.setDiskDescription("Glance disk: " + shortHash);
        }

        diskImage.setSize(getImageVirtualSize(glanceImage));
        diskImage.setActualSizeInBytes(glanceImage.getSize());

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

    private long getCowVirtualSize(String id) {
        // For the qcow2 format we need to download the image header and read the virtual size from there
        byte[] imgContent = new byte[72];
        ImageDownload downloadImage = getClient().images().download(id).execute();

        try {
            int bytesRead = downloadImage.getInputStream().read(imgContent, 0, imgContent.length);
            if (bytesRead != imgContent.length) {
                throw new OpenStackImageException(
                        OpenStackImageException.ErrorType.UNABLE_TO_DOWNLOAD_IMAGE,
                        "Unable to read image header: " + bytesRead);
            }
        } catch (IOException e) {
            throw new OpenStackImageException(
                    OpenStackImageException.ErrorType.UNABLE_TO_DOWNLOAD_IMAGE,
                    "Unable to download image");
        } finally {
            try {
                downloadImage.getInputStream().close();
            } catch (IOException | IllegalStateException e) {
                // Silently skip IOException and IllegalStateException errors
            }
        }

        ByteBuffer b = ByteBuffer.wrap(imgContent);

        if (b.getInt() == QCOW2_SIGNATURE && b.getInt() == QCOW2_VERSION) {
            b.position(QCOW2_SIZE_OFFSET);
            return b.getLong();
        }

        throw new OpenStackImageException(
                OpenStackImageException.ErrorType.UNRECOGNIZED_IMAGE_FORMAT,
                "Unable to recognize QCOW2 format");
    }

    protected long getImageVirtualSize(Image glanceImage) {
        validateContainerFormat(glanceImage);

        if (glanceImage.getDiskFormat().equals(GlanceImageFormat.RAW.getValue())
                || glanceImage.getDiskFormat().equals(GlanceImageFormat.ISO.getValue())) {
            return glanceImage.getSize();
        } else if (glanceImage.getDiskFormat().equals(GlanceImageFormat.COW.getValue())) {
            return getCowVirtualSize(glanceImage.getId());
        }

        throw new OpenStackImageException(
                OpenStackImageException.ErrorType.UNSUPPORTED_DISK_FORMAT,
                "Unknown disk format: " + glanceImage.getDiskFormat());
    }

    public Map<String, String> getDownloadHeaders() {
        HashMap<String, String> httpHeaders = new HashMap<>();

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
        return getProvider().getUrl() + API_VERSION  + "/images/" + id;
    }

    @Override
    public GlanceProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = new GlanceProviderValidator(provider);
        }
        return providerValidator;
    }
}
