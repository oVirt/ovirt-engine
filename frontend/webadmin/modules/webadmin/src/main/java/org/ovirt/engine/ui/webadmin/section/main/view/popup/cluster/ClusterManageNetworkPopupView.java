package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelCheckboxColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupView extends AbstractModelBoundPopupView<ListModel> implements ClusterManageNetworkPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterManageNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label networksLabel;

    @UiField(provided = true)
    EntityModelCellTable<ListModel> networks;

    private ClusterNetworkManageModel displayNetwork;

    @Inject
    public ClusterManageNetworkPopupView(EventBus eventBus,
            ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.networks = new EntityModelCellTable<ListModel>(false, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants);
        localize(constants);
    }

    void initEntityModelCellTable(ApplicationConstants constants) {
        networks.addColumn(new EntityModelCheckboxColumn(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ClusterNetworkManageModel manageModel = (ClusterNetworkManageModel) model;
                manageModel.setAttached(value);
                if (!value && manageModel.isDisplayNetwork()) {
                    manageModel.setDisplayNetwork(false);
                }
                networks.redraw();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isAttached();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return !((ClusterNetworkManageModel) model).isManagement();
            }
        }, constants.attachedNetwork(), "50px"); //$NON-NLS-1$

        networks.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ClusterNetworkManageModel) model).getName();
            }
        }, constants.nameNetwork());

        networks.addColumn(new EntityModelCheckboxColumn(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ((ClusterNetworkManageModel) model).setRequired(value);
                networks.redraw();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isRequired();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isAttached();
            }

        }, constants.requiredNetwork(), "50px"); //$NON-NLS-1$

        networks.addColumn(new EntityModelCheckboxColumn() {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isVmNetwork();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return false;
            }

        }, constants.vmNetwork(), "50px"); //$NON-NLS-1$

        networks.addColumn(new EntityModelCheckboxColumn(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ClusterNetworkManageModel manageModel = (ClusterNetworkManageModel) model;
                // remove existing display
                if (displayNetwork != null) {
                    displayNetwork.setDisplayNetwork(false);
                }
                // set current display
                displayNetwork = value ? manageModel : null;
                manageModel.setDisplayNetwork(value);
                // attach is needed
                if (!manageModel.isAttached()) {
                    manageModel.setAttached(true);
                }
                networks.redraw();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isDisplayNetwork();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return ((ClusterNetworkManageModel) model).isAttached();
            }
        }, constants.displayNetwork(), "50px"); //$NON-NLS-1$
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

    @Override
    public void setDisplayNetwork(ClusterNetworkManageModel displayNetwork) {
        this.displayNetwork = displayNetwork;
    }

}
