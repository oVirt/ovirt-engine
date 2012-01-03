package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddVdsGroupCommand<T extends VdsGroupOperationParameters> extends
        VdsGroupOperationCommandBase<T> {
    public static final String DefaultNetworkDescription = "Management Network";

    public AddVdsGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {

        // If either the low or the high utilization value is the configuration one, then we set both to the
        // configuration value,
        // to prevent cases where low utilization > high utilization due to specific configuration defaults
        if (getVdsGroup().gethigh_utilization() == GET_CPU_THRESHOLDS_FROM_CONFIGURATION
                || getVdsGroup().getlow_utilization() == GET_CPU_THRESHOLDS_FROM_CONFIGURATION) {

            VdsSelectionAlgorithm selectionAlgorithm;
            try {
                selectionAlgorithm = VdsSelectionAlgorithm.valueOf(Config
                        .<String> GetValue(ConfigValues.VdsSelectionAlgorithm));
            } catch (java.lang.Exception e) {
                selectionAlgorithm = VdsSelectionAlgorithm.None;
            }

            if (selectionAlgorithm == VdsSelectionAlgorithm.EvenlyDistribute) {
                getVdsGroup().sethigh_utilization(
                        Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute));
                getVdsGroup().setlow_utilization(
                        Config.<Integer> GetValue(ConfigValues.LowUtilizationForEvenlyDistribute));
            } else if (selectionAlgorithm == VdsSelectionAlgorithm.PowerSave) {
                getVdsGroup().sethigh_utilization(
                        Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave));
                getVdsGroup().setlow_utilization(
                        Config.<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave));
            }
        }
        if (getVdsGroup().getcpu_over_commit_duration_minutes() == -1) {
            getVdsGroup().setcpu_over_commit_duration_minutes(
                    Config.<Integer> GetValue(ConfigValues.CpuOverCommitDurationMinutes));
        }
        CheckMaxMemoryOverCommitValue();
        DbFacade.getInstance().getVdsGroupDAO().save(getVdsGroup());

        // add default network
        if (getParameters().getVdsGroup().getstorage_pool_id() != null) {
            final String networkName = Config.<String> GetValue(ConfigValues.ManagementNetwork);
            List<network> networks = DbFacade
                    .getInstance()
                    .getNetworkDAO()
                    .getAllForDataCenter(
                            getParameters().getVdsGroup().getstorage_pool_id()
                                    .getValue());
            // network net = null; //LINQ 31899 networks.FirstOrDefault(n =>
            // n.name == networkName);
            network net = LinqUtils.firstOrNull(networks, new Predicate<network>() {
                @Override
                public boolean eval(network network) {
                    return network.getname().equals(networkName);
                }
            });
            if (net != null) {
                DbFacade.getInstance().getNetworkClusterDAO().save(
                        new network_cluster(getParameters().getVdsGroup().getID(), net.getId(),
                                NetworkStatus.Operational.getValue(), false));
            }
        }
        setActionReturnValue(getVdsGroup().getID());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VDS_GROUP
                : AuditLogType.USER_ADD_VDS_GROUP_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        boolean result = super.canDoAction();
        getReturnValue().getCanDoActionMessages()
                .add(VdcBllMessages.VAR__ACTION__CREATE.toString());
        if (DbFacade.getInstance().getVdsGroupDAO().getByName(getVdsGroup().getname()) != null) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
            result = false;
        } else if (!CpuFlagsManagerHandler.CheckIfCpusExist(getVdsGroup().getcpu_name(),
                getVdsGroup().getcompatibility_version())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getVdsGroup().getcompatibility_version()
        )) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        } else if (getVdsGroup().getstorage_pool_id() != null) {
            setStoragePoolId(getVdsGroup().getstorage_pool_id());
            if (getStoragePool() != null
                    && getStoragePool().getcompatibility_version().compareTo(
                            getVdsGroup().getcompatibility_version()) > 0) {
                getReturnValue()
                        .getCanDoActionMessages()
                        .add(VdcBllMessages.VDS_GROUP_CANNOT_ADD_COMPATIBILITY_VERSION_WITH_LOWER_STORAGE_POOL
                                .toString());
                result = false;
            }
        }

        if (result && getVdsGroup().getstorage_pool_id() != null) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(
                    getVdsGroup().getstorage_pool_id().getValue());
            // Making sure the given SP ID is valid to prevent
            // breaking Fk_vds_groups_storage_pool_id
            if (storagePool == null) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST
                                .toString());
                result = false;
            } else if (storagePool.getstorage_pool_type() == StorageType.LOCALFS) {
                // we allow only one cluster in localfs data center
                if (!DbFacade.getInstance()
                        .getVdsGroupDAO().getAllForStoragePool(getVdsGroup().getstorage_pool_id().getValue())
                        .isEmpty()) {
                    getReturnValue().getCanDoActionMessages().add(
                            VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE
                                    .toString());
                    result = false;
                }
                // selection algorithm must be set to none in localfs
                else if (getVdsGroup().getselection_algorithm() != VdsSelectionAlgorithm.None) {
                    getReturnValue()
                            .getCanDoActionMessages()
                            .add(VdcBllMessages.VDS_GROUP_SELECTION_ALGORITHM_MUST_BE_SET_TO_NONE_ON_LOCAL_STORAGE
                                    .toString());
                    result = false;
                }
            }
        }

        if (result) {
            result = validateMetrics();
        }

        return result;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getVdsGroup().getstorage_pool_id() == null ? null
                : getVdsGroup().getstorage_pool_id().getValue(), VdcObjectType.StoragePool);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
