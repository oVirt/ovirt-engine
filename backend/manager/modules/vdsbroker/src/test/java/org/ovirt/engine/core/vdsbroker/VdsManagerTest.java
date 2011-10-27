package org.ovirt.engine.core.vdsbroker;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class VdsManagerTest {

    @Ignore
    @Test
    public void testConstructor() {
        try {
            // TODO : replace withreal Guid ID
            VDS vds = DbFacade.getInstance().getVdsDAO().get(Guid.Empty);
            // VDS vds = new VDS();
            // vds.setvds_id(25);
            VdsManager manager = VdsManager.buildVdsManager(vds);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            System.out.println("after");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
