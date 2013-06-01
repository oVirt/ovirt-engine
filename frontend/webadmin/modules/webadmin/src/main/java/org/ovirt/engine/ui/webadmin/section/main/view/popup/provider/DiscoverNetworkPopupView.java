package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
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
import com.google.inject.Inject;

public class DiscoverNetworkPopupView extends AbstractModelBoundPopupView<DiscoverNetworksModel> implements DiscoverNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DiscoverNetworksModel, DiscoverNetworkPopupView> { }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DiscoverNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Ignore
    public final EntityModelCellTable<ListModel> networksTable;

    private ListModelListBoxColumn<EntityModel, StoragePool> dcColumn;

    @Inject
    public DiscoverNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        // Initialize Editors
        this.networksTable = new EntityModelCellTable<ListModel>(SelectionMode.NONE, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants, templates);
        driver.initialize(this);
    }

    @SuppressWarnings("unchecked")
    Iterable<ExternalNetwork> getNetworksTableItems() {
        ListModel tableModel = networksTable.flush();
        return tableModel != null && tableModel.getItems() != null ? tableModel.getItems()
                : new ArrayList<ExternalNetwork>();
    }

    void refreshNetworksTable() {
        networksTable.edit(networksTable.flush());
    }

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        CheckboxHeader assignAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader("")) { //$NON-NLS-1$
            @Override
            protected void selectionChanged(Boolean value) {
                for (ExternalNetwork model : getNetworksTableItems()) {
                    model.setAttached(value);
                }
                refreshNetworksTable();
            }

            @Override
            public Boolean getValue() {
                for (ExternalNetwork model : getNetworksTableItems()) {
                    if (!model.isAttached()) {
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

        networksTable.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ExternalNetwork externalNetwork = (ExternalNetwork) model;
                externalNetwork.setAttached(value);
                externalNetwork.getDataCenters().setIsChangable(value);
                refreshNetworksTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((ExternalNetwork) model).isAttached();
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

        }, assignAllHeader, "80px"); //$NON-NLS-1$

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
        networksTable.addColumn(dcColumn);

        networksTable.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ExternalNetwork) model).getNetwork().getName();
            }
        }, constants.nameNetworkHeader());

        networksTable.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((ExternalNetwork) model).getNetwork().getProvidedBy().getExternalId();
            }
        }, constants.idNetworkHeader());

        CheckboxHeader publicAllHeader =
                new CheckboxHeader(templates.textForCheckBoxHeader(constants.publicAllNetworks())) {
                    @Override
                    protected void selectionChanged(Boolean value) {
                        for (ExternalNetwork model : getNetworksTableItems()) {
                            model.setPublicUse(value);
                        }
                        refreshNetworksTable();
                    }

                    @Override
                    public Boolean getValue() {
                        for (ExternalNetwork model : getNetworksTableItems()) {
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

        networksTable.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                ExternalNetwork externalNetwork = (ExternalNetwork) model;
                externalNetwork.setPublicUse(value);
                refreshNetworksTable();
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
                sb.append(templates.textForCheckBox(constants.publicNetwork()));
            }

        }, publicAllHeader, "80px"); //$NON-NLS-1$
    }

    @Override
    public void edit(DiscoverNetworksModel model) {
        networksTable.edit(model.getNetworkList());
        dcColumn.edit(model.getDataCenters());
        driver.edit(model);
    }

    @Override
    public DiscoverNetworksModel flush() {
        return driver.flush();
    }

}
