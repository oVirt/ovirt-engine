package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class BalanceVmCommand<T extends MigrateVmParameters> extends MigrateVmCommand<T> {
    private Logger log = LoggerFactory.getLogger(BalanceVmCommand.class);

    public BalanceVmCommand(T migrateVmParameters, CommandContext cmdContext) {
        super(migrateVmParameters, cmdContext);
    }

    @Override
    protected List<Guid> getVdsBlackList() {
        return Collections.unmodifiableList(getRunVdssList());
    }

    @Override
    protected boolean perform() {
        // Fail when same host migration is detected.
        // The balancing will try to migrate a different VM.
        if (getVm().getRunOnVds().equals(getDestinationVdsId())) {
            log.debug("Migration target host is the same as the source host, migration for VM {} will not occur.",
                    getVm().getName());
            setCommandShouldBeLogged(false);
            //In this case, the command should not wait for an answer from VDS
            //in order to release the engine lock.
            freeLock();
            return false;
        }
        return super.perform();
    }
}
