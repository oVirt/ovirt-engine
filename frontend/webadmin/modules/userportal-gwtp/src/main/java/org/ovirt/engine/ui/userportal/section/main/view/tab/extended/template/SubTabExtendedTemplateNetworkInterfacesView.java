package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.template.TemplateInterfaceListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateNetworkInterfacesPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplateInterfaceListModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedTemplateNetworkInterfacesView extends AbstractSubTabTableWidgetView<VmTemplate, VmNetworkInterface, UserPortalTemplateListModel, TemplateInterfaceListModel>
        implements SubTabExtendedTemplateNetworkInterfacesPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedTemplateNetworkInterfacesView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedTemplateNetworkInterfacesView(TemplateInterfaceListModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new TemplateInterfaceListModelTable(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
