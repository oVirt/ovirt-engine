package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostPermissionPresenter extends AbstractSubTabPresenter<VDS, HostListModel,
    PermissionListModel<HostListModel>, SubTabHostPermissionPresenter.ViewDef, SubTabHostPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.hostPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<Permissions, HostListModel,
            PermissionListModel<HostListModel>> modelProvider) {
        return new ModelBoundTabData(applicationConstants.hostPermissionSubTabLabel(), 5, modelProvider);
    }

    @Inject
    public SubTabHostPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<Permissions, HostListModel,
            PermissionListModel<HostListModel>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.dataCenterMainTabPlace);
    }

    @ProxyEvent
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
