package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.utils.ConsoleManager;
import org.ovirt.engine.ui.common.utils.ConsoleUtils;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class MainTabVirtualMachinePresenter extends AbstractMainTabWithDetailsPresenter<VM, VmListModel, MainTabVirtualMachinePresenter.ViewDef, MainTabVirtualMachinePresenter.ProxyDef> {

    private final ConsoleManager consoleManager;
    private final ErrorPopupManager errorPopupManager;
    private final ConsoleOptionsFrontendPersister consoleOptionsPersister;

    @GenEvent
    public static class VirtualMachineSelectionChange {

        List<VM> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.virtualMachineMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabVirtualMachinePresenter> {
    }

    @ProxyEvent
    public void onUiCommonInit(UiCommonInitEvent event) {
         getModel().getConsoleConnectEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                consoleOptionsPersister.loadFromLocalStorage(getModel(), ConsoleContext.WA);
                String errorMessage = MainTabVirtualMachinePresenter.this.consoleManager.connectToConsole(getModel());
                if (errorMessage != null) {
                    MainTabVirtualMachinePresenter.this.errorPopupManager.show(errorMessage);
                }
            }
        });
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<VM> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().virtualMachineMainTabLabel(), 6,
                ginjector.getMainTabVirtualMachineModelProvider());
    }

    @Inject
    public MainTabVirtualMachinePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VM, VmListModel> modelProvider,
            ConsoleManager consoleManager, ConsoleUtils consoleUtils, ErrorPopupManager errorPopupManager,
            ConsoleOptionsFrontendPersister consoleOptionsPersister) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        this.consoleManager = consoleManager;
        this.errorPopupManager = errorPopupManager;
        this.consoleOptionsPersister = consoleOptionsPersister;
    }



    @Override
    protected void fireTableSelectionChangeEvent() {
        VirtualMachineSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.virtualMachineMainTabPlace);
    }
}
