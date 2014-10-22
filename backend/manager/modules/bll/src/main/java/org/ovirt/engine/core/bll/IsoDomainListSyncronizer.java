package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetFileStatsParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.RepoFileMetaDataDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
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
public class IsoDomainListSyncronizer {
    private static final Logger log = LoggerFactory.getLogger(IsoDomainListSyncronizer.class);
    private List<RepoImage> problematicRepoFileList = new ArrayList<RepoImage>();
    private static final int MIN_TO_MILLISECONDS = 60 * 1000;
    private static volatile IsoDomainListSyncronizer isoDomainListSyncronizer;
    private static final ConcurrentMap<Object, Lock> syncDomainForFileTypeMap = new ConcurrentHashMap<Object, Lock>();
    private int isoDomainRefreshRate;
    RepoFileMetaDataDAO repoStorageDom;
    ProviderDao providerDao;

    public static final String TOOL_CLUSTER_LEVEL = "clusterLevel";
    public static final String TOOL_VERSION = "toolVersion";
    public static final String REGEX_TOOL_PATTERN =
            String.format("%1$s(?<%2$s>[0-9]{1,}.[0-9])_{1}(?<%3$s>[0-9]{1,}).[i|I][s|S][o|O]$",
                    getGuestToolsSetupIsoPrefix(),
                    TOOL_CLUSTER_LEVEL,
                    TOOL_VERSION);
    public static final String ISO_FILE_PATTERN = "*.iso";
    public static final String FLOPPY_FILE_PATTERN = "*.vfd";

    // Not kept as static member to enable reloading the config value
    public static String getGuestToolsSetupIsoPrefix() {
        return Config.<String> getValue(ConfigValues.GuestToolsSetupIsoPrefix);
    }

    /**
     * private constructor to initialize the quartz scheduler
     */
    protected IsoDomainListSyncronizer() {
        init();
    }

    protected void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        repoStorageDom = DbFacade.getInstance().getRepoFileMetaDataDao();
        providerDao = DbFacade.getInstance().getProviderDao();
        isoDomainRefreshRate = Config.<Integer> getValue(ConfigValues.AutoRepoDomainRefreshTime) * MIN_TO_MILLISECONDS;
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                "fetchIsoDomains",
                new Class[] {},
                new Object[] {},
                300000,
                isoDomainRefreshRate,
                TimeUnit.MILLISECONDS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Returns the singleton instance.
     * @return Singleton instance of IsoDomainManager
     */
    public static IsoDomainListSyncronizer getInstance() {
        if (isoDomainListSyncronizer == null) {
            synchronized (IsoDomainListSyncronizer.class) {
                if (isoDomainListSyncronizer == null) {
                    isoDomainListSyncronizer = new IsoDomainListSyncronizer();
                }
            }
        }
        return isoDomainListSyncronizer;
    }

    /**
     * Check and update if needed each Iso domain in each Data Center in the system.
     */
    @OnTimerMethodAnnotation("fetchIsoDomains")
    public void fetchIsoDomains() {
        // Gets all the active Iso storage domains.
        List<RepoImage> repofileList = DbFacade.getInstance()
                .getRepoFileMetaDataDao()
                .getAllRepoFilesForAllStoragePools(StorageDomainType.ISO,
                        StoragePoolStatus.Up,
                        StorageDomainStatus.Active,
                        VDSStatus.Up);

        resetProblematicList();
        // Iterate for each storage domain.
        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        for (final RepoImage repoImage : repofileList) {
            // If the list should be refreshed and the refresh from the VDSM was succeeded, fetch the file list again
            // from the DB.
            if (shouldRefreshIsoDomain(repoImage.getLastRefreshed())) {
                tasks.add(new Callable<Void>() {
                    @Override
                    public Void call() {
                        updateCachedIsoFileListFromVdsm(repoImage);
                        return null;
                    }
                });
            } else {
                log.debug("Automatic refresh process for '{}' file type in storage domain id '{}' was not performed"
                                + " since refresh time out did not passed yet.",
                        repoImage.getFileType(),
                        repoImage.getRepoDomainId());
            }
        }

        ThreadPoolUtil.invokeAll(tasks);

        // After refresh for all Iso domains finished, handle the log.
        handleErrorLog(new ArrayList<>(problematicRepoFileList));
    }

