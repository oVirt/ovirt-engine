package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceToGuestWithNonRespAgentModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.AbstractUserPortalListProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalListProvider extends AbstractUserPortalListProvider<UserPortalListModel> {

    private final Provider<VmPopupPresenterWidget> newVmPopupProvider;
    private final Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider;
    private final Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider;
    private final Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;
    private final Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider;
    private final Provider<ConsolePopupPresenterWidget> consolePopupProvider;
    private final Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider;
    private final Provider<CloneVmPopupPresenterWidget> cloneVmProvider;
    private final Provider<VmNextRunConfigurationPresenterWidget> nextRunProvider;
    private final Provider<VmDiskPopupPresenterWidget> newDiskPopupProvider;
    private final Provider<SingleSelectionVmDiskAttachPopupPresenterWidget> attachDiskPopupProvider;
    private final Provider<DefaultConfirmationPopupPresenterWidget> defaultPopupProvider;

    @Inject
    public UserPortalListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<VmPopupPresenterWidget> newVmPopupProvider,
            Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider,
            Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider,
            Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider,
            Provider<DefaultConfirmationPopupPresenterWidget> spiceToGuestWithNonRespAgentPopupProvider,
            Provider<ConsolePopupPresenterWidget> consolePopupProvider,
            Provider<CloneVmPopupPresenterWidget> cloneVmProvider,
            Provider<VmNextRunConfigurationPresenterWidget> nextRunProvider,
            Provider<VmDiskPopupPresenterWidget> newDiskPopupProvider,
            Provider<SingleSelectionVmDiskAttachPopupPresenterWidget> attachDiskPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.newVmPopupProvider = newVmPopupProvider;
        this.runOncePopupProvider = runOncePopupProvider;
        this.changeCDPopupProvider = changeCDPopupProvider;
        this.makeTemplatePopupProvider = makeTemplatePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.vncInfoPopupProvider = vncInfoPopupProvider;
        this.consolePopupProvider = consolePopupProvider;
        this.spiceToGuestWithNonRespAgentPopupProvider = spiceToGuestWithNonRespAgentPopupProvider;
        this.cloneVmProvider = cloneVmProvider;
        this.nextRunProvider = nextRunProvider;
        this.newDiskPopupProvider = newDiskPopupProvider;
        this.attachDiskPopupProvider = attachDiskPopupProvider;
        this.defaultPopupProvider = defaultConfirmPopupProvider;
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
        } else if (lastExecutedCommand == getModel().getNewVmCommand() || lastExecutedCommand == getModel().getEditCommand()) {
            if (windowModel instanceof AttachDiskModel) {
                return attachDiskPopupProvider.get();
            } else if ((windowModel instanceof NewDiskModel) || (windowModel instanceof EditDiskModel)) {
                return newDiskPopupProvider.get();
            } else {
                return newVmPopupProvider.get();
            }
        } else if (windowModel instanceof VncInfoModel) {
            return vncInfoPopupProvider.get();
        } else if (windowModel instanceof SpiceToGuestWithNonRespAgentModel) {
            return spiceToGuestWithNonRespAgentPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditConsoleCommand()) {
            return consolePopupProvider.get();
        } else if (lastExecutedCommand == getModel().getCloneVmCommand()) {
            return cloneVmProvider.get();
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
        }
        else if (source.getConfirmWindow() instanceof VmNextRunConfigurationModel) {
            return nextRunProvider.get();
        } else if ("OnSave".equals(lastExecutedCommand.getName())) { //$NON-NLS-1$
            return defaultPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
