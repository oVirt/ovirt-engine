package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddVdsGroupCommand<T extends VdsGroupOperationParameters> extends
        VdsGroupOperationCommandBase<T> {
    public static final String DefaultNetworkDescription = "Management Network";

    public AddVdsGroupCommand(T parameters) {
        super(parameters);
        setStoragePoolId(getVdsGroup().getStoragePoolId());
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
        if (FeatureSupported.cpuQoS(getParameters().getVdsGroup().getCompatibilityVersion())) {
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
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean canDoAction() {
        final ClusterValidator validator = new ClusterValidator(getDbFacade(), getVdsGroup());

        return validate(validator.nameNotUsed())
                && validate(validator.cpuTypeSupportsVirtService())
                && validate(validator.versionSupported())
                && validate(validator.dataCenterVersionMismatch())
                && validate(validator.dataCenterExists())
                && validate(validator.localStoragePoolAttachedToSingleCluster())
                && validate(validator.qosBaloonSupported())
                && validate(validator.glusterServiceSupported())
                && validate(validator.clusterServiceDefined())
                && validate(validator.mixedClusterServicesSupported())
                && validate(validator.attestationServerConfigured())
                && validate(validator.migrationSupported(getArchitecture()))
                && validate(validator.virtIoRngSupported());
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
