package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class CpuProfileHelper {

    public static CpuProfile createCpuProfile(Guid vdsGroupId, String name) {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName(name);
        cpuProfile.setClusterId(vdsGroupId);

        return cpuProfile;
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

    private static boolean checkPermissions(Guid cpuProfileId, Guid userId) {
        return userId == null ||
                getPermissionDao().getEntityPermissions(userId,
                        ActionGroup.ASSIGN_CPU_PROFILE,
                        cpuProfileId,
                        VdcObjectType.CpuProfile) != null;
    }

    public static ValidationResult setAndValidateCpuProfile(VmBase vmBase, Version version,  Guid userId) {
        if (!FeatureSupported.cpuQoS(version)) {
            return ValidationResult.VALID;
        }

        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase, userId);
        }

        CpuProfileValidator validator = new CpuProfileValidator(vmBase.getCpuProfileId());
        ValidationResult result = validator.isParentEntityValid(vmBase.getVdsGroupId());

        if (!result.isValid()) {
            return result;
        }

        if (!checkPermissions(vmBase.getCpuProfileId(), userId)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_NO_PERMISSION_TO_ASSIGN_CPU_PROFILE,
                    String.format("$cpuProfileId %s",
                            vmBase.getCpuProfileId()),
                    String.format("$cpuProfileName %s",
                            validator.getProfile().getName()));
        }

        return ValidationResult.VALID;
    }

    private static CpuProfileDao getCpuProfileDao() {
        return DbFacade.getInstance().getCpuProfileDao();
    }

    private static PermissionDao getPermissionDao() {
        return DbFacade.getInstance().getPermissionDao();
    }
}
