package org.ovirt.engine.ui.webadmin.section.main.view.popup.guide;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.table.cell.EventHandlingCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHostData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.MoveHostPopupPresenterWidget;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class MoveHostPopupView extends AbstractModelBoundPopupView<MoveHost> implements MoveHostPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<MoveHost, MoveHostPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, MoveHostPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MoveHostPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<MoveHost> table;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Cluster> clusterListEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MoveHostPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        localize();
        initTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    private void localize() {
        clusterListEditor.setLabel(constants.moveHostPopupClusterLabel());
    }

    private void initListBoxEditors() {
        clusterListEditor = new ListModelListBoxEditor<>(new NameRenderer<Cluster>());
    }

    private void initTable() {
        table = new EntityModelCellTable<>(true, false, true);
        table.setWidth("100%"); // $NON-NLS-1$

        AbstractTextColumn<MoveHostData> nameColumn = new AbstractTextColumn<MoveHostData>() {
            @Override
            public String getValue(MoveHostData object) {
                return object.getEntity().getName();
            }
        };
        table.addColumn(nameColumn, constants.nameHost());

        AbstractTextColumn<MoveHostData> hostColumn = new AbstractTextColumn<MoveHostData>() {
            @Override
            public String getValue(MoveHostData object) {
                return object.getEntity().getHostName();
            }
        };
        table.addColumn(hostColumn, constants.ipHost());

        AbstractTextColumn<EntityModel<VDS>> statusColumn = new AbstractEnumColumn<EntityModel<VDS>, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(EntityModel<VDS> object) {
                return object.getEntity().getStatus();
            }
        };
        table.addColumn(statusColumn, constants.statusHost(), "90px"); //$NON-NLS-1$

        final Column<MoveHostData, Boolean> activateColumn = new Column<MoveHostData, Boolean>(new ActivateCheckboxCell()) {
            @Override
            public Boolean getValue(MoveHostData object) {
                return object.getActivateHost();
            }
        };

        activateColumn.setFieldUpdater((idx, object, value) -> object.setActivateHost(value));

        table.addColumn(activateColumn, constants.activateHost(), "60px"); //$NON-NLS-1$
    }

    class ActivateCheckboxCell extends CheckboxCell implements EventHandlingCell {

        @Override
        public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
            return true;
        }

    }

    @Override
    public void edit(MoveHost object) {
        if (!object.isMultiSelection()) {
            table.setSelectionModel(new SingleSelectionModel<EntityModel>());
            table.addSelectionChangeHandler();
        }

        driver.edit(object);
        table.asEditor().edit(object);
    }

    @Override
    public MoveHost flush() {
        return table.asEditor().flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
