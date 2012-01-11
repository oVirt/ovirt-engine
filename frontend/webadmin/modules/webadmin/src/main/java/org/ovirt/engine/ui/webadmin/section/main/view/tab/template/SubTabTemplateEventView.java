package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

public class SubTabTemplateEventView extends AbstractSubTabEventView<VmTemplate, TemplateListModel, TemplateEventListModel>
        implements SubTabTemplateEventPresenter.ViewDef {

    @Inject
    public SubTabTemplateEventView(SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
