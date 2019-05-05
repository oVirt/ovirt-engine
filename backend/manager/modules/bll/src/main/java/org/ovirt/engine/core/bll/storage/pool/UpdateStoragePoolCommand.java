package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.MoveMacs;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.vdscommands.UpgradeStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T>  implements RenamedEntityInfoProvider{

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;
    @Inject
    private MoveMacs moveMacs;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private DiskLunMapDao diskLunMapDao;
    @Inject
    private VmDao vmDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public UpdateStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private StoragePool oldStoragePool;

    @Override
    protected void executeCommand() {
        updateQuotaCache();
        copyUnchangedStoragePoolProperties(getStoragePool(), oldStoragePool);
        storagePoolDao.updatePartial(getStoragePool());

        updateStoragePoolFormatType();
        updateAllClustersMacPool();
        setSucceeded(true);
    }

    private void updateAllClustersMacPool() {
        final Guid newMacPoolId = getNewMacPoolId();
        if (shouldSetNewMacPoolOnAllClusters(newMacPoolId)) {
            List<Cluster> clusters = clusterDao.getAllForStoragePool(getStoragePoolId());
            for (Cluster cluster : clusters) {
                boolean macPoolChanged = !newMacPoolId.equals(cluster.getMacPoolId());
                if (macPoolChanged) {
                    moveMacs.migrateMacsToAnotherMacPool(cluster, newMacPoolId, getContext());
                    cluster.setMacPoolId(newMacPoolId);
                    clusterDao.update(cluster);
                }
            }
        }
    }

    private Guid getNewMacPoolId() {
        return getParameters().getStoragePool().getMacPoolId();
    }

    private boolean shouldSetNewMacPoolOnAllClusters(Guid newMacPoolId) {
        return newMacPoolId != null;
    }

    private void updateQuotaCache() {
        if(wasQuotaEnforcementChanged()){
            getQuotaManager().removeStoragePoolFromCache(getStoragePool().getId());
        }
    }

    /**
     * Checks whether part of the update was disabling quota enforcement on the Data Center
     */
    private boolean wasQuotaEnforcementChanged() {
        return getOldStoragePool().getQuotaEnforcementType() != getStoragePool().getQuotaEnforcementType();
    }

    private StorageFormatType updatePoolAndDomainsFormat(final Version spVersion) {
        final StoragePool storagePool = getStoragePool();

        final StorageFormatType targetFormat = VersionStorageFormatUtil.getForVersion(spVersion);

        storagePool.setCompatibilityVersion(spVersion);
        storagePool.setStoragePoolFormatType(targetFormat);

        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                () -> {
                    storagePoolDao.updatePartial(storagePool);
                    updateMemberDomainsFormat(targetFormat);
                    vmStaticDao.incrementDbGenerationForAllInStoragePool(storagePool.getId());
                    return null;
                });

        return targetFormat;
    }

    private void updateStoragePoolFormatType() {
        final StoragePool storagePool = getStoragePool();
        final Guid spId = storagePool.getId();
        final Version spVersion = storagePool.getCompatibilityVersion();
        final Version oldSpVersion = getOldStoragePool().getCompatibilityVersion();

        if (oldSpVersion.equals(spVersion)) {
            return;
        }

        StorageFormatType targetFormat = updatePoolAndDomainsFormat(spVersion);

        if (getOldStoragePool().getStatus() == StoragePoolStatus.Up) {
            try {
                // No need to worry about "reupgrading" as VDSM will silently ignore
                // the request.
                runVdsCommand(VDSCommandType.UpgradeStoragePool,
                    new UpgradeStoragePoolVDSCommandParameters(spId, targetFormat));
            } catch (EngineException e) {
                log.warn("Upgrade process of Storage Pool '{}' has encountered a problem due to following reason: {}",
                        spId, e.getMessage());
                auditLogDirector.log(this, AuditLogType.UPGRADE_STORAGE_POOL_ENCOUNTERED_PROBLEMS);

                // if we get this error we know that no update was made, so we can safely revert the db updates
                // and return.
                if (e.getVdsError() != null && e.getErrorCode() == EngineError.PoolUpgradeInProgress) {
                    updatePoolAndDomainsFormat(oldSpVersion);
                    return;
                }
            }
        }

        runSynchronizeOperation(new RefreshPoolSingleAsyncOperationFactory(), new ArrayList<Guid>());
    }

    private void updateMemberDomainsFormat(StorageFormatType targetFormat) {
        Guid spId = getStoragePool().getId();
        List<StorageDomainStatic> domains = storageDomainStaticDao.getAllForStoragePool(spId);
        for (StorageDomainStatic domain : domains) {
            StorageDomainType sdType = domain.getStorageDomainType();

            if (sdType == StorageDomainType.Data || sdType == StorageDomainType.Master) {
                log.info("Setting storage domain '{}' (type '{}') to format '{}'",
                               domain.getId(), sdType, targetFormat);

                domain.setStorageFormat(targetFormat);
                storageDomainStaticDao.update(domain);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_POOL : AuditLogType.USER_UPDATE_STORAGE_POOL_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean validate() {
        StoragePoolValidator spValidator = createStoragePoolValidator();
        if (!validate(spValidator.exists())) {
            return false;
        }

        // Name related validations
        if (!StringUtils.equals(getOldStoragePool().getName(), getStoragePool().getName())
                && !isStoragePoolUnique(getStoragePool().getName())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        }
        if (!checkStoragePoolNameLengthValid()) {
            return false;
        }

        List<StorageDomainStatic> poolDomains = storageDomainStaticDao.getAllForStoragePool(getStoragePool().getId());
        if (getOldStoragePool().isLocal() && !getStoragePool().isLocal()
                && poolDomains.stream().anyMatch(sdc -> sdc.getStorageType() == StorageType.LOCALFS)) {
            return failValidation(EngineMessage.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_LOCAL);
        }
        if (!getOldStoragePool().isLocal() && getStoragePool().isLocal()) {
            List<Cluster> clusters = clusterDao.getAllForStoragePool(getStoragePool().getId());
            if (clusters.size() > 1) {
                return failValidation(EngineMessage.CLUSTER_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
            }
            List<VDS> hosts = vdsDao.getAllForStoragePool(getStoragePool().getId());
            if (hosts.size() > 1) {
                return failValidation(EngineMessage.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
            }
        }
        if (!getOldStoragePool().getCompatibilityVersion().equals(getStoragePool().getCompatibilityVersion())) {
            if (!isStoragePoolVersionSupported()) {
                return failValidation(VersionSupport.getUnsupportedVersionMessage());
            } else if (getStoragePool().getCompatibilityVersion().compareTo(getOldStoragePool().getCompatibilityVersion()) < 0) {
                // decreasing of compatibility version is allowed under conditions
                if (!poolDomains.isEmpty() && !isCompatibilityVersionChangeAllowedForDomains(poolDomains)) {
                    return false;
                }
                List<Network> networks = networkDao.getAllForDataCenter(getStoragePoolId());
                for (Network network : networks) {
                    NetworkValidator validator = getNetworkValidator(network);
                    if (!getManagementNetworkUtil().isManagementNetwork(network.getId())) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_DECREASE_DATA_CENTER_COMPATIBILITY_VERSION);
                    }
                }
            } else if (!checkAllClustersLevel() // Check all clusters have at least the same compatibility version.
                    || hasSuspendedVms()) {
                return false;
            }
        }

        return validate(spValidator.isNotLocalfsWithDefaultCluster())
                && validate(allMacsInEveryClusterCanBeMigratedToAnotherPool());
    }

    private ValidationResult allMacsInEveryClusterCanBeMigratedToAnotherPool() {
        Guid newMacPoolId = getNewMacPoolId();
        if (!shouldSetNewMacPoolOnAllClusters(newMacPoolId)) {
            return ValidationResult.VALID;
        }

        List<Cluster> clusters = clusterDao.getAllForStoragePool(getStoragePoolId());
        return moveMacs.canMigrateMacsToAnotherMacPool(clusters, newMacPoolId);
    }

    private boolean isCompatibilityVersionChangeAllowedForDomains(List<StorageDomainStatic> poolDomains) {
        List<String> formatProblematicDomains = new ArrayList<>();

        for (StorageDomainStatic domainStatic : poolDomains) {
            StorageDomainToPoolRelationValidator attachDomainValidator = getAttachDomainValidator(domainStatic);

            if (!attachDomainValidator.isStorageDomainFormatCorrectForDC().isValid()) {
                formatProblematicDomains.add(domainStatic.getName());
            }
        }

        return manageCompatibilityVersionChangeCheckResult(formatProblematicDomains);
    }

    private boolean manageCompatibilityVersionChangeCheckResult(List<String> formatProblematicDomains) {
        if (!formatProblematicDomains.isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DECREASING_COMPATIBILITY_VERSION_CAUSES_STORAGE_FORMAT_DOWNGRADING,
                    ReplacementUtils.replaceWith("formatDowngradedDomains", formatProblematicDomains, "," , formatProblematicDomains.size()));
        }
        return true;
    }

    protected StorageDomainToPoolRelationValidator getAttachDomainValidator(StorageDomainStatic domainStatic) {
        return new StorageDomainToPoolRelationValidator(domainStatic, getStoragePool());
    }

    protected boolean checkAllClustersLevel() {
        List<Cluster> clusters = clusterDao.getAllForStoragePool(getStoragePool().getId());
        String lowLevelClusters = clusters.stream()
                .filter(c -> getStoragePool().getCompatibilityVersion().compareTo(c.getCompatibilityVersion()) > 0)
                .map(Cluster::getName)
                .collect(Collectors.joining(","));

        if (!lowLevelClusters.isEmpty()) {
            return failValidation(
                    EngineMessage.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS,
                    String.format("$ClustersList %1$s", lowLevelClusters));
        }
        return true;
    }

    private boolean hasSuspendedVms() {
        List<VM> vmList = vmDao.getAllForStoragePool(getStoragePool().getId());
        String suspendedVms = vmList.stream()
                .filter(vm -> vm.getStatus().isSuspended() && vm.getCustomCompatibilityVersion() != null)
                .filter(vm -> getStoragePool().getCompatibilityVersion().greater(vm.getCustomCompatibilityVersion()))
                .map(VM::getName)
                .map(name -> "<li>" + name + "</li>")
                .collect(Collectors.joining());

        if (!suspendedVms.isEmpty()) {
            suspendedVms = "<ul>" + suspendedVms + "</ul>";
            addValidationMessage(EngineMessage.ERROR_CANNOT_UPDATE_STORAGE_POOL_SUSPENDED_VM_COMPATIBILITY_VERSION_NOT_SUPPORTED);
            addValidationMessageVariable("VmsList", suspendedVms);
            return true;
        }
        return false;
    }

    protected NetworkValidator getNetworkValidator(Network network) {
        return new NetworkValidator(network);
    }

    @Override
    protected StoragePoolValidator createStoragePoolValidator() {
        return super.createStoragePoolValidator();
    }

    protected boolean isStoragePoolVersionSupported() {
        return VersionSupport.checkVersionSupported(getStoragePool().getCompatibilityVersion());
    }

    /**
     * Copy properties from old entity which assumed not to be available in the param object.
     */
    private static void copyUnchangedStoragePoolProperties(StoragePool newStoragePool, StoragePool oldStoragePool) {
        newStoragePool.setStoragePoolFormatType(oldStoragePool.getStoragePoolFormatType());
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.StoragePool.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldStoragePool().getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getStoragePool().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {
        logable.setStoragePoolId(getOldStoragePool().getId());
    }

    private StoragePool getOldStoragePool() {
        if (oldStoragePool == null) {
            oldStoragePool = storagePoolDao.get(getStoragePool().getId());
        }

        return oldStoragePool;
    }

    ManagementNetworkUtil getManagementNetworkUtil() {
        return managementNetworkUtil;
    }
}
