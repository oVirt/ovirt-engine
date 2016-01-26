package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostGeneralHardwarePresenter extends
    AbstractSubTabHostPresenter<HostHardwareGeneralModel,
        SubTabHostGeneralHardwarePresenter.ViewDef, SubTabHostGeneralHardwarePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGeneralHardwareSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGeneralHardwarePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostGeneralSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.hostGeneralHardwareSubTabLabel(), 6, modelProvider);
    }

    @Inject
    public SubTabHostGeneralHardwarePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            HostMainTabSelectedItems selectedItems,
            PlaceManager placeManager, DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                HostGeneralSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
