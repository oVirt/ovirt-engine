package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddVdsGroupCommand<T extends VdsGroupOperationParameters> extends
        VdsGroupOperationCommandBase<T> {
    public static final String DefaultNetworkDescription = "Management Network";

    public AddVdsGroupCommand(T parameters) {
        super(parameters);

        updateMigrateOnError();
    }

    @Override
    protected void executeCommand() {
        getVdsGroup().setArchitecture(getArchitecture());

        checkMaxMemoryOverCommitValue();
        getVdsGroup().setDetectEmulatedMachine(true);
        getVdsGroupDAO().save(getVdsGroup());

        alertIfFencingDisabled();

        // add default network
        if (getParameters().getVdsGroup().getStoragePoolId() != null) {
            final String networkName = NetworkUtils.getEngineNetwork();
            List<Network> networks =
                    getNetworkDAO().getAllForDataCenter(getParameters().getVdsGroup().getStoragePoolId());

            Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                @Override
                public boolean eval(Network network) {
                    return network.getName().equals(networkName);
                }
            });
            if (net != null) {
                getNetworkClusterDAO().save(new NetworkCluster(getParameters().getVdsGroup().getId(),
                        net.getId(),
                        NetworkStatus.OPERATIONAL,
                        true,
                        true,
                        true));
            }
        }

        // create default CPU profile for supported clusters.
        if (FeatureSupported.cpuQoS(getParameters().getVdsGroup().getcompatibility_version())) {
            getCpuProfileDao().save(CpuProfileHelper.createCpuProfile(getParameters().getVdsGroup().getId(),
                    getParameters().getVdsGroup().getName()));
        }

        setActionReturnValue(getVdsGroup().getId());
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
        if (!isVdsGroupUnique(getVdsGroup().getName())) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
            result = false;
        } else if (getVdsGroup().supportsVirtService()
                && !CpuFlagsManagerHandler.checkIfCpusExist(getVdsGroup().getcpu_name(),
                getVdsGroup().getcompatibility_version())) {
            // cpu check required only if the cluster supports Virt service
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getVdsGroup().getcompatibility_version()
        )) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        } else if (getVdsGroup().getStoragePoolId() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId());
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

        if (result && getVdsGroup().getStoragePoolId() != null) {
            StoragePool storagePool = getStoragePoolDAO().get(getVdsGroup().getStoragePoolId());
            // Making sure the given SP ID is valid to prevent
            // breaking Fk_vds_groups_storage_pool_id
            if (storagePool == null) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST
                                .toString());
                result = false;
            } else if (storagePool.isLocal()) {
                // we allow only one cluster in localfs data center
                if (!getVdsGroupDAO().getAllForStoragePool(getVdsGroup().getStoragePoolId()).isEmpty()) {
                    getReturnValue().getCanDoActionMessages().add(
                            VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE
                                    .toString());
                    result = false;
                }
            }
        }

        if (getVdsGroup().getcompatibility_version() != null
                && Version.v3_3.compareTo(getVdsGroup().getcompatibility_version()) > 0
                && getVdsGroup().isEnableBallooning()) {
            // Members of pre-3.3 clusters don't support ballooning; here we act like a 3.2 engine
            addCanDoActionMessage(VdcBllMessages.QOS_BALLOON_NOT_SUPPORTED);
            result = false;
        }

        if (getVdsGroup().supportsGlusterService()
                && !GlusterFeatureSupported.gluster(getVdsGroup().getcompatibility_version())) {
            addCanDoActionMessage(VdcBllMessages.GLUSTER_NOT_SUPPORTED);
            addCanDoActionMessageVariable("compatibilityVersion", getVdsGroup().getcompatibility_version().getValue());
            result = false;
        }

        if(result) {
            if(!(getVdsGroup().supportsGlusterService() || getVdsGroup().supportsVirtService())) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
                result = false;
            } else if (getVdsGroup().supportsGlusterService() && getVdsGroup().supportsVirtService()
                    && !isAllowClusterWithVirtGluster()) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
                result = false;
            }
        }
        if (result && getVdsGroup().supportsTrustedService()&& Config.<String> getValue(ConfigValues.AttestationServer).equals("")) {
             addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED);
             result = false;
        }

        if (!FeatureSupported.isMigrationSupported(getArchitecture(), getVdsGroup().getcompatibility_version())
                && getVdsGroup().getMigrateOnError() != MigrateOnErrorOptions.NO) {
            return failCanDoAction(VdcBllMessages.MIGRATION_ON_ERROR_IS_NOT_SUPPORTED);
        }

        if (result) {
            result = validateClusterPolicy();
        }
        // non-empty required sources list and rng-unsupported cluster version
        if (result && !getVdsGroup().getRequiredRngSources().isEmpty()
                && !FeatureSupported.virtIoRngSupported(getVdsGroup().getcompatibility_version())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED);
            result = false;
        }

        return result;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getVdsGroup().getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
