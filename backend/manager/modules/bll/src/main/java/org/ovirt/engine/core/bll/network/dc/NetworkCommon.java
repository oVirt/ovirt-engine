package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

public abstract class NetworkCommon<T extends ActionParametersBase> extends CommandBase<T> {

    @Inject
    private VnicProfileDao vnicProfileDao;

    @Inject
    protected NetworkHelper networkHelper;

    public NetworkCommon(Guid id) {
        super(id);
    }

    public NetworkCommon(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected abstract Network getNetwork();

    public String getNetworkName() {
        return getNetwork().getName();
    }

    protected void removeVnicProfiles() {
        List<VnicProfile> profiles = vnicProfileDao.getAllForNetwork(getNetwork().getId());
        for (VnicProfile vnicProfile : profiles) {
            getCompensationContext().snapshotEntity(vnicProfile);
            vnicProfileDao.remove(vnicProfile.getId());
        }
    }

    protected void updateDefaultVnicProfileName(String oldVnicProfileName) {
        Optional<VnicProfile> defaultVnicProfileOption = vnicProfileDao.getAllForNetwork(getNetwork().getId())
                .stream()
                .filter(profile -> profile.getName().equals(oldVnicProfileName))
                .findFirst();
        if (defaultVnicProfileOption.isPresent()) {
            VnicProfile defaultVnicProfile = defaultVnicProfileOption.get();
            defaultVnicProfile.setName(getNetworkName());
            vnicProfileDao.update(defaultVnicProfile);
        }

    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
