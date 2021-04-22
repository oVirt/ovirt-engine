package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;

public class VnicProfileValidator {

    static final String VAR_VNIC_PROFILE_NAME = "VAR_VNIC_PROFILE_NAME";
    static final String VAR_NETWORK_FILTER_ID = "VAR_NETWORK_FILTER_ID";

    private VnicProfile vnicProfile;
    private VnicProfile oldVnicProfile;
    private Network network;
    private List<VnicProfile> vnicProfiles;
    private List<VM> vms;

    public VnicProfileValidator(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public ValidationResult vnicProfileIsSet() {
        return vnicProfile == null
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult vnicProfileExists() {
        return getOldVnicProfile() == null
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult networkExists() {
        return new NetworkValidator(getNetwork()).networkIsSet(vnicProfile.getNetworkId());
    }

    public ValidationResult networkQosExistsOrNull() {
        return vnicProfile.getNetworkQosId() == null
                || Injector.get(NetworkQoSDao.class).get(vnicProfile.getNetworkQosId()) != null
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_NOT_EXISTS);
    }

    public ValidationResult vnicProfileNameNotUsed() {
        for (VnicProfile profile : getVnicProfiles()) {
            if (profile.getName().equals(vnicProfile.getName()) && !profile.getId().equals(vnicProfile.getId())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NAME_IN_USE);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult networkNotChanged() {
        if (Objects.equals(vnicProfile.getNetworkId(), getOldVnicProfile().getNetworkId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_VNIC_PROFILE_NETWORK);
    }

    public ValidationResult vnicProfileNotUsedByVms() {
        return vnicProfileNotUsed(getVmsUsingProfile(), EngineMessage.VAR__ENTITIES__VMS, EngineMessage.VAR__ENTITIES__VM);
    }

    public ValidationResult vnicProfileNotUsedByTemplates() {
        return vnicProfileNotUsed(Injector.get(VmTemplateDao.class).getAllForVnicProfile(vnicProfile.getId()),
                EngineMessage.VAR__ENTITIES__VM_TEMPLATES, EngineMessage.VAR__ENTITIES__VM_TEMPLATE);
    }

    public ValidationResult vnicProfileForVmNetworkOnly() {
        return getNetwork().isVmNetwork() ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_ADD_VNIC_PROFILE_TO_NON_VM_NETWORK);
    }

    public ValidationResult passthroughProfileNoPortIsolation() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_NOT_SUPPORTS_PORT_ISOLATION)
                .when(vnicProfile.isPassthrough() && getNetwork().isPortIsolation());
    }

    protected ValidationResult vnicProfileNotUsed(List<? extends Nameable> entities, EngineMessage entitiesReplacementPlural, EngineMessage entitiesReplacementSingular) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_VNIC_PROFILE", entities);
        EngineMessage replacementMessageToUse = entities.size() == 1 ? entitiesReplacementSingular : entitiesReplacementPlural;
        replacements.add(replacementMessageToUse.name());
        return new ValidationResult(getVNicProfileInUseValidationMessage(entities.size()), replacements);
    }

    private EngineMessage getVNicProfileInUseValidationMessage(int numberOfEntities) {
        boolean singular = numberOfEntities == 1;
        if (singular) {
            return EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_IN_ONE_USE;
        } else {
            return EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_IN_MANY_USES;
        }
    }

    public ValidationResult portMirroringNotChangedIfUsedByVms() {
        if (vnicProfile.isPortMirroring() == getOldVnicProfile().isPortMirroring()) {
            return ValidationResult.VALID;
        }

        return vnicProfileNotUsedByVms();
    }

    public ValidationResult passthroughNotChangedIfUsedByVms() {
        if (vnicProfile.isPassthrough() == getOldVnicProfile().isPassthrough()) {
            return ValidationResult.VALID;
        }

        return vnicProfileNotUsedByVms();
    }

    public ValidationResult passthroughProfileContainsSupportedProperties() {
        boolean conditionOccurs = vnicProfile.isPassthrough()
                && (vnicProfile.isPortMirroring()
                    || vnicProfile.getNetworkQosId() != null
                    || vnicProfile.getNetworkFilterId() != null
                    );
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES)
                .when(conditionOccurs);
    }

