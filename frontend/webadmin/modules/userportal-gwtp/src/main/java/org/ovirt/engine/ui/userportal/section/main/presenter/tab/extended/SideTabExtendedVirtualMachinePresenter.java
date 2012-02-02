package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabTablePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SideTabExtendedVirtualMachinePresenter extends AbstractSideTabTablePresenter<UserPortalItemModel, UserPortalListModel, SideTabExtendedVirtualMachinePresenter.ViewDef, SideTabExtendedVirtualMachinePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualMachineSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedVirtualMachinePresenter> {
    }

    public interface ViewDef extends AbstractSideTabTablePresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedVirtualMachineSideTabLabel(), 0);
    }

    @Inject
    public SideTabExtendedVirtualMachinePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            UserPortalListProvider modelProvider) {
        super(eventBus, view, proxy, modelProvider);
    }

}
