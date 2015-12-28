package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
        AsyncDataProvider.getInstance().getHostListByClusterId(new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                getPinnedHost().setItems((Collection<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue());
                stopProgress();
                selectCurrentPinnedHost();
            }
        }), vm.getClusterId());
    }

    private void selectCurrentPinnedHost() {
        VDS host = Linq.findHostByIdFromIdList(getPinnedHost().getItems(), vm.getDedicatedVmForVdsList());
        if (host != null) {
            getPinnedHost().setSelectedItem(host);
        }
    }
}
