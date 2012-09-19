package org.ovirt.engine.core.bll.storage;

import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ConnectDomainToStorageCommand<T extends StorageDomainPoolParametersBase> extends StorageDomainCommandBase<T> {

    private static final long serialVersionUID = -8725633529829678185L;

    public ConnectDomainToStorageCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        log.infoFormat("ConnectDomainToStorage. Before Connect all hosts to pool. Time:{0}", new Date());
        ConnectAllHostsToPool();
        log.infoFormat("ConnectDomainToStorage. After Connect all hosts to pool. Time:{0}", new Date());
    }
}