    /**
     * Returns a RepoFilesMetaData list with Iso file names for storage domain Id and with file type extension.<BR>
     * If user choose to refresh the cache, and a problem occurs, then throws VdcBLLException.
     *
     * @param storageDomainId
     *            - The storage domain Id, which we fetch the Iso list from.
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @param forceRefresh
     *            - Indicates if the domain should be refreshed from VDSM.
     * @throws VdcBLLException - if a problem occurs when refreshing the image repo cache.
     * @return List of RepoFilesMetaData files.
     */
    public List<RepoImage> getUserRequestForStorageDomainRepoFileList(Guid storageDomainId,
            ImageFileType imageType,
            boolean forceRefresh) {
        // The result list we send back.
        List<RepoImage> repoList = null;
        if (!isStorageDomainValid(storageDomainId, imageType, forceRefresh)) {
            throw new VdcBLLException(VdcBllErrors.GetIsoListError);
        }
        // At any case, if refreshed or not, get Iso list from the cache.
        repoList = getCachedIsoListByDomainId(storageDomainId, imageType);

        // Return list of repository files.
        return repoList;
    }

    private boolean refreshRepos(Guid storageDomainId, ImageFileType imageType) {
        boolean refreshResult = false;
        List<RepoImage> tempProblematicRepoFileList = new ArrayList<RepoImage>();
        StorageDomain storageDomain = DbFacade.getInstance().getStorageDomainDao().get(storageDomainId);

        if (storageDomain.getStorageDomainType() == StorageDomainType.ISO) {
            refreshResult = refreshIsoDomain(storageDomainId, tempProblematicRepoFileList, imageType);
        } else if (storageDomain.getStorageDomainType() == StorageDomainType.Image &&
                storageDomain.getStorageType() == StorageType.GLANCE) {
            refreshResult = refreshImageDomain(storageDomain, tempProblematicRepoFileList, imageType);
        } else {
            log.error("Unable to refresh the storage domain '{}', Storage Domain Type '{}' not supported",
                    storageDomainId, storageDomain.getStorageDomainType());
            return false;
        }

        handleErrorLog(tempProblematicRepoFileList);

        // If refresh succeeded update the audit log
        if (refreshResult) {
            addToAuditLogSuccessMessage(storageDomain.getStorageName(), imageType.name());
        }

        return refreshResult;
    }

    private boolean refreshImageDomain(final StorageDomain storageDomain,
            List<RepoImage> problematicRepoFileList, final ImageFileType imageType) {
        final RepoFileMetaDataDAO repoFileMetaDataDao = repoStorageDom;

        Provider provider = providerDao.get(new Guid(storageDomain.getStorage()));
        final OpenStackImageProviderProxy client = ProviderProxyFactory.getInstance().create(provider);

        Lock syncObject = getSyncObject(storageDomain.getId(), imageType);
        try {
            syncObject.lock();
            return (Boolean) TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            repoFileMetaDataDao.removeRepoDomainFileList(storageDomain.getId(), imageType);

                            Integer totalListSize = Config.<Integer> getValue(ConfigValues.GlanceImageTotalListSize);
                            List<RepoImage> repoImages = client.getAllImagesAsRepoImages(
                                    Config.<Integer> getValue(ConfigValues.GlanceImageListSize), totalListSize);

                            if (repoImages.size() >= totalListSize) {
                                AuditLogableBase logable = new AuditLogableBase();
                                logable.addCustomValue("imageDomain", storageDomain.getName());
                                logable.addCustomValue("imageListSize", String.valueOf(repoImages.size()));
                                AuditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_INCOMPLETE);
                            }

                            for (RepoImage repoImage : repoImages) {
                                repoImage.setRepoDomainId(storageDomain.getId());
                                repoFileMetaDataDao.addRepoFileMap(repoImage);
                            }

                            return true;
                        }
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
        boolean refreshSucceeded = false;
        // Setting the indication to the indication whether the storage pool is valid.
        boolean updateFromVDSMSucceeded = true;

        // If the SPM and the storage pool are valid, try to refresh the Iso list by fetching it from the SPM.
        if (imageType == ImageFileType.ISO || imageType == ImageFileType.All) {
            updateFromVDSMSucceeded = updateIsoListFromVDSM(storagePoolId, storageDomainId);
        }

        if (imageType == ImageFileType.Floppy || imageType == ImageFileType.All) {
            updateFromVDSMSucceeded =
                    updateFloppyListFromVDSM(storagePoolId, storageDomainId) && updateFromVDSMSucceeded;
        }

