package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.vdscommands.UpgradeStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T>  implements RenamedEntityInfoProvider{
    public UpdateStoragePoolCommand(T parameters) {
        this(parameters, null);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected UpdateStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private StoragePool oldStoragePool;
    private StorageDomain masterDomainForPool;

    @Override
    protected void executeCommand() {
        updateQuotaCache();
        copyUnchangedStoragePoolProperties(getStoragePool(), oldStoragePool);
        getStoragePoolDAO().updatePartial(getStoragePool());

        updateStoragePoolFormatType();
        setSucceeded(true);
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

    private void updateStoragePoolFormatType() {
        final StoragePool storagePool = getStoragePool();
        final Guid spId = storagePool.getId();
        final Version spVersion = storagePool.getCompatibilityVersion();
        final Version oldSpVersion = getOldStoragePool().getCompatibilityVersion();

        if (oldSpVersion.equals(spVersion)) {
            return;
        }



        final StorageFormatType targetFormat =
                VersionStorageFormatUtil.getPreferredForVersion(spVersion, getMasterDomain() == null ? null : getMasterDomain().getStorageType());

        storagePool.setStoragePoolFormatType(targetFormat);

        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                             getStoragePoolDAO().updatePartial(storagePool);
                        updateMemberDomainsFormat(targetFormat);
                        if (FeatureSupported.ovfStoreOnAnyDomain(spVersion)) {
                            getVmStaticDAO().incrementDbGenerationForAllInStoragePool(spId);
                        }
                        return null;
                    }
        });

        if (getOldStoragePool().getStatus() == StoragePoolStatus.Up) {
            try {
                // No need to worry about "reupgrading" as VDSM will silently ignore
                // the request.
                runVdsCommand(VDSCommandType.UpgradeStoragePool,
                    new UpgradeStoragePoolVDSCommandParameters(spId, targetFormat));
            } catch (VdcBLLException e) {
                log.warn("Upgrade process of Storage Pool '{}' has encountered a problem due to following reason: {}",
                        spId, e.getMessage());
                AuditLogDirector.log(this, AuditLogType.UPGRADE_STORAGE_POOL_ENCOUNTERED_PROBLEMS);
            }
        }

        runSynchronizeOperation(new RefreshPoolSingleAsyncOperationFactory(), new ArrayList<Guid>());
    }

    private void updateMemberDomainsFormat(StorageFormatType targetFormat) {
        Guid spId = getStoragePool().getId();
        StorageDomainStaticDAO sdStatDao = DbFacade.getInstance().getStorageDomainStaticDao();
        List<StorageDomainStatic> domains = sdStatDao.getAllForStoragePool(spId);
        for (StorageDomainStatic domain : domains) {
            StorageDomainType sdType = domain.getStorageDomainType();

            if (sdType == StorageDomainType.Data || sdType == StorageDomainType.Master) {
                log.info("Updating storage domain '{}' (type '{}') to format '{}'",
                               domain.getId(), sdType, targetFormat);

                domain.setStorageFormat(targetFormat);
                sdStatDao.update(domain);
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        if (!checkStoragePool()) {
            return false;
        }

        // Name related validations
        if (!StringUtils.equals(getOldStoragePool().getName(), getStoragePool().getName())
                && !isStoragePoolUnique(getStoragePool().getName())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        }
        if (!checkStoragePoolNameLengthValid()) {
            return false;
        }

        List<StorageDomainStatic> poolDomains = getStorageDomainStaticDAO().getAllForStoragePool(getStoragePool().getId());
        if ( getOldStoragePool().isLocal() != getStoragePool().isLocal() && !poolDomains.isEmpty() ) {
            return failCanDoAction(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS);
        }
        if ( !getOldStoragePool().getCompatibilityVersion().equals(getStoragePool()
                .getCompatibilityVersion())) {
            if (!isStoragePoolVersionSupported()) {
                return failCanDoAction(VersionSupport.getUnsupportedVersionMessage());
            }
            // decreasing of compatibility version is allowed under conditions
            else if (getStoragePool().getCompatibilityVersion().compareTo(getOldStoragePool().getCompatibilityVersion()) < 0) {
                List<Network> networks = getNetworkDAO().getAllForDataCenter(getStoragePoolId());
                if (networks.size() == 1) {
                    Network network = networks.get(0);
                    NetworkValidator validator = getNetworkValidator(network);
                    validator.setDataCenter(getStoragePool());
                    if (!NetworkUtils.isManagementNetwork(network)
                            || !validator.canNetworkCompatabilityBeDecreased()) {
                        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
                    }
                } else if (networks.size() > 1) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
                }
            } else if (!checkAllClustersLevel()) {  // Check all clusters has at least the same compatibility version.
                return false;
            }
        }

        StoragePoolValidator validator = createStoragePoolValidator();
        return validate(validator.isNotLocalfsWithDefaultCluster());
    }

    protected boolean checkAllClustersLevel() {
        boolean returnValue = true;
        List<VDSGroup> clusters = getVdsGroupDAO().getAllForStoragePool(getStoragePool().getId());
        List<String> lowLevelClusters = new ArrayList<String>();
        for (VDSGroup cluster : clusters) {
            if (getStoragePool().getCompatibilityVersion().compareTo(cluster.getCompatibilityVersion()) > 0) {
                lowLevelClusters.add(cluster.getName());
            }
        }
        if (!lowLevelClusters.isEmpty()) {
            returnValue = false;
            getReturnValue().getCanDoActionMessages().add(String.format("$ClustersList %1$s",
                    StringUtils.join(lowLevelClusters, ",")));
            getReturnValue()
                    .getCanDoActionMessages()
                    .add(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS
                            .toString());
        }
        return returnValue;
    }

    private StorageDomain getMasterDomain() {
        if (masterDomainForPool == null) {
            Guid masterId = getStorageDomainDAO().getMasterStorageDomainIdForPool(getStoragePoolId());
            if (Guid.Empty.equals(masterId)) {
                masterDomainForPool = getStorageDomainDAO().get(masterId);
            }
        }
        return masterDomainForPool;
    }

    @Override
    protected NetworkDao getNetworkDAO() {
        return getDbFacade().getNetworkDao();
    }

    protected NetworkValidator getNetworkValidator(Network network) {
        return new NetworkValidator(network);
    }

    protected StoragePoolValidator createStoragePoolValidator() {
        return new StoragePoolValidator(getStoragePool());
    }

    protected boolean isStoragePoolVersionSupported() {
        return VersionSupport.checkVersionSupported(getStoragePool().getCompatibilityVersion());
    }

    /**
     * Copy properties from old entity which assumed not to be available in the param object.
     *
     * @param oldStoragePool
     * @param newStoragePool
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
    public void setEntityId(AuditLogableBase logable) {
        logable.setStoragePoolId(getOldStoragePool().getId());
    }

    private Guid getOldMacPoolId() {
        return getOldStoragePool().getMacPoolId();
    }

    private StoragePool getOldStoragePool() {
        if (oldStoragePool == null) {
            oldStoragePool = getStoragePoolDAO().get(getStoragePool().getId());
        }

        return oldStoragePool;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> result = new ArrayList<>(super.getPermissionCheckSubjects());

        final Guid macPoolId = getParameters().getStoragePool() == null ? null : getParameters().getStoragePool().getMacPoolId();
        final boolean changingPoolDefinition = macPoolId != null && !macPoolId.equals(getOldMacPoolId());
        if (changingPoolDefinition) {
            result.add(new PermissionSubject(macPoolId, VdcObjectType.MacPool, ActionGroup.CONFIGURE_MAC_POOL));
        }

        return result;
    }
}
