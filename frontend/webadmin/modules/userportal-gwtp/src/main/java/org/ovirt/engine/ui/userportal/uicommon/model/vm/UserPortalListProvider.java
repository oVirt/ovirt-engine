package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceToGuestWithNonRespAgentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDesktopNewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmServerNewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.AbstractUserPortalListProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalListProvider extends AbstractUserPortalListProvider<UserPortalListModel> {

    private final Provider<VmDesktopNewPopupPresenterWidget> newDesktopVmPopupProvider;
    private final Provider<VmServerNewPopupPresenterWidget> newServerVmPopupProvider;
    private final Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider;
    private final Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider;
    private final Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;
    private final Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider;
    private final Provider<ConsolePopupPresenterWidget> consolePopupProvider;
    private final Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider;

    @Inject
    public UserPortalListProvider(ClientGinjector ginjector,
            Provider<VmDesktopNewPopupPresenterWidget> newDesktopVmPopupProvider,
            Provider<VmServerNewPopupPresenterWidget> newServerVmPopupProvider,
            Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider,
            Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider,
            Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider,
            CurrentUser user,
            Provider<ConsolePopupPresenterWidget> consolePopupProvider) {
        super(ginjector, user);
        this.newDesktopVmPopupProvider = newDesktopVmPopupProvider;
        this.newServerVmPopupProvider = newServerVmPopupProvider;
        this.runOncePopupProvider = runOncePopupProvider;
        this.changeCDPopupProvider = changeCDPopupProvider;
        this.makeTemplatePopupProvider = makeTemplatePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.vncInfoPopupProvider = vncInfoPopupProvider;
        this.consolePopupProvider = consolePopupProvider;
        this.spiceToGuestWithNonRespAgentPopupProvider = spiceToGuestWithNonRespAgentPopupProvider;
    }

    @Override
    protected UserPortalListModel createModel() {
        return new UserPortalListModel();
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewTemplateCommand()) {
            return makeTemplatePopupProvider.get();
        } else if (lastExecutedCommand == getModel().getRunOnceCommand()) {
            return runOncePopupProvider.get();
        } else if (lastExecutedCommand == getModel().getChangeCdCommand()) {
            return changeCDPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getNewDesktopCommand()) {
            return newDesktopVmPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getNewServerCommand()) {
            return newServerVmPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditCommand()) {
            UnitVmModel vm = (UnitVmModel) getModel().getWindow();
            if (vm.getVmType().equals(VmType.Desktop)) {
                return newDesktopVmPopupProvider.get();
            } else {
                return newServerVmPopupProvider.get();
            }
        } else if (windowModel instanceof VncInfoModel) {
            return vncInfoPopupProvider.get();
        } else if (windowModel instanceof SpiceToGuestWithNonRespAgentModel) {
            return spiceToGuestWithNonRespAgentPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditConsoleCommand()) {
            return consolePopupProvider.get();
        }
        else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UserPortalListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
