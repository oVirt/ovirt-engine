package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class QuotaActionPanelPresenterWidget<E> extends ActionPanelPresenterWidget<E, Quota, QuotaListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<E, Quota> newButtonDefinition;

    @Inject
    public QuotaActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<E, Quota> view,
            MainModelProvider<Quota, QuotaListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<E, Quota>(constants.addQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCreateCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<E, Quota>(constants.editQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<E, Quota>(constants.copyQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<E, Quota>(constants.removeQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

    public WebAdminButtonDefinition<E, Quota> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
