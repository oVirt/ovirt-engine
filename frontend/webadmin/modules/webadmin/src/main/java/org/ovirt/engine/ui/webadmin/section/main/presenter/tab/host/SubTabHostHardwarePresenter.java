package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostHardwarePresenter extends AbstractSubTabPresenter<VDS, HostListModel, HostHardwareGeneralModel, SubTabHostHardwarePresenter.ViewDef, SubTabHostHardwarePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.hostHardwareSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostHardwarePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().hostHardwareSubTabLabel(), 6,
                ginjector.getSubTabHostHardwareModelProvider());
    }

    @Inject
    public SubTabHostHardwarePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DetailModelProvider<HostListModel, HostHardwareGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    public void onUiCommonInit(UiCommonInitEvent event) {
        super.onUiCommonInit(event);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, HostSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.hostMainTabPlace);
    }

    @ProxyEvent
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
