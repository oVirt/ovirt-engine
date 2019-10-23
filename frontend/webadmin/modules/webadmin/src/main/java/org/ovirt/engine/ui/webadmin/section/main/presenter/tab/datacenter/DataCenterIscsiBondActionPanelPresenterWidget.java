package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterIscsiBondListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterIscsiBondActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StoragePool, IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterIscsiBondActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, IscsiBond> view,
            SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, IscsiBond>(constants.addIscsiBond()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, IscsiBond>(constants.editIscsiBond()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, IscsiBond>(constants.removeIscsiBond()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
