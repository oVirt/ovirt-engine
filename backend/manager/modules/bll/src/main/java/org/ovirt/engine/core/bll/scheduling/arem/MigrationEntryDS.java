package org.ovirt.engine.core.bll.scheduling.arem;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationEntryDS {
    //Definitions
    //Fields
    Guid vm;
    Guid sourceHost;
    VdcReturnValueBase migrationReturnValue;
    VDS targetHost;

    private static final Logger log = LoggerFactory.getLogger(MigrationEntryDS.class);

    //Constructors
    public MigrationEntryDS(Guid vm, Guid sourceHost) {
        this.sourceHost = sourceHost;
        this.vm = vm;
    }

    //Methods
    public Guid getCurrentVm() {
        return vm;
    }

    public MigrationEntryDS oppositeMigration() {
        MigrationEntryDS oppositeMigration = new MigrationEntryDS(getCurrentVm(), getMigrationHost().getId());

        return oppositeMigration;
    }

    public void setMigrationReturnValue(VdcReturnValueBase returnValue) {
        this.migrationReturnValue = returnValue;
    }

    /**
     *
     * @return - Null if no targetHost specified or the targetHost if it is.
     */
    public VDS getMigrationHost() {
        return this.targetHost;
    }

    /**
     *
     * @return - Null if there is no migration status yet or the migration status if there is.
     */
    public VdcReturnValueBase getMigrationStatus() {
        return this.migrationReturnValue;
    }

    public void setTargetHost(VDS targetHost) {
        this.targetHost = targetHost;

        if(targetHost.getId().equals(sourceHost)) {
            log.error("Trying to set target host to be source host.");
        }
    }
}
