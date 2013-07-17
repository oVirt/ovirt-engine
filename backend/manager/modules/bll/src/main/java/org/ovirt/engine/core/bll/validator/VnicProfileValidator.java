package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class VnicProfileValidator {

    private VnicProfile vnicProfile;
    private VnicProfile oldVnicProfile;
    private Network network;
    private List<VnicProfile> vnicProfiles;
    private List<VM> vms;

    public VnicProfileValidator(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ValidationResult vnicProfileIsSet() {
        return vnicProfile == null
                ? new ValidationResult(VdcBllMessages.VNIC_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult vnicProfileExists() {
        return getOldVnicProfile() == null
                ? new ValidationResult(VdcBllMessages.VNIC_PROFILE_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    public ValidationResult networkExists() {
        return new NetworkValidator(getNetwork()).networkIsSet();
    }

    public ValidationResult vnicProfileNameNotUsed() {
        for (VnicProfile profile : getVnicProfiles()) {
            if (profile.getName().equals(vnicProfile.getName()) && !profile.getId().equals(vnicProfile.getId())) {
                return new ValidationResult(VdcBllMessages.VNIC_PROFILE_NAME_IN_USE);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult networkNotChanged() {
        if (ObjectUtils.equals(vnicProfile.getNetworkId(), getOldVnicProfile().getNetworkId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(VdcBllMessages.CANNOT_CHANGE_VNIC_PROFILE_NETWORK);
    }

    public ValidationResult vnicProfileNotUsedByVms() {
        return vnicProfileNotUsed(getVmsUsingProfile(), VdcBllMessages.VAR__ENTITIES__VMS);
    }

    public ValidationResult vnicProfileNotUsedByTemplates() {
        return vnicProfileNotUsed(getDbFacade().getVmTemplateDao().getAllForVnicProfile(vnicProfile.getId()),
                VdcBllMessages.VAR__ENTITIES__VM_TEMPLATES);
    }

    public ValidationResult vnicProfileForVmNetworkOnly() {
        return getNetwork().isVmNetwork() ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.CANNOT_ADD_VNIC_PROFILE_TO_NON_VM_NETWORK);
    }

    protected ValidationResult vnicProfileNotUsed(List<? extends Nameable> entities, VdcBllMessages entitiesReplacement) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_VNIC_PROFILE", entities);
        replacements.add(entitiesReplacement.name());
        return new ValidationResult(VdcBllMessages.VNIC_PROFILE_IN_USE, replacements);
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
}
