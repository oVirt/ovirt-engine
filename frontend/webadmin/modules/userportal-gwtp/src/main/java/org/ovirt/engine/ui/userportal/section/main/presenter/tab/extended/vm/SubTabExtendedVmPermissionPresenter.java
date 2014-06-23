package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.configure.UserPortalPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmPermissionListModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmPermissionPresenter
        extends AbstractSubTabExtendedVmPresenter<UserPortalPermissionListModel<UserPortalListModel>,
        SubTabExtendedVmPermissionPresenter.ViewDef, SubTabExtendedVmPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualMachinePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants) {
        return new TabDataBasic(applicationConstants.extendedVirtualMachinePermissionSubTabLabel(),
                7);
    }

    @Inject
    public SubTabExtendedVmPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VmPermissionListModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

}
