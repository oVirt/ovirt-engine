package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.RolePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RoleModelProvider extends SearchableTabModelProvider<Role, RoleListModel> {

    private final Provider<RolePopupPresenterWidget> rolePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public RoleModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RolePopupPresenterWidget> rolePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.rolePopupProvider = rolePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(RoleListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand.equals(getModel().getNewCommand())
                || lastExecutedCommand.equals(getModel().getEditCommand())
                || lastExecutedCommand.equals(getModel().getCloneCommand())) {
            return rolePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(RoleListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
