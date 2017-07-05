package org.ovirt.engine.ui.common.widget.action;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.web.bindery.event.shared.EventBus;

public class PermissionActionPanelPresenterWidget<M extends ListWithDetailsModel, P extends PermissionListModel<?>>
    extends ActionPanelPresenterWidget<Permission, P> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public PermissionActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Permission> view,
            SearchableDetailModelProvider<Permission, M, P> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<Permission>(getSharedEventBus(), constants.addPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<Permission>(getSharedEventBus(), constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
