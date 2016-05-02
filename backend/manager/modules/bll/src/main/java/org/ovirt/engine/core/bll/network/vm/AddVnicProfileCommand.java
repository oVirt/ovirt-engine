package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class AddVnicProfileCommand<T extends AddVnicProfileParameters> extends VnicProfileCommandBase<T> {

    public AddVnicProfileCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Inject
    private NetworkFilterDao networkFilterDao;

    @Override
    protected boolean validate() {
        VnicProfileValidator validator = createVnicProfileValidator();

        return validate(validator.vnicProfileIsSet())
                && validate(validator.networkExists())
                && validate(validator.networkQosExistsOrNull())
                && validate(validator.vnicProfileForVmNetworkOnly())
                && validate(validator.vnicProfileNameNotUsed())
                && validate(validator.portMirroringNotSetIfExternalNetwork())
                && validator.validateCustomProperties(getReturnValue().getValidationMessages())
                && validate(validator.passthroughProfileContainsSupportedProperties())
                && validate(validator.validUseDefaultNetworkFilterFlag(getParameters().isUseDefaultNetworkFiterId()))
                && validate(validator.validNetworkFilterId());
    }

    @Override
    protected void executeCommand() {
        getVnicProfile().setId(Guid.newGuid());
        updateDefaultNetworkFilterIfRequired();
        getVnicProfileDao().save(getVnicProfile());
        NetworkHelper.addPermissionsOnVnicProfile(getCurrentUser().getId(),
                getVnicProfile().getId(),
                getParameters().isPublicUse());
        getReturnValue().setActionReturnValue(getVnicProfile().getId());
        setSucceeded(true);
    }

    private void updateDefaultNetworkFilterIfRequired() {
        if (getParameters().isUseDefaultNetworkFiterId()) {
            final NetworkFilter networkFilter = resolveVnicProfileDefaultNetworkFilter();
            if (networkFilter != null) {
                final Guid networkFilterId = networkFilter.getId();
                setNetworkFilterId(networkFilterId);
            }
        }
    }

    private NetworkFilter resolveVnicProfileDefaultNetworkFilter() {
        if (Config.<Boolean> getValue(ConfigValues.EnableMACAntiSpoofingFilterRules)) {
            return networkFilterDao.getNetworkFilterByName(NetworkFilter.VDSM_NO_MAC_SPOOFING);
        }
        return null;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.ADD_VNIC_PROFILE
                : AuditLogType.ADD_VNIC_PROFILE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid networkId = getVnicProfile() == null ? null : getVnicProfile().getNetworkId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
