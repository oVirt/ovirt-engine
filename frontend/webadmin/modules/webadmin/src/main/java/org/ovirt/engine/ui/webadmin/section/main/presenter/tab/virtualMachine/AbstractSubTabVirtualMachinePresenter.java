package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class AbstractSubTabVirtualMachinePresenter<D extends HasEntity<?>,
    V extends AbstractSubTabPresenter.ViewDef<VM>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<VM, VmListModel<Void>, D, V, P> {

    public AbstractSubTabVirtualMachinePresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager,
            DetailModelProvider<VmListModel<Void>, D> modelProvider, VirtualMachineMainTabSelectedItems selectedItems,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, slot);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.virtualMachineMainTabPlace);
    }
}
