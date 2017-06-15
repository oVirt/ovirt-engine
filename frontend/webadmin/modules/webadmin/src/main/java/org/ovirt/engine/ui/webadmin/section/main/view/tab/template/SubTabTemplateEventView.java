package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabTemplateEventView extends AbstractSubTabEventView<VmTemplate, TemplateListModel, TemplateEventListModel>
        implements SubTabTemplateEventPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabTemplateEventView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabTemplateEventView(SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
