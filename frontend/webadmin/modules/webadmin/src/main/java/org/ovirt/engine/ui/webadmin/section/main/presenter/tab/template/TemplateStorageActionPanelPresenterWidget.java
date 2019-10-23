package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateStorageActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VmTemplate, StorageDomain, TemplateListModel, TemplateStorageListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TemplateStorageActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VmTemplate, StorageDomain> view,
            SearchableDetailModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VmTemplate, StorageDomain>(getSharedEventBus(), constants.removeStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
