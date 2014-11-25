package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmSessionsPresenter
  extends AbstractSubTabExtendedVmPresenter<VmSessionsModel, SubTabExtendedVmSessionsPresenter.ViewDef, SubTabExtendedVmSessionsPresenter.ProxyDef>
{

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedVirtualMachineSessionsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmSessionsPresenter> {

    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
        public void update();
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.extendedVirtualMachineSessionsSubTabLabel(), 11, modelProvider);
    }

    @Inject
    public SubTabExtendedVmSessionsPresenter(EventBus eventBus,
            ViewDef view,
            ProxyDef proxy,
            PlaceManager placeManager,
            UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void onDetailModelEntityChange(Object entity) {
        getView().update();
    }

}
