package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmInterfaceListModelProvider extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, VmInterfaceListModel> {

    private final Provider<VmInterfacePopupPresenterWidget> newTemplateInterfacePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public VmInterfaceListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<VmInterfacePopupPresenterWidget> newTemplateInterfacePopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.newTemplateInterfacePopupProvider = newTemplateInterfacePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VmInterfaceListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand() || lastExecutedCommand == getModel().getEditCommand()) {
            return newTemplateInterfacePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VmInterfaceListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
