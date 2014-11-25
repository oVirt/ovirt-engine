package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedVirtualMachineSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public abstract class AbstractSubTabExtendedVmPresenter<D extends EntityModel, V
    extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<UserPortalItemModel, UserPortalListModel, D, V, P> {

    public AbstractSubTabExtendedVmPresenter(EventBus eventBus, V view, P proxy,
            PlaceManager placeManager, DetailModelProvider<UserPortalListModel, D> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                ExtendedVmSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(UserPortalApplicationPlaces.extendedVirtualMachineSideTabPlace);
    }

    @ProxyEvent
    public void onExtendedVirtualMachineSelectionChange(ExtendedVirtualMachineSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