        // Log if the refresh succeeded or add the storage domain to the problematic list.
        if (updateFromVDSMSucceeded) {
            refreshSucceeded = true;
            log.debug("Refresh succeeded for file type '{}' at storage domain id '{}' in storage pool id '{}'.",
                    imageType.name(),
                    storageDomainId,
                    storagePoolId);
        }
        return refreshSucceeded;
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
        boolean refreshSucceeded = false;
        List<RepoImage> tempProblematicRepoFileList = new ArrayList<RepoImage>();

        // Fetch all the Storage pools for this Iso domain Id.
        List<StoragePoolIsoMap> isoMapList =
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .getAllForStorage(storageDomainId);
        log.debug("Fetched {} storage pools for '{}' file type, in Iso domain '{}'.",
                isoMapList.size(),
                imageType,
                storageDomainId);
        Iterator<StoragePoolIsoMap> iter = isoMapList.iterator();

        while (iter.hasNext() && !refreshSucceeded) {
            StoragePoolIsoMap storagePoolIsoMap = iter.next();
            Guid storagePoolId = storagePoolIsoMap.getstorage_pool_id();
            StorageDomainStatus status = storagePoolIsoMap.getStatus();

            if (status != StorageDomainStatus.Active) {
                log.debug("Storage domain id '{}', is not active, and therefore could not be refreshed for '{}'"
                                + " file type (Iso domain status is '{}').",
                        storageDomainId,
                        imageType,
                        status);
            }
            else {
                // Try to refresh the domain of the storage pool id because its status is active.
                refreshSucceeded =
                        refreshIsoDomainFileForStoragePool(storageDomainId,
                                storagePoolId,
                                imageType);
                if (!refreshSucceeded) {
                    log.debug("Failed refreshing Storage domain id '{}', for '{}' file type in storage pool id '{}'.",
                            storageDomainId,
                            imageType,
                            storagePoolId);
                    // set a mock repository file meta data with storage domain id and storage pool id.
                    RepoImage repoImage = new RepoImage();
                    repoImage.setStoragePoolId(storagePoolId);
                    repoImage.setRepoDomainId(storageDomainId);
                    repoImage.setFileType(imageType);

                    // Add the repository file to the list of problematic Iso domains.
                    tempProblematicRepoFileList.add(repoImage);

                }
            }
        }

        // If refreshed was not succeeded add the problematic storage Iso domain to the list.
        if (!refreshSucceeded) {
            problematicRepoFileList.addAll(tempProblematicRepoFileList);
        }

