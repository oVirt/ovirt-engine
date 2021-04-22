package org.ovirt.engine.core.bll.network.vm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

public class UpdateVnicProfileCommand<T extends VnicProfileParameters>
        extends VnicProfileCommandBase<T> implements RenamedEntityInfoProvider {

    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private AuditLogDirector auditLogDirector;

    private VnicProfile oldVnicProfile;

    public UpdateVnicProfileCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VnicProfileValidator validator = createVnicProfileValidator();
        return validate(validator.vnicProfileIsSet())
                && validate(validator.vnicProfileExists())
                && validate(validator.vnicProfileNameNotUsed())
                && validate(validator.networkNotChanged())
                && validate(validator.networkQosExistsOrNull())
                && validate(validator.portMirroringNotChangedIfUsedByVms())
                && validate(validator.portMirroringNotSetIfExternalNetwork())
                && validator.validateCustomProperties(getReturnValue().getValidationMessages())
                && validate(validator.passthroughNotChangedIfUsedByVms())
                && validate(validator.passthroughProfileContainsSupportedProperties())
                && validate(validator.passthroughProfileNoPortIsolation())
                && validate(validator.validNetworkFilterId())
                && validate(validator.validFailoverId())
                && validate(validator.validateProfileNotUpdatedIfFailover())
                && validate(validator.failoverNotChangedIfUsedByVms());
    }

    @Override
    protected void executeCommand() {
        getOldVnicProfile();
        markVnicsOutOfSync();
        vnicProfileDao.update(getVnicProfile());
        setSucceeded(true);
    }

    private void markVnicsOutOfSync() {
        if (isNonLiveUpdate()) {
            var vnics = vmNicDao.getActiveForVnicProfile(getVnicProfile().getId());
            vnics.forEach(vnic -> vnic.setSynced(false));
            vmNicDao.updateAllInBatch(vnics);
            logVnicsOutOfSync(vnics);
        }
    }

    private boolean isNonLiveUpdate() {
        var prev = getOldVnicProfile();
        var curr = getVnicProfile();
        return !Objects.equals(prev.getNetworkId(), curr.getNetworkId()) ||
            !Objects.equals(prev.getNetworkQosId(), curr.getNetworkQosId()) ||
            !Objects.equals(prev.getCustomProperties(), curr.getCustomProperties()) ||
            !Objects.equals(prev.getNetworkFilterId(), curr.getNetworkFilterId()) ||
            !Objects.equals(prev.getFailoverVnicProfileId(), curr.getFailoverVnicProfileId());
    }

    private void logVnicsOutOfSync(List<VmNic> vnics) {
        var logable = new AuditLogableImpl();
        logable.addCustomValue("ProfileName", getVnicProfileName());
        logable.addCustomValue("VnicNames", vnics.stream().map(VmNic::getName).collect(Collectors.joining(",")));
        auditLogDirector.log(logable, AuditLogType.VNICS_OUT_OF_SYNC_ON_PROFILE_UPDATE);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UPDATE_VNIC_PROFILE
                : AuditLogType.UPDATE_VNIC_PROFILE_FAILED;
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VnicProfile.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldVnicProfile().getName();
    }

    @Override
    public String getEntityNewName() {
        return getVnicProfile().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {
    }

    private VnicProfile getOldVnicProfile() {
        if (oldVnicProfile == null) {
            oldVnicProfile = vnicProfileDao.get(getVnicProfile().getId());
        }

        return oldVnicProfile;
    }
}
