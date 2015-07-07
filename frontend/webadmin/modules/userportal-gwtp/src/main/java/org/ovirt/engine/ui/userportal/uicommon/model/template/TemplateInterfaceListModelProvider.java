package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplateInterfaceListModelProvider
        extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalTemplateListModel, TemplateInterfaceListModel> {

    private final Provider<TemplateInterfacePopupPresenterWidget> newTemplateInterfacePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public TemplateInterfaceListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<TemplateInterfacePopupPresenterWidget> newTemplateInterfacePopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.newTemplateInterfacePopupProvider = newTemplateInterfacePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(TemplateInterfaceListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand()
                || lastExecutedCommand == getModel().getEditCommand()) {
            return newTemplateInterfacePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(TemplateInterfaceListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
