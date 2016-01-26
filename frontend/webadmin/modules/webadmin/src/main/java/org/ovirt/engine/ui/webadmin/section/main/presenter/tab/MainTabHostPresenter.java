package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabHostPresenter extends AbstractMainTabWithDetailsPresenter<VDS, HostListModel<Void>, MainTabHostPresenter.ViewDef, MainTabHostPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @GenEvent
    public class HostSelectionChange {

        List<VDS> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabHostPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(
            MainModelProvider<VDS, HostListModel<Void>> modelProvider) {
        return new ModelBoundTabData(constants.hostMainTabLabel(), 2, modelProvider);
    }

    @Inject
    public MainTabHostPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VDS, HostListModel<Void>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        HostSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.hostMainTabPlace);
    }

    @Override
    protected PlaceRequest getSubTabRequest() {
        HasEntity<?> activeDetailModel = modelProvider.getModel().getActiveDetailModel();
        if (activeDetailModel instanceof HostHardwareGeneralModel || activeDetailModel instanceof AbstractErrataCountModel) {
            //Since the host hardware section has been merged into the general sub sub tab, it no longer has its
            //own place. So we need to make sure it stays on the host-general sub tab, if not it will generate
            //an invalid sub tab and go to the VM main tab.
            String requestToken = getMainTabRequest().getNameToken() + WebAdminApplicationPlaces.SUB_TAB_PREFIX
                    + "general"; //$NON-NLS-1$
            return PlaceRequestFactory.get(requestToken);
        } else {
            return super.getSubTabRequest();
        }
    }
}
