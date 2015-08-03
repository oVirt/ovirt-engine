package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

/**
 * It creates instances of {@link VmConsoles} and configures them according settings stored in client.
 */
public class ConsolesFactory {

    private final Model parentModel;
    private final ConsoleContext consoleContext;
    private final ConsoleOptionsFrontendPersister persister;

    public ConsolesFactory(ConsoleContext consoleContext, Model parentModel) {
        this.consoleContext = consoleContext;
        this.parentModel = parentModel;
        persister = (ConsoleOptionsFrontendPersister) TypeResolver.getInstance().resolve(ConsoleOptionsFrontendPersister.class);
    }

    public VmConsoles getVmConsolesForPool(VM poolRepresentative) {
        VmConsoles console = new PoolConsolesImpl(poolRepresentative, parentModel, consoleContext);
        persister.loadFromLocalStorage(console);
        return console;
    }

    public VmConsoles getVmConsolesForVm(VM vm) {
        VmConsoles console = new VmConsolesImpl(vm, parentModel, consoleContext);
        persister.loadFromLocalStorage(console);
        return console;
    }


}