        return refreshSucceeded;
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
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    refreshActivatedStorageDomainFromVdsm(storagePoolId, isoStorageDomainId);
                }
            });
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
        List<RepoImage> fileListMD = new ArrayList<RepoImage>();
        if (isoStorageDomainId != null) {
            fileListMD =
                    repoStorageDom.getRepoListForStorageDomain(isoStorageDomainId, imageType);
        }
        return fileListMD;
    }

    /**
     * Handling the list of problematic repository files, to maintain multi thread caching.
     * @see #resetProblematicList()
     */
    private synchronized void addRepoFileToProblematicList(List<RepoImage> repoImageList) {
        problematicRepoFileList.addAll(repoImageList);
    }

   /**
     * Reset the list of problematic repository files, before starting the refresh procedure.
     * uses for multy thread caching.
     * @see #addRepoFileToProblematicList(List<RepoImage>)
     */
    private synchronized void resetProblematicList() {
        problematicRepoFileList.clear();
    }

    /**
     * Print information on the problematic storage domain. Mainly transfer the business entity to list, for handling
     * the error uniformly.
     * Create a mock RepoImage object in a list, to use the functionality of the handleErrorLog with list.
     *
     * @param storagePoolId
     *            - The storage domain Id.
     * @param storagePoolId
     *            - The storage pool Id.
     * @param imageType
     *            - The file type extension (ISO  or Floppy).
     * @see #handleErrorLog(List)
     */
    private static void handleErrorLog(Guid storagePoolId, Guid storageDomainId, ImageFileType imageType) {
        List<RepoImage> tempProblematicRepoFileList = new ArrayList<RepoImage>();

        // set mock repo file meta data with storage domain id and storage pool id.
        RepoImage repoImage = new RepoImage();
        repoImage.setStoragePoolId(storagePoolId);
        repoImage.setRepoDomainId(storageDomainId);
        repoImage.setFileType(imageType);

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
    private static boolean handleErrorLog(List<RepoImage> problematicFileListForHandleError) {
        boolean hasProblematic = false;
        if (problematicFileListForHandleError != null && !problematicFileListForHandleError.isEmpty()) {
            StringBuilder problematicStorages = new StringBuilder();
            StringBuilder problematicIsoDomainsForAuditLog = new StringBuilder();
            Set<String> storageDomainNames = new HashSet<String>();
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
     * @return
     */
    private static String buildDetailedAuditLogMessage(RepoImage repoImage) {
        String storageDomainName = "Repository not found";
        if (repoImage != null && repoImage.getRepoDomainId() != null) {
            StorageDomain storageDomain =
                    DbFacade.getInstance().getStorageDomainDao().get(repoImage.getRepoDomainId());
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

    /**
     * Updates the DB cache table with files fetched from VDSM.
     * The method is dedicated for multiple threads refresh.
     * If refresh from VDSM has encounter problems, we update the problematic domain list.
     * @param repoImage
     */
    private void updateCachedIsoFileListFromVdsm(RepoImage repoImage) {
        boolean isRefreshed = false;
        try {
            List<RepoImage> problematicRepoFileList = new ArrayList<RepoImage>();
            isRefreshed =
                    refreshIsoDomain(repoImage.getRepoDomainId(),
                            problematicRepoFileList,
                            repoImage.getFileType());
            addRepoFileToProblematicList(problematicRepoFileList);
        } finally {
            log.info("Finished automatic refresh process for '{}' file type with {}, for storage domain id '{}'.",
                    repoImage.getFileType(),
                    isRefreshed ? "success"
                            : "failure",
                    repoImage.getRepoDomainId());
        }
    }

    private static boolean refreshIsoFileListMetaData(final Guid repoStorageDomainId,
                                                      final RepoFileMetaDataDAO repoFileMetaDataDao,
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
                            RepoImage repo_md;
                            for (Map.Entry<String, Map<String, Object>> entry : fileStats.entrySet()) {
                                repo_md = new RepoImage();
                                repo_md.setLastRefreshed(currentTime);
                                repo_md.setSize(retrieveIsoFileSize(entry));
                                repo_md.setRepoDomainId(repoStorageDomainId);
                                repo_md.setDateCreated(null);
                                repo_md.setRepoImageId(entry.getKey());
                                repo_md.setRepoImageName(null);
                                repo_md.setFileType(imageType);
                                repoFileMetaDataDao.addRepoFileMap(repo_md);
                            }
                            return true;
                        }
                    });
        } finally {
            syncObject.unlock();
        }
    }

    private static long retrieveIsoFileSize(Map.Entry<String, Map<String, Object>> fileStats) {
        try {
            return Long.valueOf((String) fileStats.getValue().get(VdsProperties.size));
        } catch (RuntimeException e) {
            // Illegal number or null are treated as not available,
            // handling exception in UI will be much more complicated.
            log.error("File's '{}' size is illegal number: {}", fileStats.getKey(), e.getMessage());
            log.debug("Exception", e);
            return StorageConstants.SIZE_IS_NOT_AVAILABLE;
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

    /**
     * Check if last refreshed time has exceeded the time limit configured in isoDomainRefreshRate.
     *
     * @param lastRefreshed
     *            - Time when repository file was last refreshed.
     * @return True if time exceeded, and should refresh the domain, false otherwise.
     */
    private boolean shouldRefreshIsoDomain(long lastRefreshed) {
        return ((System.currentTimeMillis() - lastRefreshed) > isoDomainRefreshRate);
    }

    /**
     * Gets the Iso file list from VDSM, and if the fetch is valid refresh the Iso list in the DB.
     *
     * @param repoStoragePoolId
     *            - The repository storage pool id, we want to update the file list.
     * @param repoStorageDomainId
     *            - The repository storage domain id, for activate storage domain id.
     * @return True, if the fetch from VDSM has succeeded. False otherwise.
     */
    private boolean updateIsoListFromVDSM(Guid repoStoragePoolId, Guid repoStorageDomainId) {
        boolean refreshIsoSucceeded = false;
        VDSReturnValue returnValue;

        if (repoStorageDomainId != null) {
            try {
                StoragePool dc = getStoragePoolDAO().get(repoStoragePoolId);
                if (FeatureSupported.getFileStats(dc.getcompatibility_version())) {
                    returnValue = Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.GetFileStats,
                                    new GetFileStatsParameters(repoStoragePoolId,
                                            repoStorageDomainId, ISO_FILE_PATTERN, false));
                } else {
                    returnValue = Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.GetIsoList,
                                    new IrsBaseVDSCommandParameters(repoStoragePoolId));
                }

                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> fileStats =
                        (Map<String, Map<String, Object>>) returnValue.getReturnValue();
                if (returnValue.getSucceeded() && fileStats != null) {
                    log.debug("The refresh process from VDSM, for Iso files succeeded.");
                    // Set the Iso domain file list fetched from VDSM into the DB.
                    refreshIsoSucceeded =
                            refreshIsoFileListMetaData(repoStorageDomainId,
                                    repoStorageDom,
                                    fileStats,
                                    ImageFileType.ISO);
                }
            } catch (Exception e) {
                refreshIsoSucceeded = false;
                log.warn("The refresh process from VDSM, for Iso files failed: {}", e.getMessage());
                log.debug("Exception", e);
            }
        }
        return refreshIsoSucceeded;
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
        boolean refreshFloppySucceeded = false;
        VDSReturnValue returnValue;

        if (repoStorageDomainId != null) {
            try {
                StoragePool dc = getStoragePoolDAO().get(repoStoragePoolId);
                if (FeatureSupported.getFileStats(dc.getcompatibility_version())) {
                    returnValue = Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.GetFileStats,
                                    new GetFileStatsParameters(repoStoragePoolId,
                                            repoStorageDomainId, FLOPPY_FILE_PATTERN, false));
                } else {
                    returnValue = Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.GetFloppyList,
                                    new IrsBaseVDSCommandParameters(repoStoragePoolId));
                }

                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> fileStats =
                        (Map<String, Map<String, Object>>) returnValue.getReturnValue();
                if (returnValue.getSucceeded() && fileStats != null) {
                    // Set the Iso domain floppy file list fetched from VDSM into the DB.
                    refreshFloppySucceeded =
                            refreshIsoFileListMetaData(repoStorageDomainId,
                                    repoStorageDom,
                                    fileStats,
                                    ImageFileType.Floppy);
                }
                log.debug("The refresh process from VDSM, for Floppy files succeeded.");
            } catch (Exception e) {
                refreshFloppySucceeded = false;
                log.warn("The refresh process from VDSM, for Floppy files failed: {}", e.getMessage());
                log.debug("Exception", e);
            }
        }
        return refreshFloppySucceeded;
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
    private static Lock getSyncObject(Guid domainId, ImageFileType imageType) {
        Pair<Guid, ImageFileType> domainPerFileType = new Pair<Guid, ImageFileType>(domainId, imageType);
        syncDomainForFileTypeMap.putIfAbsent(domainPerFileType, new ReentrantLock());
        return syncDomainForFileTypeMap.get(domainPerFileType);
    }

    /**
     * Add audit log message when fetch encounter problems.
     *
     * @param problematicRepoFilesList
     *            - List of Iso domain names, which encounter problem fetching from VDSM.
     */
    private static void addToAuditLogErrorMessage(String problematicRepoFilesList) {
        AuditLogableBase logable = new AuditLogableBase();

        // Get translated error by error code ,if no translation found (should not happened) ,
        // will set the error code instead.
        logable.addCustomValue("imageDomains", problematicRepoFilesList);
        AuditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_FAILED);
    }

    /**
     * Add audit log message when fetch encounter problems.
     */
    private static void addToAuditLogSuccessMessage(String IsoDomain, String imageType) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("imageDomains", String.format("%s (%s file type)", IsoDomain, imageType));
        AuditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_IMAGE_LIST_SUCCEEDED);
    }


    private boolean isStorageDomainValid(Guid storageDomainId, ImageFileType imageType, boolean forceRefresh) {
        // Check storage domain Id validity.
        if (storageDomainId == null) {
            log.error("Storage domain ID received from command query is null.");
            return false;
        }
        if (forceRefresh) {
            return refreshRepos(storageDomainId, imageType);
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
        List<StorageDomain> domains = getStorageDomainDAO().getAllForStoragePool(
                storagePoolId);
        for (StorageDomain domain : domains) {
            if (domain.getStorageDomainType() == StorageDomainType.ISO &&
                    domain.getStatus() == StorageDomainStatus.Active) {
                return domain.getId();
            }
        }
        return null;
    }

    private StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    private StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDao();
    }
}
