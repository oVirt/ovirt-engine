package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

@Singleton
public class CpuProfileHelper {

    @Inject
    CpuProfileDao cpuProfileDao;

    @Inject
    PermissionDao permissionDao;

    public static CpuProfile createCpuProfile(Guid clusterId, String name) {
        CpuProfile cpuProfile = new CpuProfile();
        cpuProfile.setId(Guid.newGuid());
        cpuProfile.setName(name);
        cpuProfile.setClusterId(clusterId);

        return cpuProfile;
    }

    public ValidationResult assignFirstCpuProfile(VmBase vmBase, Guid userId) {
        List<CpuProfile> cpuProfileWithPermissions = cpuProfileDao.getAllForCluster(
                vmBase.getClusterId(), userId, !Guid.isNullOrEmpty(userId), ActionGroup.ASSIGN_CPU_PROFILE);

            /* TODO use a properly selected default CPU profile for the cluster once the API becomes available
               see bug https://bugzilla.redhat.com/show_bug.cgi?id=1262293 for the explanation. We should probably
               add a permission check as well to make sure the profile is available to the user who added the VM. */
        if (cpuProfileWithPermissions.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
        }

        vmBase.setCpuProfileId(cpuProfileWithPermissions.get(0).getId());
        return ValidationResult.VALID;
    }

    private boolean checkPermissions(Guid cpuProfileId, Guid userId) {
        return Guid.isNullOrEmpty(userId) ||
                permissionDao.getEntityPermissions(userId,
                    ActionGroup.ASSIGN_CPU_PROFILE,
                    cpuProfileId,
                    VdcObjectType.CpuProfile) != null;
    }

    public ValidationResult setAndValidateCpuProfile(VmBase vmBase, Guid userId) {
        if (vmBase.getCpuProfileId() == null) {
            return assignFirstCpuProfile(vmBase, userId);
        }

        Guid clusterId =  vmBase.getClusterId();
        if (clusterId == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_CLUSTER_NOT_PROVIDED);
        }

        CpuProfile fetchedCpuProfile = cpuProfileDao.get(vmBase.getCpuProfileId());
        if (fetchedCpuProfile == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CPU_PROFILE_NOT_FOUND);
        }

        if (!clusterId.equals(fetchedCpuProfile.getClusterId())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_NOT_MATCH_CLUSTER);
        }

        if (!checkPermissions(vmBase.getCpuProfileId(), userId)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_NO_PERMISSION_TO_ASSIGN_CPU_PROFILE,
                    String.format("$cpuProfileId %s",
                            vmBase.getCpuProfileId()),
                    String.format("$cpuProfileName %s",
                            fetchedCpuProfile.getName()));
        }

        return ValidationResult.VALID;
    }
}
