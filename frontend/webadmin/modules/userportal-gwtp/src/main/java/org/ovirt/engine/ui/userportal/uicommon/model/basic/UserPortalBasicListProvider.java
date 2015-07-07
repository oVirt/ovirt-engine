package org.ovirt.engine.ui.userportal.uicommon.model.basic;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceToGuestWithNonRespAgentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.AbstractUserPortalListProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalBasicListProvider extends AbstractUserPortalListProvider<UserPortalBasicListModel> {

    private final Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider;
    private final Provider<ConsolePopupPresenterWidget> consolePopupProvider;
    private final Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider;

    @Inject
    public UserPortalBasicListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider,
            Provider<ConsolePopupPresenterWidget> consolePopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.vncInfoPopupProvider = vncInfoPopupProvider;
        this.consolePopupProvider = consolePopupProvider;
        this.spiceToGuestWithNonRespAgentPopupProvider = spiceToGuestWithNonRespAgentPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalBasicListModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (windowModel instanceof VncInfoModel) {
            return vncInfoPopupProvider.get();
        } else if (windowModel instanceof SpiceToGuestWithNonRespAgentModel) {
            return spiceToGuestWithNonRespAgentPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditConsoleCommand()) {
            return consolePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

}
