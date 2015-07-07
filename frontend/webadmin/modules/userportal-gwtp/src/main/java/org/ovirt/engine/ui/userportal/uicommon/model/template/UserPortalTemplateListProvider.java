package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateEditPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableTableModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalTemplateListProvider
    extends UserPortalDataBoundModelProvider<VmTemplate, UserPortalTemplateListModel>
    implements UserPortalSearchableTableModelProvider<VmTemplate, UserPortalTemplateListModel> {

    private final Provider<TemplateEditPopupPresenterWidget> editTemplatePopupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    @Inject
    public UserPortalTemplateListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<TemplateEditPopupPresenterWidget> editTemplatePopupProvider,
            Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.editTemplatePopupProvider = editTemplatePopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalTemplateListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getEditCommand()) {
            return editTemplatePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public void clearCurrentItems() {
        // No-op, if we ever need to optimize the rendering of the templates this will be useful like in the
        // Virtual Machine list provider.
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UserPortalTemplateListModel source, UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }
}
