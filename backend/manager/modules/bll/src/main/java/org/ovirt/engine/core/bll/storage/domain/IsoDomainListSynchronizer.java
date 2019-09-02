package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.mutable.MutableLong;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetFileStatsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.RepoFileMetaDataDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class manages the Iso domain cache mechanism, <BR/>
 * which reflects upon support for Iso tool validation, activation of Iso domain, and fetching the Iso list by query.<BR/>
 * The cache is being refreshed with quartz scheduler which run by configuration value AutoRepoDomainRefreshTime. The
 * cache procedure using VDSM to fetch the Iso files from all the Data Centers and update the DB cache table with the
 * appropriate file data.<BR/>
 */
@SuppressWarnings("synthetic-access")
@Singleton
public class IsoDomainListSynchronizer implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(IsoDomainListSynchronizer.class);

    private static final String ISO_VDSM_FILE_PATTERN = "*.iso";
    private static final Pattern ISO_FILE_PATTERN_REGEX = Pattern.compile("^.*\\.iso$", Pattern.CASE_INSENSITIVE);
    private static final String FLOPPY_VDSM_FILE_PATTERN = "*.vfd";
    private static final Pattern FLOPPY_FILE_PATTERN_REGEX = Pattern.compile("^.*\\.vfd$", Pattern.CASE_INSENSITIVE);
    private static final String ALL_FILES_PATTERN = "*";

    private ConcurrentMap<Guid, MutableLong> domainsLastRefreshedTime = new ConcurrentHashMap<>();

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    private RepoFileMetaDataDao repoFileMetaDataDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Inject
    private VmHandler vmHandler;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    private final ConcurrentMap<Object, Lock> syncDomainForFileTypeMap = new ConcurrentHashMap<>();

    public static final String TOOL_CLUSTER_LEVEL = "clusterLevel";
    public static final String TOOL_VERSION = "toolVersion";

    // Not kept as static member to enable reloading the config value
    public String getGuestToolsSetupIsoPrefix() {
        return Config.getValue(ConfigValues.GuestToolsSetupIsoPrefix);
    }

    public String getRegexToolPattern() {
        return String.format("%1$s(?<%2$s>[0-9]{1,}.[0-9])[_|-]{1}(?<%3$s>[0-9]{1,})[.\\w]*.[i|I][s|S][o|O]$",
                getGuestToolsSetupIsoPrefix(),
                TOOL_CLUSTER_LEVEL,
                TOOL_VERSION);
    }

    /**
     * Returns a RepoFilesMetaData list with Iso file names for storage domain Id and with file type extension.<BR>
     * If user choose to refresh the cache, and a problem occurs, then throws EngineException.
     *
     * @param storageDomainId
     *            - The storage domain Id, which we fetch the Iso list from.
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @param forceRefresh
     *            - Indicates if the domain should be refreshed from VDSM.
     * @return List of RepoFilesMetaData files.
     * @throws EngineException
     *             - if a problem occurs when refreshing the image repo cache.
     */
    public List<RepoImage> getUserRequestForStorageDomainRepoFileList(Guid storageDomainId,
            ImageFileType imageType,
            Boolean forceRefresh) {
        // Query for storageDoaminId is looking for Active ISO domain
        if (!isStorageDomainIdValid(storageDomainId)) {
            throw new EngineException(EngineError.GetIsoListError);
        }

        refreshReposIfNeeded(storageDomainId, imageType, forceRefresh);

        // In any case, whether refreshed or not, get Iso list from the cache.
        return getCachedIsoListByDomainId(storageDomainId, imageType);
    }

    private void refreshReposIfNeeded(Guid storageDomainId, ImageFileType imageType, Boolean forceRefresh) {
        MutableLong lastRefreshed = domainsLastRefreshedTime.computeIfAbsent(storageDomainId, k -> new MutableLong(-1));

        if (shouldForceRefresh(forceRefresh) || shouldInvalidateCache(lastRefreshed.longValue())) {
            synchronized (lastRefreshed) {
                // Double check as another thread might have already finished a refresh and released the lock
                if (shouldForceRefresh(forceRefresh) || shouldInvalidateCache(lastRefreshed.longValue())) {
                    boolean refreshSucceeded = refreshRepos(storageDomainId, imageType);
                    lastRefreshed.setValue(System.currentTimeMillis());
                    if (!refreshSucceeded) {
                        throw new EngineException(EngineError.IMAGES_NOT_SUPPORTED_ERROR);
                    }
                }
            }
        }
    }

    private boolean shouldForceRefresh(Boolean forceRefresh) {
        return Boolean.TRUE.equals(forceRefresh) || (forceRefresh == null && getShouldForceRefreshByDefault());
    }

    private boolean shouldInvalidateCache(long lastRefreshed) {
        return System.currentTimeMillis() > lastRefreshed + getInvalidateCachePeriodFromConfig();
    }

    private boolean refreshRepos(Guid storageDomainId, ImageFileType imageType) {
        boolean refreshResult;
        List<RepoImage> tempProblematicRepoFileList = new ArrayList<>();
        StorageDomain storageDomain = storageDomainDao.get(storageDomainId);

        if (storageDomain.getStorageDomainType() == StorageDomainType.ISO) {
            refreshResult = refreshIsoDomain(storageDomainId, tempProblematicRepoFileList, imageType);
        } else if (storageDomain.getStorageDomainType() == StorageDomainType.Image &&
                storageDomain.getStorageType() == StorageType.GLANCE) {
            refreshResult = refreshImageDomain(storageDomain, imageType);
        } else {
            log.error("Unable to refresh the storage domain '{}', Storage Domain Type '{}' not supported",
                    storageDomainId, storageDomain.getStorageDomainType());
            return false;
        }

        handleErrorLog(tempProblematicRepoFileList);

        // If refresh succeeded update the audit log
        if (refreshResult) {
            addToAuditLogSuccessMessage(storageDomain, imageType.name());
        }

        return refreshResult;
    }

    private boolean refreshImageDomain(final StorageDomain storageDomain, final ImageFileType imageType) {
        Provider provider = providerDao.get(new Guid(storageDomain.getStorage()));
        final OpenStackImageProviderProxy client = providerProxyFactory.create(provider);

        Lock syncObject = getSyncObject(storageDomain.getId(), imageType);
        try {
            syncObject.lock();
            return TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                    () -> {
                        repoFileMetaDataDao.removeRepoDomainFileList(storageDomain.getId(), imageType);

                        Integer totalListSize = Config.<Integer> getValue(ConfigValues.GlanceImageTotalListSize);
                        List<RepoImage> repoImages = client.getAllImagesAsRepoImages(
                                Config.<Integer> getValue(ConfigValues.GlanceImageListSize), totalListSize);

                        if (repoImages.size() >= totalListSize) {
                            AuditLogable logable = new AuditLogableImpl();
                            logable.addCustomValue("imageDomain", storageDomain.getName())
                                    .addCustomValue("imageListSize", String.valueOf(repoImages.size()));
                            logable.setStorageDomainId(storageDomain.getId());
                            logable.setStorageDomainName(storageDomain.getName());
                            auditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_INCOMPLETE);
                        }

                        for (RepoImage repoImage : repoImages) {
                            repoImage.setRepoDomainId(storageDomain.getId());
                            repoFileMetaDataDao.addRepoFileMap(repoImage);
                        }

                        return true;
                    });
        } finally {
            syncObject.unlock();
        }
    }

    /**
     * The procedure Try to refresh the repository files of the storage domain id, with storage pool Id. If succeeded
     * will return True, otherwise return false and update the list, of the problematic repository files with the
     * storage pool and storage domain id, that could not complete the cache update transaction.
     *
     * @param storageDomainId
     *            - The Repository domain Id, we want to refresh.
     * @param storagePoolId
     *            - The Storage pool Id, we use to fetch the Iso files from..
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @return Boolean value indicating if the refresh succeeded or not.
     */
    private boolean refreshIsoDomainFileForStoragePool(Guid storageDomainId,
            Guid storagePoolId,
            ImageFileType imageType) {

        // Setting the indication to the indication whether the storage pool is valid.
        boolean updateFromVDSMSucceeded = updateFileList(storageDomainId, storagePoolId, imageType);

        // Log if the refresh succeeded or add the storage domain to the problematic list.
        if (updateFromVDSMSucceeded) {
            log.debug("Refresh succeeded for file type '{}' at storage domain id '{}' in storage pool id '{}'.",
                    imageType.name(),
                    storageDomainId,
                    storagePoolId);
        }

        return updateFromVDSMSucceeded;
    }

    private boolean updateFileList(Guid storageDomainId, Guid storagePoolId, ImageFileType imageType) {
        switch (imageType) {
        case All:
            return updateAllFileListFromVDSM(storagePoolId, storageDomainId);
        case ISO:
            return updateIsoListFromVDSM(storagePoolId, storageDomainId);
        case Floppy:
            return updateFloppyListFromVDSM(storagePoolId, storageDomainId);
        case Unknown:
            return updateUnknownFileListFromVDSM(storagePoolId, storageDomainId);
        default:
            log.warn("Refreshing Iso domain using unsupported imageType: {}", imageType);
            return false;
        }
    }

    /**
     * The procedure Try to refresh the repository files of the storage domain id, By iterate over the storage pools of
     * this domain, and try to choose a valid storage pool, to fetch the repository files from the VDSM, and refresh the
     * cached table. <BR/>
     * If succeeded, will return True. Otherwise return false with updated list of problematic repository files with the
     * storage pool, storage domain, and file type, that could not complete the cache update transaction.
     *
     * @param storageDomainId
     *            - The Repository domain Id, we want to refresh.
     * @param problematicRepoFileList
     *            - List of business entities, each one indicating the problematic entity.
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @return Boolean value indicating if the refresh succeeded or not.
     */
    private boolean refreshIsoDomain(Guid storageDomainId,
            List<RepoImage> problematicRepoFileList,
            ImageFileType imageType) {
        List<StoragePoolIsoMap> isoMapList = fetchAllStoragePoolsForIsoDomain(storageDomainId, imageType);

        for (StoragePoolIsoMap storagePoolIsoMap : isoMapList) {
            Guid storagePoolId = storagePoolIsoMap.getStoragePoolId();
            StorageDomainStatus status = storagePoolIsoMap.getStatus();

            if (StorageDomainStatus.Active != status) {
                handleInactiveStorageDomain(storageDomainId, imageType, status);
            } else {
                // Try to refresh the domain of the storage pool id because its status is active.
                boolean refreshOk = refreshIsoDomainFileForStoragePool(storageDomainId, storagePoolId, imageType);
                if (!refreshOk) {
                    log.debug("Failed refreshing Storage domain id '{}', for '{}' file type in storage pool id '{}'.",
                            storageDomainId,
                            imageType,
                            storagePoolId);

                    // Add the repository file to the list of problematic Iso domains.
                    RepoImage repoImage = createMockRepositoryFileMetaData(storageDomainId, imageType, storagePoolId);
                    problematicRepoFileList.add(repoImage);
                    return false;
                }
            }
        }

        return true;
    }

    // Fetch all the Storage pools for this Iso domain Id.
    private List<StoragePoolIsoMap> fetchAllStoragePoolsForIsoDomain(Guid storageDomainId, ImageFileType imageType) {
        List<StoragePoolIsoMap> isoMapList = storagePoolIsoMapDao.getAllForStorage(storageDomainId);
        log.debug("Fetched {} storage pools for '{}' file type, in Iso domain '{}'.",
                isoMapList.size(),
                imageType,
                storageDomainId);
        return isoMapList;
    }

    // set a mock repository file meta data with storage domain id and storage pool id.
    private static RepoImage createMockRepositoryFileMetaData(Guid storageDomainId,
            ImageFileType imageType,
            Guid storagePoolId) {
        RepoImage repoImage = new RepoImage();
        repoImage.setStoragePoolId(storagePoolId);
        repoImage.setRepoDomainId(storageDomainId);
        repoImage.setFileType(imageType);
        return repoImage;
    }

    private void handleInactiveStorageDomain(Guid storageDomainId,
            ImageFileType imageType,
            StorageDomainStatus status) {
        log.debug("Storage domain id '{}', is not active, and therefore could not be refreshed for '{}'"
                        + " file type (Iso domain status is '{}').",
                storageDomainId,
                imageType,
                status);
    }

    /**
     * Refresh the Iso domain when activating the domain,
     * with executing a new Thread to prevent long lock status for the domain.
     *
     * @param isoStorageDomainId
     *            - The storage domain Id we want to get the file list from.
     * @param storagePoolId
     *            - The storage pool Id we get an Iso active domain, we want to get the file list from (used mainly for log issues).
     */
    public void refresheIsoDomainWhenActivateDomain(final Guid isoStorageDomainId,
            final Guid storagePoolId) {
        if (storagePoolId != null && (isoStorageDomainId != null)) {
            ThreadPoolUtil.execute(() -> refreshActivatedStorageDomainFromVdsm(storagePoolId, isoStorageDomainId));
        }
    }

    /**
     * Returns the cached Iso file meta data list, for storage domain.
     *
     * @param isoStorageDomainId
     *            - The storage domain Id we want to get the file list from.
     * @return List of Iso files fetched from DB, if parameter is invalid returns an empty list.
     */
    public List<RepoImage> getCachedIsoListByDomainId(Guid isoStorageDomainId,
            ImageFileType imageType) {
        List<RepoImage> fileListMD = new ArrayList<>();
        if (isoStorageDomainId != null) {
            fileListMD =
                    repoFileMetaDataDao.getRepoListForStorageDomain(isoStorageDomainId, imageType);
        }
        return fileListMD;
    }

    /**
     * Print information on the problematic storage domain. Mainly transfer the business entity to list, for handling
     * the error uniformly.
     * Create a mock RepoImage object in a list, to use the functionality of the handleErrorLog with list.
     *
     * @param storagePoolId
     *            - The storage pool Id.
     * @param storageDomainId
     *            - The storage domain Id.
     * @param imageType
     *            - The file type extension (ISO  or Floppy).
     * @see #handleErrorLog(List)
     */
    private void handleErrorLog(Guid storagePoolId, Guid storageDomainId, ImageFileType imageType) {
        List<RepoImage> tempProblematicRepoFileList = new ArrayList<>();

        RepoImage repoImage = createMockRepositoryFileMetaData(
                storageDomainId,
                imageType,
                storagePoolId);

        // Add the repository file to the list, and use handleError.
        tempProblematicRepoFileList.add(repoImage);
        handleErrorLog(tempProblematicRepoFileList);
    }

    /**
     * Print information on the problematic storage domains and print an audit log.<BR/>
     * If the problematicFileListForHandleError list retrieved empty or null,<BR/>
     * then don't do nothing and return false flag.
     *
     * @param problematicFileListForHandleError
     *            - List of repository file meta data, each one indicating a problematic repository domain.
     * @return true, if has problematic storage domains, false otherwise (List is empty).
     */
    private boolean handleErrorLog(List<RepoImage> problematicFileListForHandleError) {
        boolean hasProblematic = false;
        if (problematicFileListForHandleError != null && !problematicFileListForHandleError.isEmpty()) {
            StringBuilder problematicStorages = new StringBuilder();
            StringBuilder problematicIsoDomainsForAuditLog = new StringBuilder();
            Set<String> storageDomainNames = new HashSet<>();
            for (RepoImage repoMap : problematicFileListForHandleError) {
                problematicStorages.append(buildDetailedProblematicMapMsg(repoMap));
                storageDomainNames.add(buildDetailedAuditLogMessage(repoMap));
            }

            // Build Audit log message with problematic domains.
            for (String domainName : storageDomainNames) {
                problematicIsoDomainsForAuditLog.append("  ").append(domainName);
            }

            hasProblematic = true;
            log.error("The following storage domains had a problem retrieving data from VDSM: {}",
                    problematicStorages);
            addToAuditLogErrorMessage(problematicIsoDomainsForAuditLog.toString());
        }
        return hasProblematic;
    }

    /**
     * Returns a string builder contains problematic repoImage details.
     *
     * @param repoImage
     *            - The problematic storage domain.
     */
    private static StringBuilder buildDetailedProblematicMapMsg(RepoImage repoImage) {
        StringBuilder problematicStorageMsg = new StringBuilder();
        if (repoImage != null) {
            problematicStorageMsg.append(" (");
            if (repoImage.getStoragePoolId() != null) {
                problematicStorageMsg.append(" Storage Pool Id: ").append(repoImage.getStoragePoolId());
            }
            if (repoImage.getRepoDomainId() != null) {
                problematicStorageMsg.append(" Storage domain Id: ").append(repoImage.getRepoDomainId());
            }
            problematicStorageMsg.append(" File type: ").append(repoImage.getFileType()).append(") ");
        } else {
            problematicStorageMsg.append("(A repository file meta data business entity, has null value) ");
        }
        return problematicStorageMsg;
    }

    /**
     * Returns String contains problematic iso domain name for audit log message.
     * @param repoImage
     *            - The problematic storage domain.
     */
    private String buildDetailedAuditLogMessage(RepoImage repoImage) {
        String storageDomainName = "Repository not found";
        if (repoImage != null && repoImage.getRepoDomainId() != null) {
            StorageDomain storageDomain =
                    storageDomainDao.get(repoImage.getRepoDomainId());
            if (storageDomain != null) {
                storageDomainName =
                        String.format("%s (%s file type)",
                                storageDomain.getStorageName(),
                                repoImage.getFileType().name());
            }
        } else {
            log.error("Repository file meta data not found for logging");
        }
        return storageDomainName;
    }

    private boolean refreshIsoFileListMetaData(final Guid repoStorageDomainId,
                                               final Map<String, Map<String, Object>> fileStats,
                                               final ImageFileType imageType) {
        Lock syncObject = getSyncObject(repoStorageDomainId, imageType);
        try {
            syncObject.lock();
            return (Boolean) TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            long currentTime = System.currentTimeMillis();
                            repoFileMetaDataDao.removeRepoDomainFileList(repoStorageDomainId, imageType);
                            for (Map.Entry<String, Map<String, Object>> entry : fileStats.entrySet()) {
                                repoFileMetaDataDao.addRepoFileMap(newRepoImage(currentTime, entry));
                            }
                            return true;
                        }

                        public RepoImage newRepoImage(long currentTime, Map.Entry<String, Map<String, Object>> entry) {
                            RepoImage repo_md = new RepoImage();
                            repo_md.setLastRefreshed(currentTime);
                            repo_md.setSize(retrieveIsoFileSize(entry));
                            repo_md.setRepoDomainId(repoStorageDomainId);
                            repo_md.setDateCreated(null);
                            repo_md.setRepoImageId(entry.getKey());
                            repo_md.setRepoImageName(null);
                            repo_md.setFileType(imageType);
                            return repo_md;
                        }
                    });
        } catch (Exception e) {
            log.warn("Updating repository content to DB failed for repoStorageDomainId={}, imageType={}: {}",
                    repoStorageDomainId,
                    imageType,
                    e.getMessage());
            log.debug("Exception", e);
            return false;
        } finally {
            syncObject.unlock();
        }
    }

    private static Long retrieveIsoFileSize(Map.Entry<String, Map<String, Object>> fileStats) {
        try {
            Object fileSize = fileStats.getValue().get(VdsProperties.size);
            if (fileSize == null) {
                return null;
            }
            return Long.valueOf((String) fileStats.getValue().get(VdsProperties.size));
        } catch (RuntimeException e) {
            // Illegal number or null are treated as not available,
            // handling exception in UI will be much more complicated.
            log.error("File's '{}' size is illegal number: {}", fileStats.getKey(), e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    /**
     * Try to update the cached table from the VDSM, if succeeded fetch the file list again from the DB. if not ,handle
     * the log message.
     *
     * @param storagePoolId
     *            - The storage pool id we want to get the file list from.
     * @param storageDomainId
     *            - The storage domain id we want to get the file list from.
     */
    private synchronized void refreshActivatedStorageDomainFromVdsm(Guid storagePoolId, Guid storageDomainId) {
        if (!updateIsoListFromVDSM(storagePoolId, storageDomainId)) {
            // Add an audit log that refresh was failed for Iso files.
            handleErrorLog(storagePoolId, storageDomainId, ImageFileType.ISO);
        }
        if (!updateFloppyListFromVDSM(storagePoolId, storageDomainId)) {
            // Add an audit log that refresh was failed for Floppy files.
            handleErrorLog(storagePoolId, storageDomainId, ImageFileType.Floppy);
        }
    }

    private boolean updateAllFileListFromVDSM(Guid repoStoragePoolId, Guid repoStorageDomainId) {
        VDSReturnValue fileStatsVDSReturnValue = getFileStats(repoStoragePoolId,
                repoStorageDomainId,
                ALL_FILES_PATTERN);

        Map<String, Map<String, Object>> fileStats = fileStatsFromVDSReturnValue(fileStatsVDSReturnValue);
        updateIsoListFromVDSM(repoStoragePoolId,
                repoStorageDomainId,
                removeFileStatsForComplyingFileNames(fileStats, ISO_FILE_PATTERN_REGEX));

        updateFloppyListFromVDSM(repoStoragePoolId,
                repoStorageDomainId,
                removeFileStatsForComplyingFileNames(fileStats, FLOPPY_FILE_PATTERN_REGEX));

        //all remaining fileStats are uncategorized, of ImageFileType.Unknown type
        return refreshVdsmFileList(repoStoragePoolId,
                repoStorageDomainId,
                ImageFileType.Unknown,
                fileStats,
                null);
    }

    private Map<String, Map<String, Object>> removeFileStatsForComplyingFileNames(Map<String, Map<String, Object>> fileStats,
            Pattern filePatternRegex) {

        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Iterator<Map.Entry<String, Map<String, Object>>> it = fileStats.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Map<String, Object>> entry = it.next();
            String fileName = entry.getKey();
            if (filePatternRegex.matcher(fileName).matches()) {
                result.put(fileName, entry.getValue());
                it.remove();
            }
        }

        return result;
    }

    private boolean updateUnknownFileListFromVDSM(Guid repoStoragePoolId, Guid repoStorageDomainId) {
        VDSReturnValue fileStatsVDSReturnValue = getFileStats(repoStoragePoolId,
                repoStorageDomainId,
                ALL_FILES_PATTERN);

        Map<String, Map<String, Object>> fileStats = fileStatsFromVDSReturnValue(fileStatsVDSReturnValue);
        removeFileStatsForComplyingFileNames(fileStats, ISO_FILE_PATTERN_REGEX);
        removeFileStatsForComplyingFileNames(fileStats, FLOPPY_FILE_PATTERN_REGEX);

        // all remaining fileStats are uncategorized, of ImageFileType.Unknown type
        return refreshVdsmFileList(repoStoragePoolId,
                repoStorageDomainId,
                ImageFileType.Unknown,
                fileStats,
                null);
    }

    /**
     * Gets the Iso file list from VDSM, and if the fetch is valid refresh the Iso list in the DB.
     *
     * @param repoStoragePoolId - The repository storage pool id, we want to update the file list.
     * @param repoStorageDomainId - The repository storage domain id, for activate storage domain id.
     *
     * @return True, if the fetch from VDSM has succeeded. False otherwise.
     */

    private boolean updateIsoListFromVDSM(Guid repoStoragePoolId, Guid repoStorageDomainId) {
        VDSReturnValue fileStats = getFileStats(repoStoragePoolId,
                repoStorageDomainId,
                ISO_VDSM_FILE_PATTERN);

        return updateIsoListFromVDSM(repoStoragePoolId, repoStorageDomainId, fileStatsFromVDSReturnValue(fileStats));
    }

    private boolean updateIsoListFromVDSM(Guid repoStoragePoolId,
            Guid repoStorageDomainId, Map<String, Map<String, Object>> fileStats) {

        return refreshVdsmFileList(repoStoragePoolId,
                repoStorageDomainId,
                ImageFileType.ISO, fileStats, vmHandler::refreshVmsToolsVersion);
    }

    private boolean refreshVdsmFileList(Guid repoStoragePoolId,
            Guid repoStorageDomainId,
            ImageFileType imageFileType,
            Map<String, Map<String, Object>> fileStats,
            FileListRefreshed fileListRefreshed) {

        if (repoStorageDomainId == null) {
            return false;
        }

        boolean vdsmRefreshOk = fileStats != null;
        log.debug("The refresh process from VDSM, for {}, {}.",
                imageFileType,
                succeededOrFailed(vdsmRefreshOk));

        if (!vdsmRefreshOk) {
            return false;
        }

        boolean refreshSucceeded = refreshIsoFileListMetaData(repoStorageDomainId,
                fileStats,
                imageFileType);

        if (refreshSucceeded && fileListRefreshed != null) {
            fileListRefreshed.onFileListRefreshed(repoStoragePoolId, fileStats.keySet());
        }

        return refreshSucceeded;
    }

    private Map<String, Map<String, Object>> fileStatsFromVDSReturnValue(VDSReturnValue fileStats) {
        if (fileStats == null || !fileStats.getSucceeded()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> result = (Map<String, Map<String, Object>>) fileStats.getReturnValue();
        return result;
    }

    public interface FileListRefreshed {
        void onFileListRefreshed(Guid poolId, Set<String> isoList);
    }

    /**
     * Gets the Iso floppy file list from VDSM, and if the fetch is valid refresh the Iso floppy list in the DB.
     *
     * @param repoStoragePoolId
     *            - The repository storage pool id, we want to update the file list.
     * @param repoStorageDomainId
     *            - The repository storage domain id, for activate storage domain id.
     * @return True, if the fetch from VDSM has succeeded. False otherwise.
     */
    private boolean updateFloppyListFromVDSM(Guid repoStoragePoolId, Guid repoStorageDomainId) {

        VDSReturnValue fileStats = getFileStats(repoStoragePoolId,
                repoStorageDomainId,
                FLOPPY_VDSM_FILE_PATTERN);

        return updateFloppyListFromVDSM(repoStoragePoolId, repoStorageDomainId, fileStatsFromVDSReturnValue(fileStats));
    }

    private boolean updateFloppyListFromVDSM(Guid repoStoragePoolId,
            Guid repoStorageDomainId, Map<String, Map<String, Object>> fileStats) {
        return refreshVdsmFileList(repoStoragePoolId,
                repoStorageDomainId,
                ImageFileType.Floppy, fileStats, null
        );
    }

    private String succeededOrFailed(boolean status) {
        return status ? " succeeded" : "failed";
    }

    private VDSReturnValue getFileStats(Guid repoStoragePoolId,
            Guid repoStorageDomainId,
            String filePattern) {

        try {
            return resourceManager.runVdsCommand(VDSCommandType.GetFileStats,
                    new GetFileStatsParameters(repoStoragePoolId,
                            repoStorageDomainId, filePattern, false));
        } catch (Exception e) {
            log.warn("The refresh process for pattern {} failed: {}", filePattern, e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    /**
     * Maintain a <code>ConcurrentMap</code> which contains <code>Lock</code> object.<BR/>
     * The key Object is a <code>Pair</code> object, which will represent the domain and the file type.<BR/>
     * If no synchronized object found, the <code>Lock</code> will be add to the <code>ConcurrentMap</code>.
     *
     * @param domainId
     *            - The domain Id that supposed to be refreshed.
     * @param imageType
     *            - The file type supposed to be refreshed.
     * @return - The Lock object, which represent the domain and the file type, to lock.
     */
    private Lock getSyncObject(Guid domainId, ImageFileType imageType) {
        Pair<Guid, ImageFileType> domainPerFileType = new Pair<>(domainId, imageType);
        syncDomainForFileTypeMap.putIfAbsent(domainPerFileType, new ReentrantLock());
        return syncDomainForFileTypeMap.get(domainPerFileType);
    }

    /**
     * Add audit log message when fetch encounter problems.
     *
     * @param problematicRepoFilesList
     *            - List of Iso domain names, which encounter problem fetching from VDSM.
     */
    private void addToAuditLogErrorMessage(String problematicRepoFilesList) {
        AuditLogable logable = new AuditLogableImpl();

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        logable.addCustomValue("imageDomains", problematicRepoFilesList);
        auditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_FAILED);
    }

    /**
     * Add audit log message when fetch encounter problems.
     */
    private void addToAuditLogSuccessMessage(StorageDomain isoDomain, String imageType) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("imageDomains", String.format("%s (%s file type)", isoDomain.getName(), imageType));
        logable.setStorageDomainId(isoDomain.getId());
        logable.setStorageDomainName(isoDomain.getName());
        auditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_SUCCEEDED);
    }

    private boolean isStorageDomainIdValid(Guid storageDomainId) {
        if (storageDomainId == null) {
            log.error("Storage domain ID received from command query is null.");
            return false;
        }
        return true;
    }

    /**
     * Checks if there is an active ISO domain in the storage pool. If so returns the Iso Guid, otherwise returns null.
     * @param storagePoolId
     *            The storage pool id.
     * @return Iso Guid of active Iso, and null if not.
     */
    public Guid findActiveISODomain(Guid storagePoolId) {
        List<StorageDomain> domains = storageDomainDao.getAllForStoragePool(
                storagePoolId);
        for (StorageDomain domain : domains) {
            if (domain.getStorageDomainType() == StorageDomainType.ISO &&
                    domain.getStatus() == StorageDomainStatus.Active) {
                return domain.getId();
            }
        }
        return null;
    }

    private int getInvalidateCachePeriodFromConfig() {
        return Config.<Integer> getValue(ConfigValues.RepoDomainInvalidateCacheTimeInMinutes) * 60 * 1000;
    }

    private boolean getShouldForceRefreshByDefault() {
        return Config.getValue(ConfigValues.ForceRefreshDomainFilesListByDefault);
    }
}
