package org.ovirt.engine.core.bll.hostedengine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.RetrieveImageDataParameters;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.archivers.tar.TarInMemoryExport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostedEngineConfigFetcher {

    public static final String HOSTED_ENGINE_CONFIGURATION_IMAGE = "HostedEngineConfigurationImage";
    private static final String HOSTED_ENGINE_CONF = "hosted-engine.conf";
    private static final Logger log = LoggerFactory.getLogger(HostedEngineConfigFetcher.class);

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private Instance<HostedEngineHelper> heHelper;

    @Inject
    private BackendInternal backend;
    /**
     * Retrieve the hosted engine configuration from the configuration disk on the hosted engine domain.
     * The disk contains the config in a key value format
     *
     * @return A {@link CompletableFuture} holding the configuration of the hosted engine
     * agent as kept on the shared storage
     */
    public CompletableFuture<Map<String, String>> fetchPromise() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> configFromImage = Collections.emptyMap();
            final Guid spId = heHelper.get().getStoragePoolId();
            final Guid sdId = heHelper.get().getStorageDomainId();
            final Guid hostId = heHelper.get().getRunningHostId();

            // Get Images List for the HE domain
            final List<Guid> diskIds = getHEDomainImages(spId, sdId);
            // Get all volumes for each image and trace the volume by a certain description
            final Optional<DiskImage> configDisk = traceConfigurationDisk(spId, sdId, diskIds);
            // If exist, download its content. It holds all the configuration that was saved during the
            // install of the 1st hosted engine host.
            if (configDisk.isPresent()) {
                final byte[] diskData = downloadDisk(spId, sdId, configDisk.get());
                if (diskData != null) {
                    // the content of the disk is a tar which contains the hosted-engine.conf file
                    configFromImage = extractFileFromDisk(configFromImage, diskData);
                }
            }
            return configFromImage;
        });
    }

    /**
     * Retrieve the hosted engine configuration from the configuration disk on the hosted engine domain.
     * The disk contains the config in a key value format
     *
     * @return The configuration of the hosted engine agent as kept on the shared storage
     */
    public Map<String, String> fetch() {
        try {
            return fetchPromise().get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch the hosted engine config disk due to {}", e);
        }
        return Collections.emptyMap();
    }

    private List<Guid> getHEDomainImages(Guid spId, Guid sdId) {
        return (List<Guid>) resourceManager.runVdsCommand(
                VDSCommandType.GetImagesList,
                new GetImagesListVDSCommandParameters(sdId, spId)
        ).getReturnValue();
    }

    private Optional<DiskImage> traceConfigurationDisk(Guid spId, Guid sdId, List<Guid> diskIds) {
        if (diskIds == null) {
            return Optional.empty();
        }
        return diskIds.stream()
                .map(diskId -> new Pair<>(diskId, (List<Guid>) resourceManager.runVdsCommand(
                        VDSCommandType.GetVolumesList,
                        new StoragePoolDomainAndGroupIdBaseVDSCommandParameters(spId, sdId, diskId))
                        .getReturnValue()))
                .flatMap(diskToVolumes -> diskToVolumes.getSecond().stream()
                        .map(volumeId -> getImageInfo(spId, sdId, diskToVolumes.getFirst(), volumeId)))
                .filter(Objects::nonNull)
                .map(diskImageCall -> (DiskImage)diskImageCall.getReturnValue())
                .filter(Objects::nonNull)
                .filter(diskImage -> HOSTED_ENGINE_CONFIGURATION_IMAGE
                        .equals(diskImage.getDescription()))
                .findAny();
    }

    private VDSReturnValue getImageInfo(Guid spId, Guid sdId, Guid diskId, Guid volumeId) {
        return resourceManager.runVdsCommand(
                VDSCommandType.GetImageInfo,
                new GetImageInfoVDSCommandParameters(spId, sdId, diskId, volumeId));
    }

    private byte[] downloadDisk(Guid spId, Guid sdId, DiskImage diskImage) {
        long downloadSize = Config.<Integer>getValue(ConfigValues.HostedEngineConfigDiskSizeInBytes);
        log.info("Found the HE configuration disk. Downloading the content in size of {} bytes", downloadSize);
        VdcReturnValueBase returnValue = backend.runInternalAction(VdcActionType.RetrieveImageData,
                new RetrieveImageDataParameters(spId,
                        sdId,
                        diskImage.getId(),
                        diskImage.getImageId(),
                        downloadSize));
        if (returnValue.getSucceeded()) {
            return (byte[]) returnValue.getActionReturnValue();
        } else {
            throw new EngineException(EngineError.ENGINE, "Failed to download the HE configuration disk");
        }
    }

    private Map<String, String> extractFileFromDisk(Map<String, String> configFromImage, byte[] diskData) {
        log.info("Untar the HE config disk");
        try (TarInMemoryExport tar = new TarInMemoryExport(new ByteArrayInputStream(diskData))) {
            Optional<Map.Entry<String, ByteBuffer>> taredConfig = tar.unTar().entrySet().stream()
                    .peek(entry ->
                            log.debug("File name in HE config tar '{}'. Looking for '{}'", entry.getKey(), HOSTED_ENGINE_CONF))
                    .filter(entry -> HOSTED_ENGINE_CONF.equals(entry.getKey()))
                    .findAny();
            if (taredConfig.isPresent()) {
                configFromImage = loadConfigMap(taredConfig.get().getValue().array());
            }
        } catch (Exception e) {
            log.error("Failed to untar the hosted engine configuration disk due to {}", e);
        }
        return configFromImage;
    }

    /**
     * Take a array of data, parse it to a Map of key: String -> val: String
     * @param data byte[] loaded by the previously fetched file
     * @return A key value map of the hosted engine configuration 'as is' represented by the data passed or
     * {@linkplain Collections#emptyMap} in case some error occurred.
     */
    protected Map<String, String> loadConfigMap(final byte[] data) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue() ));
        } catch (IOException e) {
            log.error("Failed to load hosted-engine.conf map from file due to {}", e);
            return Collections.emptyMap();
        }
    }
}
