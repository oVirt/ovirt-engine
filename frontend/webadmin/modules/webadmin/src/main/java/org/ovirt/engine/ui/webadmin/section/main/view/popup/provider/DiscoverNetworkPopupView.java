package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HorizontalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.ListModelListBoxColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.DiscoverNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ExternalNetwork;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.DiscoverNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CheckboxHeader;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class DiscoverNetworkPopupView extends AbstractModelBoundPopupView<DiscoverNetworksModel> implements DiscoverNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DiscoverNetworksModel, DiscoverNetworkPopupView> { }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DiscoverNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    HorizontalSplitTable splitTable;

    @Ignore
    EntityModelCellTable<ListModel> providerNetworks;

    @Ignore
    EntityModelCellTable<ListModel> importedNetworks;

    private ListModelListBoxColumn<EntityModel, StoragePool> dcColumn;

    @Inject
    public DiscoverNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        // Initialize Editors
        providerNetworks = new EntityModelCellTable<ListModel>(true, false, true);
        importedNetworks = new EntityModelCellTable<ListModel>(true, false, true);
        splitTable = new HorizontalSplitTable(providerNetworks, importedNetworks, constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTables(constants, templates);
        driver.initialize(this);
    }

    @SuppressWarnings("unchecked")
    Iterable<ExternalNetwork> getAllImportedNetworks() {
        ListModel tableModel = importedNetworks.flush();
        return tableModel != null && tableModel.getItems() != null ? tableModel.getItems()
                : new ArrayList<ExternalNetwork>();
    }

    public void refreshImportedNetworks() {
        importedNetworks.edit(importedNetworks.flush());
    }

    void initEntityModelCellTables(final ApplicationConstants constants, final ApplicationTemplates templates) {
        Column<EntityModel, String> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ExternalNetwork) model).getNetwork().getName();
            }
        };
        providerNetworks.addColumn(nameColumn, constants.nameNetworkHeader());
        importedNetworks.addColumn(nameColumn, constants.nameNetworkHeader());

        Column<EntityModel, String> idColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ExternalNetwork) model).getNetwork().getProvidedBy().getExternalId();
            }
        };
        providerNetworks.addColumn(idColumn, constants.idNetworkHeader());
        importedNetworks.addColumn(idColumn, constants.idNetworkHeader());

        dcColumn = new ListModelListBoxColumn<EntityModel, StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool dc) {
                return dc.getName();
            }
        })
        {
            @Override
            public ListModel getValue(EntityModel network) {
                return ((ExternalNetwork) network).getDataCenters();
            }
        };
        importedNetworks.addColumn(dcColumn, constants.dcNetworkHeader()); //$NON-NLS-1$

        CheckboxHeader publicAllHeader =
                new CheckboxHeader(templates.textForCheckBoxHeader(constants.publicNetwork())) {
                    @Override
                    protected void selectionChanged(Boolean value) {
                        for (ExternalNetwork model : getAllImportedNetworks()) {
                            model.setPublicUse(value);
                        }
                        refreshImportedNetworks();
                    }

                    @Override
                    public Boolean getValue() {
                        for (ExternalNetwork model : getAllImportedNetworks()) {
                            if (!model.isPublicUse()) {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                };

        importedNetworks.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ExternalNetwork externalNetwork = (ExternalNetwork) model;
                externalNetwork.setPublicUse(value);
                refreshImportedNetworks();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ExternalNetwork) model).isPublicUse();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return true;
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox("")); //$NON-NLS-1$
            }

        }, publicAllHeader, "80px"); //$NON-NLS-1$
    }

    @Override
    public void edit(DiscoverNetworksModel model) {
        providerNetworks.edit(model.getProviderNetworks());
        importedNetworks.edit(model.getImportedNetworks());
        dcColumn.edit(model.getDataCenters());
        driver.edit(model);
    }

    @Override
    public DiscoverNetworksModel flush() {
        return driver.flush();
    }

}
