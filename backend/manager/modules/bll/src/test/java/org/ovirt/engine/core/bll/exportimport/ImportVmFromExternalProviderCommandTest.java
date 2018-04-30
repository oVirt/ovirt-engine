package org.ovirt.engine.core.bll.exportimport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.OriginType;

public class ImportVmFromExternalProviderCommandTest {

    @Test
    public void renameVmdkImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.VMWARE, "[datastore] Fedora21/Fedora21.vmdk");
        assertEquals("Fedora21", alias);
    }

    @Test
    public void renameXenImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.XEN, "/home/vdsm/Fedora22.img");
        assertEquals("Fedora22", alias);
    }
}
