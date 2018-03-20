package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class ModelWithPinnedHost extends Model {

    private ListModel<VDS> pinnedHost;

    private VM vm;

    public ModelWithPinnedHost() {
        setPinnedHost(new ListModel<VDS>());
    }

    public void init(VM vm) {
        this.vm = vm;
    }

    public ListModel<VDS> getPinnedHost() {
        return pinnedHost;
    }

    private void setPinnedHost(ListModel<VDS> pinnedHost) {
        this.pinnedHost = pinnedHost;
    }

    public VM getVm() {
        return vm;
    }

    protected void initHosts() {
        startProgress();
        AsyncDataProvider.getInstance().getHostListByClusterId(new AsyncQuery<>(hosts -> {
            getPinnedHost().setItems(hosts);
            stopProgress();
            selectCurrentPinnedHost();
        }), vm.getClusterId());
    }

    private void selectCurrentPinnedHost() {
        getPinnedHost().getItems()
                .stream()
                .filter(new Linq.IdsPredicate<>(vm.getDedicatedVmForVdsList()))
                .findFirst()
                .ifPresent(vds -> getPinnedHost().setSelectedItem(vds));
    }
}
