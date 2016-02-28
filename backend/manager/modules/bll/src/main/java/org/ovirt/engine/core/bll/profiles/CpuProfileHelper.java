package org.ovirt.engine.core.bll.profiles;

import java.util.Optional;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class CpuProfileHelper {

    public static CpuProfile createCpuProfile(Guid clusterId, String name) {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName(name);
        cpuProfile.setClusterId(clusterId);

        return cpuProfile;
    }

    public static ValidationResult setAndValidateCpuProfileForUser(VmBase vmBase, Guid userId) {
        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase);
        }

        Optional<CpuProfile> authorizedCpuProfile = getCpuProfileDao().getAllForCluster(vmBase.getClusterId(), userId, true, ActionGroup.ASSIGN_CPU_PROFILE).stream()
                    .filter(
                            cp ->
                                    cp.getId().equals(vmBase.getCpuProfileId())
                    ).findFirst();

        if (authorizedCpuProfile.isPresent()) {
            vmBase.setCpuProfileId(authorizedCpuProfile.get().getId());
            return ValidationResult.VALID;
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

    private static ValidationResult assignFirstCpuProfile(VmBase vmBase) {
        Optional<CpuProfile> cpuProfileWithPermissions = getCpuProfileDao().getAllForCluster(vmBase.getClusterId()).stream()
                .findFirst();
            /* TODO use a properly selected default CPU profile for the cluster once the API becomes available
               see bug https://bugzilla.redhat.com/show_bug.cgi?id=1262293 for the explanation. We should probably
               add a permission check as well to make sure the profile is available to the user who added the VM. */
        if (!cpuProfileWithPermissions.isPresent()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
        } else {
            vmBase.setCpuProfileId(cpuProfileWithPermissions.get().getId());
            return ValidationResult.VALID;
        }
    }

    public static ValidationResult setAndValidateCpuProfile(VmBase vmBase) {
        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase);
        } else {
            return new CpuProfileValidator(vmBase.getCpuProfileId()).isParentEntityValid(vmBase.getClusterId());
        }
    }

    private static CpuProfileDao getCpuProfileDao() {
        return DbFacade.getInstance().getCpuProfileDao();
    }
}
