package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
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

public class SubTabHostGeneralSoftwarePresenter
    extends AbstractSubTabHostPresenter<HostGeneralModel, SubTabHostGeneralSoftwarePresenter.ViewDef,
        SubTabHostGeneralSoftwarePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGeneralSoftwareSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGeneralSoftwarePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostGeneralSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.hostGeneralSoftwareSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabHostGeneralSoftwarePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, HostMainTabSelectedItems selectedItems,
            DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                HostGeneralSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
