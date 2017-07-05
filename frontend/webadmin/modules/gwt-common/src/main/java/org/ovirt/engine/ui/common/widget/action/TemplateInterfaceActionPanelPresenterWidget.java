package org.ovirt.engine.ui.common.widget.action;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateInterfaceActionPanelPresenterWidget extends
    ActionPanelPresenterWidget<VmNetworkInterface, TemplateInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TemplateInterfaceActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<VmNetworkInterface> view,
            SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getNewCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getEditCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getRemoveCommand();
            }
        });
    }

}
