package org.ovirt.engine.core.bll.profiles;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;


public class CpuProfileValidator extends ProfileValidator<CpuProfile> {
    private VDSGroup cluster;

    public CpuProfileValidator(CpuProfile profile) {
        super(profile);
    }

    public CpuProfileValidator(Guid profileId) {
        super(profileId);
    }

    @Override
    public ValidationResult parentEntityExists() {
        if (DbFacade.getInstance().getVdsGroupDao().get(getProfile().getClusterId()) == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult parentEntityNotChanged() {
        if (ObjectUtils.equals(getProfile().getClusterId(), getProfileFromDb().getClusterId())) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_PROFILE);
    }

    protected VDSGroup getCluster() {
        if (cluster == null) {
            cluster = getDbFacade().getVdsGroupDao().get(getProfile().getClusterId());
        }

        return cluster;
    }

    @Override
    public ValidationResult isParentEntityValid(Guid clusterId) {
        if (clusterId == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_CPU_PROFILE_CLUSTER_NOT_PROVIDED);
        }
        Guid id = getProfile().getId();
        if (id == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_CPU_PROFILE_EMPTY);
        }
        CpuProfile fetchedCpuProfile = getProfileDao().get(id);
        if (fetchedCpuProfile == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CPU_PROFILE_NOT_FOUND);
        }
        if (!clusterId.equals(fetchedCpuProfile.getClusterId())) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_CPU_PROFILE_NOT_MATCH_CLUSTER);
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult isLastProfileInParentEntity() {
        if (getProfileDao().getAllForCluster(getProfile().getClusterId()).size() == 1) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_CANNOT_REMOVE_LAST_CPU_PROFILE_IN_CLUSTER);
        }
        return ValidationResult.VALID;
    }

    @Override
    protected CpuProfileDao getProfileDao() {
        return getDbFacade().getCpuProfileDao();
    }

    @Override
    protected List<CpuProfile> getProfilesByParentEntity() {
        return getDbFacade().getCpuProfileDao().getAllForCluster(getCluster().getId());
    }
}
