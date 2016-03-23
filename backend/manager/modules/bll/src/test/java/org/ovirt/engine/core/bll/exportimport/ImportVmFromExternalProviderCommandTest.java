package org.ovirt.engine.core.bll.exportimport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OriginType;

public class ImportVmFromExternalProviderCommandTest {

    @Test
    public void renameVmdkImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.VMWARE, "[datastore] Fedora21/Fedora21.vmdk");
        assertEquals(alias, "Fedora21");
    }

    @Test
    public void renameXenImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.XEN, "/home/vdsm/Fedora22.img");
        assertEquals(alias, "Fedora22");
    }
}
