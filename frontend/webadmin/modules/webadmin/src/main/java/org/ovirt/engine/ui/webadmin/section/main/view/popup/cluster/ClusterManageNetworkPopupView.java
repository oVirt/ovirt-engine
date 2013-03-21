package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CheckboxHeader;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupView extends AbstractModelBoundPopupView<ClusterNetworkManageModel> implements ClusterManageNetworkPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterManageNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    EntityModelCellTable<ClusterNetworkManageModel> networks;

    @Inject
    public ClusterManageNetworkPopupView(EventBus eventBus,
            ApplicationResources resources, ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        this.networks = new EntityModelCellTable<ClusterNetworkManageModel>(SelectionMode.NONE, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants, templates);
    }

    @SuppressWarnings("unchecked")
    Iterable<EntityModel> getNetworksTableItems() {
        ListModel tableModel = networks.flush();
        return tableModel != null ? tableModel.getItems() : new ArrayList<EntityModel>();
    }

    void refreshNetworksTable() {
        networks.edit(networks.flush());
    }

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        CheckboxHeader assignAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader(constants.assignAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (canEditAssign(clusterNetworkManageModel)) {
                        changeIsAttached(clusterNetworkManageModel, value);
                    }
                }
                refreshNetworksTable();
            }

            @Override
            public Boolean getValue() {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (canEditAssign(clusterNetworkManageModel)) {
                        if (!clusterNetworkManageModel.isAttached()) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean isEnabled() {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (clusterNetworkManageModel.getIsChangable()) {
                        return true;
                    }
                }
                return false;
            }
        };

        networks.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).getDisplayedName();
            }
        }, constants.nameNetwork());

        networks.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                changeIsAttached(clusterNetworkManageModel, value);
                refreshNetworksTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).isAttached();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return ClusterManageNetworkPopupView.this.canEditAssign(model);
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.assign()));
            }

        }, assignAllHeader, "90px"); //$NON-NLS-1$

        CheckboxHeader requiredAllHeader = new CheckboxHeader(
                templates.textForCheckBoxHeader(constants.requiredAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (canEditRequired(clusterNetworkManageModel)) {
                        clusterNetworkManageModel.setRequired(value);
                    }
                    refreshNetworksTable();
                }
            }

            @Override
            public Boolean getValue() {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (canEditRequired(clusterNetworkManageModel)) {
                        if (!clusterNetworkManageModel.isRequired()) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean isEnabled() {
                for (EntityModel model : getNetworksTableItems()) {
                    ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;
                    if (clusterNetworkManageModel.getIsChangable()) {
                        return true;
                    }
                }
                return false;
            }
        };

        networks.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ((ClusterNetworkModel) model).setRequired(value);
                refreshNetworksTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).isRequired();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return canEditRequired(model);
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.required()));
            }
        }, requiredAllHeader, "110px"); //$NON-NLS-1$

        networks.addColumn(new CheckboxColumn<EntityModel>() {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).isVmNetwork();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return false;
            }

        }, constants.vmNetwork(), "90px"); //$NON-NLS-1$

        networks.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;

                networks.flush().setDisplayNetwork(clusterNetworkManageModel, value);
                refreshNetworksTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).isDisplayNetwork();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return ((ClusterNetworkModel) model).isAttached();
            }
        }, constants.displayNetwork(), "100px"); //$NON-NLS-1$

        networks.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ClusterNetworkModel clusterNetworkManageModel = (ClusterNetworkModel) model;

                networks.flush().setMigrationNetwork(clusterNetworkManageModel, value);
                refreshNetworksTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ClusterNetworkModel) model).isMigrationNetwork();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                ClusterNetworkModel clusterNetworkModel = ((ClusterNetworkModel) model);
                Boolean migrationNetworkEnabled =
                        (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.MigrationNetworkEnabled,
                         clusterNetworkModel.getCluster().getcompatibility_version().toString());
                return migrationNetworkEnabled && clusterNetworkModel.isAttached();
            }
        }, constants.migrationNetwork(), "100px"); //$NON-NLS-1$
    }

    @Override
    public void edit(ClusterNetworkManageModel model) {
        networks.edit(model);
    }

    @Override
    public ClusterNetworkManageModel flush() {
        return networks.flush();
    }

    private void changeIsAttached(ClusterNetworkModel clusterNetworkManageModel, Boolean value) {
        clusterNetworkManageModel.setAttached(value);
        if (!value && clusterNetworkManageModel.isDisplayNetwork()) {
            clusterNetworkManageModel.setDisplayNetwork(false);
        }
        if (!value && clusterNetworkManageModel.isRequired()) {
            clusterNetworkManageModel.setRequired(false);
        }
    }

    private boolean canEditAssign(EntityModel model) {
        return !((ClusterNetworkModel) model).isManagement();
    }

    private boolean canEditRequired(EntityModel model) {
        return ((ClusterNetworkModel) model).isAttached() && !((ClusterNetworkModel) model).isManagement();
    }

}
