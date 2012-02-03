package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleProtocol;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleUtils;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalBasicListProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainTabBasicDetailsPresenterWidget extends PresenterWidget<MainTabBasicDetailsPresenterWidget.ViewDef> {

    public interface ViewDef extends View, HasEditorDriver<UserPortalBasicListModel> {

        void editDistItems(Iterable<DiskImage> diskImages);

        void setConsoleWarningMessage(String message);

        void setConsoleProtocol(String protocol);

        void setEditEnabled(boolean enabled);
    }

    @Inject
    public MainTabBasicDetailsPresenterWidget(EventBus eventBus,
            ViewDef view,
            final UserPortalBasicListProvider modelProvider,
            final ConsoleUtils consoleUtils) {
        super(eventBus, view);

        // TODO check if this works on logout-login
        modelProvider.getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (modelProvider.getModel().getSelectedItem() == null) {
                    return;
                }
                getView().edit(modelProvider.getModel());
                setupProtocol(modelProvider, consoleUtils);
            }

        });

        modelProvider.getModel().getvmBasicDiskListModel().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (modelProvider.getModel().getSelectedItem() == null) {
                    return;
                }
                @SuppressWarnings("unchecked")
                Iterable<DiskImage> diskImages = modelProvider.getModel().getvmBasicDiskListModel().getItems();
                getView().editDistItems(diskImages);
            }
        });

    }

    private void setupProtocol(final UserPortalBasicListProvider modelProvider,
            final ConsoleUtils consoleUtils) {
        UserPortalItemModel item = modelProvider.getModel().getSelectedItem();
        if (!item.getIsPool()) {
            ConsoleProtocol protocol = consoleUtils.determineDefaultProtocol(item);
            if (protocol == null) {
                getView().setConsoleWarningMessage(consoleUtils.determineProtocolMessage(item));
                getView().setEditEnabled(false);
            } else {
                getView().setConsoleProtocol(protocol == null ? "" : protocol.displayName);
                getView().setEditEnabled(true);
            }
        } else {
            getView().setConsoleProtocol("");
            getView().setEditEnabled(false);
        }
    }
}
