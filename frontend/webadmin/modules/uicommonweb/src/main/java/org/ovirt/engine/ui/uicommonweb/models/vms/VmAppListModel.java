package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmAppListModel<E> extends SearchableListModel<E, String> {
    @Override
    public void setItems(Collection<String> value) {
        if (items != value) {
            itemsChanging(value, items);
            items = value;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    public VmAppListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().applicationsTitle());
        setHelpTag(HelpTag.applications);
        setHashName("applications"); //$NON-NLS-1$
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        if (e.propertyName.equals("appList")) { //$NON-NLS-1$
            updateAppList();
        }
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        updateAppList();
    }

    protected void updateAppList() {
        if (getEntity() instanceof VM) {
            updateAppListFromVm((VM) getEntity());
        } else {
            VmPool pool = (VmPool) getEntity();
            if (pool != null) {
                Frontend.getInstance().runQuery(QueryType.GetVmDataByPoolId,
                        new IdQueryParameters(pool.getVmPoolId()),
                        new AsyncQuery<QueryReturnValue>(result -> {
                            if (result != null) {
                                VM vm = result.getReturnValue();
                                if (vm != null) {
                                    updateAppListFromVm(vm);
                                }
                            }
                        }));
            }
        }
    }

    private void updateAppListFromVm(VM vm) {
        setItems(null);
        if (vm != null && vm.getAppList() != null) {
            ArrayList<String> list = new ArrayList<>();

            String[] array = vm.getAppList().split("[,]", -1); //$NON-NLS-1$
            for (String item : array) {
                list.add(item);
            }
            Collections.sort(list);

            setItems(list);
        } else {
            setItems(new ArrayList<String>());
        }
    }

    @Override
    protected void syncSearch() {
        updateAppList();
        setIsQueryFirstTime(false);
    }

    @Override
    protected String getListName() {
        return "VmAppListModel"; //$NON-NLS-1$
    }
}
