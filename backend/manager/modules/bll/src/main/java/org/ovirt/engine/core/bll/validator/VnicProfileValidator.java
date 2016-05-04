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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;

public class VnicProfileValidator {

    static final String VAR_VNIC_PROFILE_NAME = "VAR_VNIC_PROFILE_NAME";
    static final String VAR_NETWORK_FILTER_ID = "VAR_NETWORK_FILTER_ID";

    private final VmDao vmDao;
    private final StoragePoolDao dcDao;
    private final NetworkFilterDao networkFilterDao;

    private VnicProfile vnicProfile;
    private VnicProfile oldVnicProfile;
    private Network network;
    private List<VnicProfile> vnicProfiles;
    private List<VM> vms;

    public VnicProfileValidator(VnicProfile vnicProfile, VmDao vmDao, StoragePoolDao dcDao, NetworkFilterDao networkFilterDao) {
        this.vnicProfile = vnicProfile;

        this.vmDao = vmDao;
        this.dcDao = dcDao;
        this.networkFilterDao = networkFilterDao;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
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
        return new NetworkValidator(vmDao, getNetwork()).networkIsSet(vnicProfile.getNetworkId());
    }

    public ValidationResult networkQosExistsOrNull() {
        return vnicProfile.getNetworkQosId() == null
                || getDbFacade().getNetworkQosDao().get(vnicProfile.getNetworkQosId()) != null
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
        return vnicProfileNotUsed(getDbFacade().getVmTemplateDao().getAllForVnicProfile(vnicProfile.getId()),
                EngineMessage.VAR__ENTITIES__VM_TEMPLATES, EngineMessage.VAR__ENTITIES__VM_TEMPLATE);
    }

    public ValidationResult vnicProfileForVmNetworkOnly() {
        return getNetwork().isVmNetwork() ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_ADD_VNIC_PROFILE_TO_NON_VM_NETWORK);
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
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES)
                .when(vnicProfile.isPassthrough() && (vnicProfile.isPortMirroring()
                        || vnicProfile.getNetworkQosId() != null));
    }

    public boolean validateCustomProperties(List<String> messages) {
        StoragePool dataCenter = dcDao.get(getNetwork().getDataCenterId());
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
            network = getDbFacade().getNetworkDao().get(vnicProfile.getNetworkId());
        }

        return network;
    }

    protected List<VnicProfile> getVnicProfiles() {
        if (vnicProfiles == null) {
            vnicProfiles = getDbFacade().getVnicProfileDao().getAllForNetwork(vnicProfile.getNetworkId());
        }

        return vnicProfiles;
    }

    protected VnicProfile getOldVnicProfile() {
        if (oldVnicProfile == null) {
            oldVnicProfile = getDbFacade().getVnicProfileDao().get(vnicProfile.getId());
        }

        return oldVnicProfile;
    }

    protected List<VM> getVmsUsingProfile() {
        if (vms == null) {
            vms = getDbFacade().getVmDao().getAllForVnicProfile(vnicProfile.getId());
        }

        return vms;
    }

    public ValidationResult validNetworkFilterId() {
        final Guid networkFilterId = getNetworkFilterId();

        if (networkFilterId == null) {
            return ValidationResult.VALID;
        }

        NetworkFilter networkFilter = networkFilterDao.getNetworkFilterById(networkFilterId);
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

    private Guid getNetworkFilterId() {
        return vnicProfile.getNetworkFilterId();
    }
}
