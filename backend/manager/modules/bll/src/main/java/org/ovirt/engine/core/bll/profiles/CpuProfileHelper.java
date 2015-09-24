package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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

    public static ValidationResult setAndValidateCpuProfile(VmBase vmBase, Version version) {
        if (!FeatureSupported.cpuQoS(version))
            return ValidationResult.VALID;
        if (vmBase.getCpuProfileId() == null) {
            List<CpuProfile> cpuProfiles = getCpuProfileDao().getAllForCluster(vmBase.getVdsGroupId());
            /* TODO use a properly selected default CPU profile for the cluster once the API becomes available
               see bug https://bugzilla.redhat.com/show_bug.cgi?id=1262293 for the explanation. We should probably
               add a permission check as well to make sure the profile is available to the user who added the VM. */
            if (cpuProfiles.size() > 0) {
                vmBase.setCpuProfileId(cpuProfiles.get(0).getId());
                return ValidationResult.VALID;
            } else {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_CPU_PROFILE_EMPTY);
            }
        } else {
            return new CpuProfileValidator(vmBase.getCpuProfileId()).isParentEntityValid(vmBase.getVdsGroupId());
        }
    }

    private static CpuProfileDao getCpuProfileDao() {
        return DbFacade.getInstance().getCpuProfileDao();
    }
}
