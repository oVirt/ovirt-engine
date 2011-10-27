/**
 *
 */
package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for all migration commands parameter classes Includes a "force migration" flag that indicates that the
 * user requests to perform migration even if the VM is non migratable
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MigrateVmParameters")
public class MigrateVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -7523728706659584319L;
    @XmlElement(name = "forceMigrationForNonMigratableVM")
    protected boolean forceMigrationForNonMigratableVM;

    public MigrateVmParameters() {
    }

    public boolean isForceMigrationForNonMigratableVM() {
        return forceMigrationForNonMigratableVM;
    }

    public void setForceMigrationForNonMigratableVM(boolean forceMigrationForNonMigratableVM) {
        this.forceMigrationForNonMigratableVM = forceMigrationForNonMigratableVM;
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId) {
        super(vmId);
        this.forceMigrationForNonMigratableVM = forceMigrationForNonMigratableVM;
    }

}
