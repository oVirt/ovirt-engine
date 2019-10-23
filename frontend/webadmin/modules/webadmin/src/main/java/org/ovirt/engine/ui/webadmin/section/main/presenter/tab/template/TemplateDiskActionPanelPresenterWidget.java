package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateDiskActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VmTemplate, DiskModel, TemplateListModel, TemplateDiskListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TemplateDiskActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VmTemplate, DiskModel> view,
            SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VmTemplate, DiskModel>(getSharedEventBus(), constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCopyCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmTemplate, DiskModel>(getSharedEventBus(), constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getChangeQuotaCommand();
            }
        });
    }

}
