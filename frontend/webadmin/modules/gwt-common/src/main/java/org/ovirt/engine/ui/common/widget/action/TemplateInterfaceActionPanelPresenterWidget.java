package org.ovirt.engine.ui.common.widget.action;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateInterfaceActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VmTemplate, VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TemplateInterfaceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VmTemplate, VmNetworkInterface> view,
            SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VmTemplate, VmNetworkInterface>(getSharedEventBus(),
                constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmTemplate, VmNetworkInterface>(getSharedEventBus(),
                constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmTemplate, VmNetworkInterface>(getSharedEventBus(),
                constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
