package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelTextColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupView extends WebAdminModelBoundPopupView<ListModel> implements ClusterManageNetworkPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterManageNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label networksLabel;

    @UiField(provided = true)
    EntityModelCellTable<ListModel> networks;

    @Inject
    public ClusterManageNetworkPopupView(EventBus eventBus,
            ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        addEntityModelCellTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable();
        localize(constants);
    }

    void addEntityModelCellTable() {
        networks = new EntityModelCellTable<ListModel>(true);
    }

    void initEntityModelCellTable() {
        networks.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                // Window.alert("Lookey: " + ((EntityModel) model.getEntity()).toString()); // useful popup
                return model.getTitle();
            }
        }, "Name");
    }

    void localize(ApplicationConstants constants) {
        networksLabel.setText(constants.clusterManageNetworkPopupLabel());
    }

    @Override
    public void edit(ListModel model) {
        networks.edit(model);
    }

    @Override
    public ListModel flush() {
        return networks.flush();
    }

}
