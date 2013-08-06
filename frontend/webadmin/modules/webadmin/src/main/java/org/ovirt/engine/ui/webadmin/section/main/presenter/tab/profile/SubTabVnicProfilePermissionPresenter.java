package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VnicProfileSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVnicProfilePermissionPresenter extends AbstractSubTabPresenter<VnicProfile, VnicProfileListModel, PermissionListModel, SubTabVnicProfilePermissionPresenter.ViewDef, SubTabVnicProfilePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.vnicProfilePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVnicProfilePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VnicProfile> {
    }

    @TabInfo(container = VnicProfileSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<permissions, VnicProfileListModel, PermissionListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.vnicProfilePermissionSubTabLabel(), 2,
                modelProvider);
    }

    @Inject
    public SubTabVnicProfilePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<permissions, VnicProfileListModel, PermissionListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, VnicProfileSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.vnicProfileMainTabPlace);
    }

    @ProxyEvent
    public void onVnicProfileSelectionChange(VnicProfileSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