    public boolean validateCustomProperties(List<String> messages) {
        StoragePool dataCenter = Injector.get(StoragePoolDao.class).get(getNetwork().getDataCenterId());
        List<ValidationError> errors =
                DevicePropertiesUtils.getInstance().validateProperties(dataCenter.getCompatibilityVersion(),
                        VmDeviceGeneralType.INTERFACE,
                        vnicProfile.getCustomProperties());

        if (!errors.isEmpty()) {
            DevicePropertiesUtils.getInstance().handleCustomPropertiesError(errors, messages);
            return false;
        }

        return true;
    }

    public ValidationResult portMirroringNotSetIfExternalNetwork() {
        return !vnicProfile.isPortMirroring() || !getNetwork().isExternal()
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED);
    }

    protected Network getNetwork() {
        if (network == null) {
            network = Injector.get(NetworkDao.class).get(vnicProfile.getNetworkId());
        }

        return network;
    }

    protected List<VnicProfile> getVnicProfiles() {
        if (vnicProfiles == null) {
            vnicProfiles = Injector.get(VnicProfileDao.class).getAllForNetwork(vnicProfile.getNetworkId());
        }

        return vnicProfiles;
    }

    protected VnicProfile getOldVnicProfile() {
        if (oldVnicProfile == null) {
            oldVnicProfile = Injector.get(VnicProfileDao.class).get(vnicProfile.getId());
        }

        return oldVnicProfile;
    }

    protected List<VM> getVmsUsingProfile() {
        if (vms == null) {
            vms = Injector.get(VmDao.class).getAllForVnicProfile(vnicProfile.getId());
        }

        return vms;
    }

    public ValidationResult validNetworkFilterId() {
        final Guid networkFilterId = getNetworkFilterId();

        if (networkFilterId == null) {
            return ValidationResult.VALID;
        }

        NetworkFilter networkFilter = Injector.get(NetworkFilterDao.class).getNetworkFilterById(networkFilterId);
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VNIC_PROFILE_NETWORK_FILTER_ID,
                ReplacementUtils.createSetVariableString(VAR_VNIC_PROFILE_NAME, vnicProfile.getName()),
                ReplacementUtils.createSetVariableString(VAR_NETWORK_FILTER_ID, networkFilterId))
                .when(networkFilter == null);
    }

    public ValidationResult validUseDefaultNetworkFilterFlag(boolean useDefaultNetworkFilterId) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VNIC_PROFILE_NETWORK_ID_CONFIGURATION,
                ReplacementUtils.createSetVariableString(VAR_VNIC_PROFILE_NAME, vnicProfile.getName()),
                ReplacementUtils.createSetVariableString(VAR_NETWORK_FILTER_ID, vnicProfile.getNetworkFilterId()))
                .when(useDefaultNetworkFilterId && vnicProfile.getNetworkFilterId() != null);
    }

    public ValidationResult validFailoverId() {
        var failoverId = vnicProfile.getFailoverVnicProfileId();
        if (failoverId == null) {
            return ValidationResult.VALID;
        }

        if (Objects.equals(vnicProfile.getId(), failoverId)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_ID_CANNOT_POINT_TO_SELF);
        }

        var failoverProfile = Injector.get(VnicProfileDao.class).get(failoverId);
        if (failoverProfile == null || failoverProfile.isPassthrough()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_ID_IS_NOT_VALID);
        }

        var failoverNetwork = Injector.get(NetworkDao.class).get(failoverProfile.getNetworkId());
        if (failoverNetwork.isExternal()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_NOT_SUPPORTED_WITH_EXTERNAL_NETWORK);
        }

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_IS_SUPPORTED_ONLY_FOR_MIGRATABLE_PASSTROUGH)
                .when(!vnicProfile.isPassthrough() || !vnicProfile.isMigratable());
    }

    public ValidationResult validateProfileNotUpdatedIfFailover() {
        var profiles = Injector.get(VnicProfileDao.class).getAllByFailoverVnicProfileId(vnicProfile.getId());
        if (profiles.isEmpty()) {
            return ValidationResult.VALID;
        }
        var replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_FAILOVER", profiles);
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UPDATE_OF_FAILOVER_PROFILE_IS_NOT_SUPPORTED, replacements);
    }

    public ValidationResult failoverNotChangedIfUsedByVms() {
        if (Objects.equals(vnicProfile.getFailoverVnicProfileId(), getOldVnicProfile().getFailoverVnicProfileId())) {
            return ValidationResult.VALID;
        }

        return vnicProfileNotUsedByVms();
    }

    private Guid getNetworkFilterId() {
        return vnicProfile.getNetworkFilterId();
    }

}
