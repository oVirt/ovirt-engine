package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImportVmFromExternalProviderCommandTest {

    @Test
    public void renameVmdkImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias("[datastore] Fedora21/Fedora21.vmdk");
        assertEquals(alias, "Fedora21");
    }
}
