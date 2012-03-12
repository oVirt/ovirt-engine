package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateNewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalTemplateListProvider extends UserPortalDataBoundModelProvider<VmTemplate, UserPortalTemplateListModel> {

    private final Provider<TemplateNewPopupPresenterWidget> newTemplatePopupProvider;

    @Inject
    public UserPortalTemplateListProvider(ClientGinjector ginjector,
            Provider<TemplateNewPopupPresenterWidget> newTemplatePopupProvider) {
        super(ginjector);

        this.newTemplatePopupProvider = newTemplatePopupProvider;
    }

    @Override
    protected UserPortalTemplateListModel createModel() {
        return new UserPortalTemplateListModel();
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {

        if (lastExecutedCommand == getModel().getEditCommand()) {
            return newTemplatePopupProvider.get();
        }

        return super.getModelPopup(lastExecutedCommand);
    }

}
