package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;


public class CpuProfileValidator extends ProfileValidator<CpuProfile> {
    private Cluster cluster;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private CpuProfileDao cpuProfileDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmDao vmDao;

    public CpuProfileValidator(CpuProfile profile) {
        super(profile);
    }

    public CpuProfileValidator(Guid profileId) {
        super(profileId);
    }

    @Override
    public ValidationResult parentEntityExists() {
        if (clusterDao.get(getProfile().getClusterId()) == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult parentEntityNotChanged() {
        if (Objects.equals(getProfile().getClusterId(), getProfileFromDb().getClusterId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE);
    }

    protected Cluster getCluster() {
        if (cluster == null) {
            cluster = clusterDao.get(getProfile().getClusterId());
        }

        return cluster;
    }

    @Override
    public ValidationResult isLastProfileInParentEntity() {
        if (getProfileDao().getAllForCluster(getProfile().getClusterId()).size() == 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CANNOT_REMOVE_LAST_CPU_PROFILE_IN_CLUSTER);
        }
        return ValidationResult.VALID;
    }

    @Override
    protected CpuProfileDao getProfileDao() {
        return cpuProfileDao;
    }

    @Override
    protected List<CpuProfile> getProfilesByParentEntity() {
        return cpuProfileDao.getAllForCluster(getCluster().getId());
    }

    @Override
    public List<VmTemplate> getTemplatesUsingProfile() {
        return vmTemplateDao.getAllForCpuProfile(getProfile().getId());
    }

    @Override
    public List<VM> getVmsUsingProfile() {
        return vmDao.getAllForCpuProfiles(Collections.singletonList(getProfile().getId()));
    }
}
