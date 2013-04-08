package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.RepoFileMetaDataDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * The class manages the Iso domain cache mechanism, <BR/>
 * which reflects upon support for Iso tool validation, activation of Iso domain, and fetching the Iso list by query.<BR/>
 * The cache is being refreshed with quartz scheduler which run by configuration value AutoRepoDomainRefreshTime. The
 * cache procedure using VDSM to fetch the Iso files from all the Data Centers and update the DB cache table with the
 * appropriate file data.<BR/>
 */
@SuppressWarnings("synthetic-access")
public class IsoDomainListSyncronizer {
    private static final Log log = LogFactory.getLog(IsoDomainListSyncronizer.class);
    private List<RepoFileMetaData> problematicRepoFileList = new ArrayList<RepoFileMetaData>();
    private static final int MIN_TO_MILLISECONDS = 60 * 1000;
    private static final IsoDomainListSyncronizer isoDomainListSyncronizer = new IsoDomainListSyncronizer();
    private static final ConcurrentMap<Object, Lock> syncDomainForFileTypeMap = new ConcurrentHashMap<Object, Lock>();
    private int isoDomainRefreshRate;
    RepoFileMetaDataDAO repoStorageDom;

    // Not kept as static member to enable reloading the config value
    public static String getGuestToolsSetupIsoPrefix() {
        return Config.<String> GetValue(ConfigValues.GuestToolsSetupIsoPrefix);
    }

    // Not kept as static member to enable reloading the config value
    public static String getRegexToolPattern() {
        final String guestToolsSetupIsoPrefix = getGuestToolsSetupIsoPrefix();
        return guestToolsSetupIsoPrefix + "([0-9]{1,}.[0-9])(_{1})([0-9]{1,}).[i|I][s|S][o|O]$";
    }

