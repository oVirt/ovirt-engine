package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.action.TemplateInterfaceActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.template.TemplateInterfaceListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateInterfacePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabTemplateInterfaceView extends AbstractSubTabTableWidgetView<VmTemplate, VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel>
        implements SubTabTemplateInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabTemplateInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabTemplateInterfaceView(SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> modelProvider,
            EventBus eventBus,
            TemplateInterfaceActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(new TemplateInterfaceListModelTable(modelProvider, eventBus, actionPanel, clientStorage,
                modelProvider.getMainModel()));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        getTable().enableColumnResizing();
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
