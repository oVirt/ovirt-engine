package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VfsConfigValidator;
import org.ovirt.engine.core.common.action.VfsConfigBaseParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public abstract class VfsConfigCommandBase<T extends VfsConfigBaseParameters> extends VdsCommand<T> {

    private VdsNetworkInterface nic;
    private HostNicVfsConfig oldVfsConfig;
    private VfsConfigValidator vfsConfigValidator;

    @Inject
    private HostNicVfsConfigDao hostNicVfsConfigDao;
    @Inject
    private InterfaceDao interfaceDao;

    public VfsConfigCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected boolean validate() {
        return validate(getVfsConfigValidator().nicExists()) && validate(getVfsConfigValidator().nicSriovEnabled());
    }

    protected HostNicVfsConfigDao getVfsConfigDao() {
        return hostNicVfsConfigDao;
    }

    public VfsConfigValidator getVfsConfigValidator() {
        if (vfsConfigValidator == null) {
            vfsConfigValidator = new VfsConfigValidator(getParameters().getNicId(), getVfsConfig());
        }
        return vfsConfigValidator;
    }

    public HostNicVfsConfig getVfsConfig() {
        if (oldVfsConfig == null) {
            oldVfsConfig = getVfsConfigDao().getByNicId(getParameters().getNicId());
        }
        return oldVfsConfig;
    }

    public VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = interfaceDao.get(getVfsConfig().getNicId());
        }
        return nic;
    }

    public String getNicName() {
        return getNic().getName();
    }

    @Override
    protected String getDescription() {
        return getNic() == null ? getParameters().getNicId().toString() : getNicName();
    }
}
