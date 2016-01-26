package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmGuestInfoPresenter
      extends AbstractSubTabExtendedVmPresenter<VmGuestInfoModel, SubTabExtendedVmGuestInfoPresenter.ViewDef,
          SubTabExtendedVmGuestInfoPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedVirtualMachineGuestInfoSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmGuestInfoPresenter> {

    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
        public void update();
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(
            UserPortalDetailModelProvider<UserPortalListModel, VmGuestInfoModel> modelProvider) {
        return new ModelBoundTabData(constants.extendedVirtualMachineGuestInfoSubTabLabel(), 11, modelProvider);
    }

    @Inject
    public SubTabExtendedVmGuestInfoPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ExtendedVmMainTabSelectedItems selectedItems,
            UserPortalDetailModelProvider<UserPortalListModel, VmGuestInfoModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, selectedItems, modelProvider);
    }

    @Override
    protected void onDetailModelEntityChange(Object entity) {
        getView().update();
    }

}