    /**
     * private constructor to initialize the quartz scheduler
     */
    private IsoDomainListSyncronizer() {
        log.info("Start initializing " + getClass().getSimpleName());
        repoStorageDom = DbFacade.getInstance().getRepoFileMetaDataDao();
        isoDomainRefreshRate = Config.<Integer> GetValue(ConfigValues.AutoRepoDomainRefreshTime) * MIN_TO_MILLISECONDS;
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                "fetchIsoDomains",
                new Class[] {},
                new Object[] {},
                300000,
                isoDomainRefreshRate,
                TimeUnit.MILLISECONDS);
        log.info("Finished initializing " + getClass().getSimpleName());
    }

    /**
     * Returns the singleton instance.
     * @return Singleton instance of IsoDomainManager
     */
    public static IsoDomainListSyncronizer getInstance() {
        return isoDomainListSyncronizer;
    }

    /**
     * Check and update if needed each Iso domain in each Data Center in the system.
     */
    @OnTimerMethodAnnotation("fetchIsoDomains")
    public void fetchIsoDomains() {
        // Gets all the active Iso storage domains.
        List<RepoFileMetaData> repofileList = DbFacade.getInstance()
                .getRepoFileMetaDataDao()
                .getAllRepoFilesForAllStoragePools(StorageDomainType.ISO,
                        StoragePoolStatus.Up,
                        StorageDomainStatus.Active,
                        VDSStatus.Up);

        resetProblematicList();
        // Iterate for each storage domain.
        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        for (final RepoFileMetaData repoFileMetaData : repofileList) {
            // If the list should be refreshed and the refresh from the VDSM was succeeded, fetch the file list again
            // from the DB.
            if (shouldRefreshIsoDomain(repoFileMetaData.getLastRefreshed())) {
                tasks.add(new Callable<Void>() {
                    @Override
                    public Void call() {
                        updateCachedIsoFileListFromVdsm(repoFileMetaData);
                        return null;
                    }
                });
            } else {
                log.debugFormat("Automatic refresh process for {0} file type in storage domain id {1} was not performed since refresh time out did not passed yet.",
                        repoFileMetaData.getFileType(),
                        repoFileMetaData.getRepoDomainId());
            }
        }

        ThreadPoolUtil.invokeAll(tasks);

        // After refresh for all Iso domains finished, handle the log.
        handleErrorLog(problematicRepoFileList);
    }

    /**
     * Returns a RepoFilesMetaData list with Iso file names for storage domain Id and with file type extension.<BR>
     * If user choose to refresh the cache, and a problem occurs, then returns null.
     *
     * @param storageDomainId
     *            - The storage domain Id, which we fetch the Iso list from.
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @param forceRefresh
     *            - Indicates if the domain should be refreshed from VDSM.
     * @return List of RepoFilesMetaData files or null (If fetch from VDSM failed).
     */
    public List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList(Guid storageDomainId,
            ImageFileType imageType,
            boolean forceRefresh) {
        // The result list we send back.
        List<RepoFileMetaData> repoList = null;
        if (! isStorageDomainValid(storageDomainId, imageType, forceRefresh)){
            return null;
        }
        // At any case, if refreshed or not, get Iso list from the cache.
        repoList = getCachedIsoListByDomainId(storageDomainId, imageType);

        // Return list of repository files.
        return repoList;
    }

    /**
     * Returns a RepoFilesMetaData list with Iso file names for storage domain Id and with file type extension.<BR>
     * If user choose to refresh the cache, and a problem occurs, then returns null.
     *
     * @param storagePoolId
     *            - The storage pool Id
     * @param storageDomainId
     *            - The storage domain Id, which we fetch the Iso list from.
     * @param imageType
     *            - The imageType we want to fetch the files from the cache.
     * @param forceRefresh
     *            - Indicates if the domain should be refreshed from VDSM.
     * @return List of RepoFilesMetaData files or null (If fetch from VDSM failed).
     */
    public List<RepoFileMetaData> getUserRequestForStoragePoolAndDomainRepoFileList(Guid storagePoolId,
            Guid storageDomainId,
            ImageFileType imageType,
            boolean forceRefresh) {
        // The result list we send back.
        List<RepoFileMetaData> repoList = null;

        if (! isStorageDomainValid(storageDomainId, imageType, forceRefresh)){
            return null;
        }
        // At any case, if refreshed or not, get Iso list from the cache.
        repoList = getCachedIsoListByStoragePoolAndDomainId(storagePoolId, storageDomainId, imageType);

        // Return list of repository files.
        return repoList;
    }

    private boolean refreshRepos(Guid storageDomainId, ImageFileType imageType, boolean forceRefresh) {
        boolean res = true;
        List<RepoFileMetaData> tempProblematicRepoFileList = new ArrayList<RepoFileMetaData>();
        // If user choose to force refresh.
        if (forceRefresh) {
            // Add an audit log if refresh succeeded.
            if (refreshIsoDomain(storageDomainId, tempProblematicRepoFileList, imageType)) {
                // Print log Indicating the problematic pools (If was any).
                handleErrorLog(tempProblematicRepoFileList);

                // If refresh succeeded print an audit log Indicating that.
                StorageDomain storageDomain = DbFacade.getInstance().getStorageDomainDao().get(storageDomainId);
                addToAuditLogSuccessMessage(storageDomain.getStorageName(), imageType.name());
            } else {
                // Print log Indicating the problematic pools.
                handleErrorLog(tempProblematicRepoFileList);
                res=false;
            }
        }
        return res;
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
        if (imageType == ImageFileType.ISO) {
            updateFromVDSMSucceeded = updateIsoListFromVDSM(storagePoolId, storageDomainId);
        } else if (imageType == ImageFileType.Floppy) {
            updateFromVDSMSucceeded =
                    updateFloppyListFromVDSM(storagePoolId, storageDomainId) && updateFromVDSMSucceeded;
        }

        // Log if the refresh succeeded or add the storage domain to the problematic list.
        if (updateFromVDSMSucceeded) {
            refreshSucceeded = true;
            log.debugFormat("Refresh succeeded for file type {0} at storage domain id {1} in storage pool id {2}.",
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
            List<RepoFileMetaData> problematicRepoFileList,
            ImageFileType imageType) {
        boolean refreshSucceeded = false;
        List<RepoFileMetaData> tempProblematicRepoFileList = new ArrayList<RepoFileMetaData>();

        // Fetch all the Storage pools for this Iso domain Id.
        List<StoragePoolIsoMap> isoMapList =
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .getAllForStorage(storageDomainId);
        log.debugFormat("Fetched {0} storage pools for {1} file type, in Iso domain {2}.",
                isoMapList.size(),
                imageType,
                storageDomainId);
        Iterator<StoragePoolIsoMap> iter = isoMapList.iterator();

        while (iter.hasNext() && !refreshSucceeded) {
            StoragePoolIsoMap storagePoolIsoMap = iter.next();
            Guid storagePoolId = storagePoolIsoMap.getstorage_pool_id().getValue();
            StorageDomainStatus status = storagePoolIsoMap.getstatus();

            if (status != StorageDomainStatus.Active) {
                log.debugFormat("Storage domain id {0}, is not active, and therefore could not be refreshed for {1} file type (Iso domain status is {2}).",
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
                    log.debugFormat("Failed refreshing Storage domain id {0}, for {1} file type in storage pool id {2}.",
                            storageDomainId,
                            imageType,
                            storagePoolId);
                    // set a mock repository file meta data with storage domain id and storage pool id.
                    RepoFileMetaData repoFileMetaData = new RepoFileMetaData();
                    repoFileMetaData.setStoragePoolId(storagePoolId);
                    repoFileMetaData.setRepoDomainId(storageDomainId);
                    repoFileMetaData.setFileType(imageType);

                    // Add the repository file to the list of problematic Iso domains.
                    tempProblematicRepoFileList.add(repoFileMetaData);

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
        if (storagePoolId != null && (isoStorageDomainId != null))
        {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    refreshActivatedStorageDomainFromVdsm(storagePoolId, isoStorageDomainId);
                }
            });
        }
    }

    /**
     * Returns the cached Iso file meta data list, of the storage pool Id with the storage domain id.
     *
     * @param isoStoragePoolId
     *            - The storage domain Id we want to get the file list from.
     * @param isoStorageDomainId
     *            - The storage pool Id we want to get the file list from.
     * @param imageType
     *            - The file type extension (ISO  or Floppy).
     * @return List of Iso file fetched from DB, if parameter is invalid returns an empty list.
     */
    public List<RepoFileMetaData> getCachedIsoListByStoragePoolAndDomainId(Guid isoStoragePoolId, Guid isoStorageDomainId, ImageFileType imageType) {
        List<RepoFileMetaData> fileListMD = new ArrayList<RepoFileMetaData>();
        // Check validation of parameters.
        if (isoStorageDomainId != null && isoStoragePoolId != null && VmRunHandler.getInstance().findActiveISODomain(isoStoragePoolId) != null) {
            // Get all the Iso files of storage and domain ID.
            fileListMD = repoStorageDom.getRepoListForStorageDomainAndStoragePool(isoStoragePoolId, isoStorageDomainId, imageType);
        }
        return fileListMD;
    }

    /**
     * Returns the cached Iso file meta data list, for storage domain.
     *
     * @param isoStorageDomainId
     *            - The storage domain Id we want to get the file list from.
     * @return List of Iso files fetched from DB, if parameter is invalid returns an empty list.
     */
    public List<RepoFileMetaData> getCachedIsoListByDomainId(Guid isoStorageDomainId,
            ImageFileType imageType) {
        List<RepoFileMetaData> fileListMD = new ArrayList<RepoFileMetaData>();
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
    private synchronized void addRepoFileToProblematicList(List<RepoFileMetaData> repoFileMetaDataList) {
        problematicRepoFileList.addAll(repoFileMetaDataList);
    }

   /**
     * Reset the list of problematic repository files, before starting the refresh procedure.
     * uses for multy thread caching.
     * @see #addRepoFileToProblematicList()
     */
    private synchronized void resetProblematicList() {
        problematicRepoFileList.clear();
    }

    /**
     * Print information on the problematic storage domain. Mainly transfer the business entity to list, for handling
     * the error uniformly.
     * Create a mock RepoFileMetaData object in a list, to use the functionality of the handleErrorLog with list.
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
        List<RepoFileMetaData> tempProblematicRepoFileList = new ArrayList<RepoFileMetaData>();

        // set mock repo file meta data with storage domain id and storage pool id.
        RepoFileMetaData repoFileMetaData = new RepoFileMetaData();
        repoFileMetaData.setStoragePoolId(storagePoolId);
        repoFileMetaData.setRepoDomainId(storageDomainId);
        repoFileMetaData.setFileType(imageType);

        // Add the repository file to the list, and use handleError.
        tempProblematicRepoFileList.add(repoFileMetaData);
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
    private static boolean handleErrorLog(List<RepoFileMetaData> problematicFileListForHandleError) {
        boolean hasProblematic = false;
        if (problematicFileListForHandleError != null && !problematicFileListForHandleError.isEmpty()) {
            StringBuilder problematicStorages = new StringBuilder();
            StringBuilder problematicIsoDomainsForAuditLog = new StringBuilder();
            Set<String> storageDomainNames = new HashSet<String>();
            for (RepoFileMetaData repoMap : problematicFileListForHandleError) {
                problematicStorages.append(buildDetailedProblematicMapMsg(repoMap));
                storageDomainNames.add(buildDetailedAuditLogMessage(repoMap));
            }

            // Build Audit log message with problematic domains.
            for (String domainName : storageDomainNames) {
                problematicIsoDomainsForAuditLog.append("  ").append(domainName);
            }

            hasProblematic = true;
            log.errorFormat("The following storage domains had a problem retrieving data from VDSM {0}",
                    problematicStorages.toString());
            addToAuditLogErrorMessage(problematicIsoDomainsForAuditLog.toString());
        }
        return hasProblematic;
    }

    /**
     * Returns a string builder contains problematic repoFileMetaData details.
     *
     * @param repoFileMetaData
     *            - The problematic storage domain.
     */
    private static StringBuilder buildDetailedProblematicMapMsg(RepoFileMetaData repoFileMetaData) {
        StringBuilder problematicStorageMsg = new StringBuilder();
        if (repoFileMetaData != null) {
            problematicStorageMsg.append(" (");
            if (repoFileMetaData.getStoragePoolId() != null) {
                problematicStorageMsg.append(" Storage Pool Id: ").append(repoFileMetaData.getStoragePoolId());
            }
            if (repoFileMetaData.getRepoDomainId() != null) {
                problematicStorageMsg.append(" Storage domain Id: ").append(repoFileMetaData.getRepoDomainId());
            }
            problematicStorageMsg.append(" File type: ").append(repoFileMetaData.getFileType()).append(") ");
        } else {
            problematicStorageMsg.append("(A repository file meta data business entity, has null value) ");
        }
        return problematicStorageMsg;
    }

    /**
     * Returns String contains problematic iso domain name for audit log message.
     * @param repoFileMetaData
     *            - The problematic storage domain.
     * @return
     */
    private static String buildDetailedAuditLogMessage(RepoFileMetaData repoFileMetaData) {
        String storageDomainName = "Repository not found";
        if (repoFileMetaData != null && repoFileMetaData.getRepoDomainId() != null) {
            StorageDomain storageDomain =
                    DbFacade.getInstance().getStorageDomainDao().get(repoFileMetaData.getRepoDomainId());
            if (storageDomain != null) {
                storageDomainName =
                        String.format("%s (%s file type)",
                                storageDomain.getStorageName(),
                                repoFileMetaData.getFileType().name());
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
     * @param repoFileMetaData
     */
    private void updateCachedIsoFileListFromVdsm(RepoFileMetaData repoFileMetaData)
    {
        boolean isRefreshed = false;
        try {
            List<RepoFileMetaData> problematicRepoFileList = new ArrayList<RepoFileMetaData>();
            isRefreshed =
                    refreshIsoDomain(repoFileMetaData.getRepoDomainId(),
                            problematicRepoFileList,
                            repoFileMetaData.getFileType());
            addRepoFileToProblematicList(problematicRepoFileList);
        } finally {
            log.infoFormat("Finished automatic refresh process for {0} file type with {1}, for storage domain id {2}.",
                    repoFileMetaData.getFileType(),
                    isRefreshed ? "success"
                            : "failure",
                    repoFileMetaData.getRepoDomainId());
        }
    }

    /**
     * Create a new transaction to refresh the Iso file list in to the DB.
     *
     * @param repoStorageDomainId
     *            - The repository domain id we want to update.
     * @param repoFileMetaDataDao
     *            - The Data Access Layer for storage domain.
     * @param isoDomainList
     *            - The Iso files which refreshed in the cache.
     */
    private static boolean refreshIsoFileListMetaData(final Guid repoStorageDomainId,
            final RepoFileMetaDataDAO repoFileMetaDataDao,
            final List<String> isoDomainList, final ImageFileType imageType) {
        Lock syncObject = getSyncObject(repoStorageDomainId, imageType);
        try {
            syncObject.lock();
            return (Boolean) TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            long currentTime = System.currentTimeMillis();
                            repoFileMetaDataDao.removeRepoDomainFileList(repoStorageDomainId, imageType);
                            RepoFileMetaData repo_md;
                            for (String isoFile : isoDomainList) {
                                repo_md = new RepoFileMetaData();
                                repo_md.setLastRefreshed(currentTime);
                                repo_md.setSize(0);
                                repo_md.setRepoDomainId(repoStorageDomainId);
                                repo_md.setDateCreated(null);
                                repo_md.setRepoFileName(isoFile);
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

    /**
     * Try to update the cached table from the VDSM, if succeeded fetch the file list again from the DB. if not ,handle
     * the log message.
     *
     * @param IsoStorageDomainId
     *            - The storage domain Id we want to get the file list from.
     * @param fileListMD
     *            - File list of repository files.
     * @param repoFileMetaData
     *            - repository file we fetch the domain counting on its domain id.
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
        if (repoStorageDomainId != null) {
            try {
                // Get Iso domain file list from storage pool.
                VDSReturnValue returnValue = Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.GetIsoList,
                                new IrsBaseVDSCommandParameters(repoStoragePoolId));
                @SuppressWarnings("unchecked")
                List<String> isoDomainList = (List<String>) returnValue.getReturnValue();
                if (returnValue.getSucceeded() && isoDomainList != null) {
                    log.debugFormat("The refresh process from VDSM, for Iso files succeeded.");
                    // Set the Iso domain file list fetched from VDSM into the DB.
                    refreshIsoSucceeded =
                            refreshIsoFileListMetaData(repoStorageDomainId,
                                    repoStorageDom,
                                    isoDomainList, ImageFileType.ISO);
                }
            } catch (Exception e) {
                refreshIsoSucceeded = false;
                log.warnFormat("The refresh process from VDSM, for Iso files failed.");
                log.error(e);
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
        if (repoStorageDomainId != null) {
            try {
                // Get Iso domain floppy file list from storage pool.
                VDSReturnValue returnValue = Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.GetFloppyList,
                                new IrsBaseVDSCommandParameters(repoStoragePoolId));
                @SuppressWarnings("unchecked")
                List<String> isoDomainFloppyList = (List<String>) returnValue.getReturnValue();
                if (returnValue.getSucceeded() && isoDomainFloppyList != null) {
                    // Set the Iso domain floppy file list fetched from VDSM into the DB.
                    refreshFloppySucceeded =
                            refreshIsoFileListMetaData(repoStorageDomainId,
                                    repoStorageDom,
                                    isoDomainFloppyList, ImageFileType.Floppy);
                }
                log.debugFormat("The refresh process from VDSM, for Floppy files succeeded.");
            } catch (Exception e) {
                refreshFloppySucceeded = false;
                log.warnFormat("The refresh process from VDSM, for Floppy files failed.");
                log.error(e);
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
        logable.addCustomValue("isoDomains", problematicRepoFilesList);
        AuditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_FILE_LIST_FAILED);
    }

    /**
     * Add audit log message when fetch encounter problems.
     */
    private static void addToAuditLogSuccessMessage(String IsoDomain, String imageType) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("isoDomains", String.format("%s (%s file type)", IsoDomain, imageType));
        AuditLogDirector.log(logable, AuditLogType.REFRESH_REPOSITORY_FILE_LIST_SUCCEEDED);
    }


    private boolean isStorageDomainValid(Guid storageDomainId, ImageFileType imageType, boolean forceRefresh) {
        // Check storage domain Id validity.
        if (storageDomainId == null) {
            log.error("Storage domain ID received from command query is null.");
            return false;
        }
        if (!refreshRepos(storageDomainId, imageType, forceRefresh)) {
            return false;
        }
        return true;
    }
}
