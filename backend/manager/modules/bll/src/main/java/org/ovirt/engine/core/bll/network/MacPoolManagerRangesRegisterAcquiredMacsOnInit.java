package org.ovirt.engine.core.bll.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;

class MacPoolManagerRangesRegisterAcquiredMacsOnInit extends MacPoolManagerRanges {
    public MacPoolManagerRangesRegisterAcquiredMacsOnInit(String rangesString, Boolean allowDuplicates) {
        super(MacAddressRangeUtils.parseRangeString(rangesString), allowDuplicates);
    }

    @Override
    protected void onInit() {
        loadAcquiredMacsFromDb();
    }

    private void loadAcquiredMacsFromDb() {
        List<VmNic> interfaces = DbFacade.getInstance().getVmNicDao().getAll();

        for (VmNic vmNic : interfaces) {
            final String macAddress = vmNic.getMacAddress();
            if (macAddress != null) {
                forceAddMacWithoutLocking(macAddress);
            }
        }
    }

}
