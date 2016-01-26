package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.presenter.popup.ConsoleModelChangedEvent;
import org.ovirt.engine.ui.common.presenter.popup.ConsoleModelChangedEvent.ConsoleModelChangedHandler;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.UserPortalBasicListProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainTabBasicDetailsPresenterWidget extends PresenterWidget<MainTabBasicDetailsPresenterWidget.ViewDef> {

    public interface ViewDef extends View, HasEditorDriver<UserPortalBasicListModel> {

        void editDistItems(Iterable<DiskImage> diskImages);

        void setConsoleProtocolMessage(String protocol);

        void setConsoleConnectLinkEnabled(boolean enabled);

        void setEditConsoleEnabled(boolean enabled);

        HasClickHandlers getConsoleConnectAnchor();

        HasClickHandlers getEditButton();

        HasClickHandlers getConsoleClientResourcesAnchor();

        void clear();

        void displayVmOsImages(boolean dispaly);
    }

    private final DynamicMessages dynamicMessages;
    private final ErrorPopupManager errorPopupManager;
    private final Map<ConsoleProtocol, String> consoleTypeToName = new HashMap<>();

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public MainTabBasicDetailsPresenterWidget(EventBus eventBus,
            ViewDef view,
            final UserPortalBasicListProvider modelProvider,
            final DynamicMessages dynamicMessages,
            final ErrorPopupManager errorPopupManager) {
        super(eventBus, view);
        this.dynamicMessages = dynamicMessages;
        this.errorPopupManager = errorPopupManager;

        initConsoleTypeToNameMap();

        listenOnSelectedItemEvent(modelProvider);

        listenOnDiskModelChangeEvent(modelProvider);

        listenOnConsoleConnectAnchor(modelProvider);
        listenOnEditButton(modelProvider);
        listenOnConsoleClientResourcesAnchor();

        listenOnConsoleModelChangeEvent(eventBus, modelProvider);

        getEventBus().addHandler(UserPortalModelInitEvent.getType(), new UserPortalModelInitHandler() {

            @Override
            public void onUserPortalModelInit(UserPortalModelInitEvent event) {
                listenOnSelectedItemEvent(modelProvider);
                listenOnDiskModelChangeEvent(modelProvider);
            }

        });
    }

    private void listenOnConsoleConnectAnchor(final UserPortalBasicListProvider modelProvider) {
        registerHandler(getView().getConsoleConnectAnchor().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                try {
                    VmConsoles vmConsoles = modelProvider.getModel().getSelectedItem().getVmConsoles();
                    if (vmConsoles.canConnectToConsole()) {
                        vmConsoles.connect();
                    }
                } catch (VmConsoles.ConsoleConnectException e) {
                    errorPopupManager.show(e.getLocalizedErrorMessage());
                }
            }
        }));
    }

    private void listenOnConsoleClientResourcesAnchor() {
        registerHandler(getView().getConsoleClientResourcesAnchor().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(dynamicMessages.consoleClientResourcesUrl(), "_blank", "resizable=yes,scrollbars=yes"); //$NON-NLS-1$ $NON-NLS-2$
            }
        }));
    }

    private void initConsoleTypeToNameMap() {
        consoleTypeToName.put(ConsoleProtocol.SPICE, constants.spice());
        consoleTypeToName.put(ConsoleProtocol.RDP, constants.remoteDesktop());
        consoleTypeToName.put(ConsoleProtocol.VNC, constants.vnc());
    }

    protected void listenOnConsoleModelChangeEvent(EventBus eventBus, final UserPortalBasicListProvider modelProvider) {
        eventBus.addHandler(ConsoleModelChangedEvent.getType(), new ConsoleModelChangedHandler() {

            @Override
            public void onConsoleModelChanged(ConsoleModelChangedEvent event) {
                if (modelProvider.getModel().getSelectedItem() == null) {
                    return;
                }

                setupConsole(modelProvider);
            }
        });
    }

    private void listenOnDiskModelChangeEvent(final UserPortalBasicListProvider modelProvider) {
        modelProvider.getModel().getVmBasicDiskListModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (modelProvider.getModel().getSelectedItem() == null) {
                    return;
                }
                setupDisks(modelProvider);
            }
        });
    }

    private void listenOnSelectedItemEvent(final UserPortalBasicListProvider modelProvider) {
        modelProvider.getModel().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (modelProvider.getModel().getSelectedItem() == null) {
                    getView().clear();
                    return;
                }
                getView().edit(modelProvider.getModel());
                getView().displayVmOsImages(true);
                setupDisks(modelProvider);
                setupConsole(modelProvider);
            }

        });
    }

    private void listenOnEditButton(final UserPortalBasicListProvider modelProvider) {
        registerHandler(getView().getEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!isEditConsoleEnabled(modelProvider.getModel().getSelectedItem())) {
                    return;
                }
                modelProvider.getModel().getEditConsoleCommand().execute();
            }
        }));
    }

    private void setupDisks(final UserPortalBasicListProvider modelProvider) {
        @SuppressWarnings("unchecked")
        Iterable<DiskImage> diskImages = modelProvider.getModel().getVmBasicDiskListModel().getItems();
        if (diskImages != null) {
            getView().editDistItems(diskImages);
        }
    }

    private void setupConsole(final UserPortalBasicListProvider modelProvider) {
        UserPortalItemModel item = modelProvider.getModel().getSelectedItem();

        getView().setEditConsoleEnabled(isEditConsoleEnabled(item));
        getView().setConsoleConnectLinkEnabled(canConnectToConsole(item));
        getView().setConsoleProtocolMessage(determineProtocolMessage(item));
    }

    private boolean canConnectToConsole(UserPortalItemModel item) {
        if (item == null) {
            return false;
        }

        return item.getVmConsoles().canConnectToConsole();
    }

    private String determineProtocolMessage(UserPortalItemModel item) {
        VmConsoles vmConsoles = (item == null)
            ? null
            : item.getVmConsoles();

        if (item == null || vmConsoles == null || !canConnectToConsole(item)) {
            return "";
        }

        ConsoleProtocol selectedProcotol = vmConsoles.getSelectedProcotol();
        boolean smartcardEnabled = selectedProcotol == ConsoleProtocol.SPICE && vmConsoles.getVm().isSmartcardEnabled();
        boolean smartcardOverriden = vmConsoles.getConsoleModel(SpiceConsoleModel.class).getspice().getOptions().isSmartcardEnabledOverridden();

        if (smartcardEnabled && !smartcardOverriden) {
            return messages.consoleWithSmartcard(consoleTypeToName.get(selectedProcotol));
        }

        return consoleTypeToName.get(selectedProcotol);
    }

    private boolean isEditConsoleEnabled(UserPortalItemModel item) {
        if (item == null) {
            return false;
        }

        return item.isPool() ||
                (item.getVM() != null && item.getVM().isRunningOrPaused());
    }

}

