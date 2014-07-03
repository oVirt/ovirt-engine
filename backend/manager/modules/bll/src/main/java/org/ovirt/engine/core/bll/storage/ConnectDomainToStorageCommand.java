package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ConnectDomainToStorageCommand<T extends StorageDomainPoolParametersBase> extends StorageDomainCommandBase<T> {

    public ConnectDomainToStorageCommand(T parameters) {
        super(parameters, null);
    }

    public ConnectDomainToStorageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        log.infoFormat("ConnectDomainToStorage. Before Connect all hosts to pool. Time:{0}", new Date());
        connectAllHostsToPool();
        log.infoFormat("ConnectDomainToStorage. After Connect all hosts to pool. Time:{0}", new Date());
    }
}
