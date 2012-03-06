package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDesktopNewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmServerNewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalListModel> {

    private final Provider<VmDesktopNewPopupPresenterWidget> newDesktopVmPopupProvider;
    private final Provider<VmServerNewPopupPresenterWidget> newServerVmPopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public UserPortalListProvider(ClientGinjector ginjector,
            Provider<VmDesktopNewPopupPresenterWidget> newDesktopVmPopupProvider,
            Provider<VmServerNewPopupPresenterWidget> newServerVmPopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(ginjector);
        this.newDesktopVmPopupProvider = newDesktopVmPopupProvider;
        this.newServerVmPopupProvider = newServerVmPopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    protected UserPortalListModel createModel() {
        return new UserPortalListModel();
    }

    @Override
    protected String[] getWindowPropertyNames() {
        return new String[] { "VmModel", "RunOnceModel", "AttachCdModel" };
    }

    @Override
    protected Model getWindowModel(String propertyName) {
        if ("VmModel".equals(propertyName)) {
            return getModel().getVmModel();
        } else if ("RunOnceModel".equals(propertyName)) {
            return getModel().getRunOnceModel();
        } else if ("AttachCdModel".equals(propertyName)) {
            return getModel().getAttachCdModel();
        }

        return null;
    }

    @Override
    protected String[] getConfirmWindowPropertyNames() {
        return new String[] { "ConfirmationModel" };
    }

    @Override
    protected Model getConfirmWindowModel(String propertyName) {
        return getModel().getConfirmationModel();
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getNewTemplateCommand()) {
            // TODO popup bound to UnitVmModel
            return null;
        } else if (lastExecutedCommand == getModel().getRunOnceCommand()) {
            // TODO popup bound to RunOnceModel
            return null;
        } else if (lastExecutedCommand == getModel().getChangeCdCommand()) {
            // TODO popup bound to AttachCdModel
            return null;
        } else if (lastExecutedCommand == getModel().getNewDesktopCommand()) {
            return newDesktopVmPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getNewServerCommand()) {
            return newServerVmPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getEditCommand()) {
            UnitVmModel vm = getModel().getVmModel();
            if (vm.getVmType().equals(VmType.Desktop)) {
                return newDesktopVmPopupProvider.get();
            } else {
                return newServerVmPopupProvider.get();
            }
        } else {
            return super.getModelPopup(lastExecutedCommand);
        }
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(lastExecutedCommand);
        }
    }

}
