package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class CpuProfileHelper {

    public static CpuProfile createCpuProfile(Guid vdsGroupId, String name) {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName(name);
        cpuProfile.setClusterId(vdsGroupId);

        return cpuProfile;
    }

    public static ValidationResult setAndValidateCpuProfileForUser(VmBase vmBase, Version version, Guid userId) {
        if (!FeatureSupported.cpuQoS(version)) {
            return ValidationResult.VALID;
        }

        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase, userId);
        }

        /* Getting the entire list inorder to get all the cpu profiles that the user has permissions for. */
        List<CpuProfile> authorizedCpuProfiles = getCpuProfileDao().getAllForCluster(vmBase.getVdsGroupId(), userId, true, ActionGroup.ASSIGN_CPU_PROFILE);

        for(CpuProfile cp : authorizedCpuProfiles) {
            if(cp.getId().equals(vmBase.getCpuProfileId())) {
                return ValidationResult.VALID;
            }
        }

        CpuProfile chosenCpuProfile = getCpuProfileDao().get(vmBase.getCpuProfileId());

        if(chosenCpuProfile == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_NO_CPU_PROFILE_WITH_THAT_ID,
                    String.format("$cpuProfileId %s", vmBase.getCpuProfileId()));
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_NO_PERMISSION_TO_ASSIGN_CPU_PROFILE,
                String.format("$cpuProfileId %s",
                        vmBase.getCpuProfileId()),
                String.format("$cpuProfileName %s",
                        chosenCpuProfile.getName()));
    }

    public static ValidationResult assignFirstCpuProfile(VmBase vmBase, Guid userId) {
        List<CpuProfile> cpuProfilesWithPermissions = getCpuProfileDao().getAllForCluster(
                vmBase.getVdsGroupId(), userId, userId != null, ActionGroup.ASSIGN_CPU_PROFILE);

            /* TODO use a properly selected default CPU profile for the cluster once the API becomes available
               see bug https://bugzilla.redhat.com/show_bug.cgi?id=1262293 for the explanation. We should probably
               add a permission check as well to make sure the profile is available to the user who added the VM. */
        if (cpuProfilesWithPermissions.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
        } else {
            vmBase.setCpuProfileId(cpuProfilesWithPermissions.get(0).getId());
            return ValidationResult.VALID;
        }
    }

    public static ValidationResult setAndValidateCpuProfile(VmBase vmBase, Version version) {
        if (!FeatureSupported.cpuQoS(version))
            return ValidationResult.VALID;
        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase, null);
        } else {
            return new CpuProfileValidator(vmBase.getCpuProfileId()).isParentEntityValid(vmBase.getVdsGroupId());
        }
    }

    private static CpuProfileDao getCpuProfileDao() {
        return DbFacade.getInstance().getCpuProfileDao();
    }
}
