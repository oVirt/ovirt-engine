package org.ovirt.engine.core.bll.storage.connection;

import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ConnectDomainToStorageCommand<T extends StorageDomainPoolParametersBase> extends StorageDomainCommandBase<T> {

    public ConnectDomainToStorageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        log.info("ConnectDomainToStorage. Before Connect all hosts to pool. Time: {}", new Date());
        connectHostsInUpToDomainStorageServer();
        log.info("ConnectDomainToStorage. After Connect all hosts to pool. Time: {}", new Date());
    }
}
