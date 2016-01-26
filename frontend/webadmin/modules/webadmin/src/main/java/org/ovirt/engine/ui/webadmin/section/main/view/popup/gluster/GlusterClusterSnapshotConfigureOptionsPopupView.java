package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterClusterSnapshotConfigModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class GlusterClusterSnapshotConfigureOptionsPopupView extends AbstractModelBoundPopupView<GlusterClusterSnapshotConfigModel> implements GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<GlusterClusterSnapshotConfigModel, GlusterClusterSnapshotConfigureOptionsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GlusterClusterSnapshotConfigureOptionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterClusterSnapshotConfigureOptionsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    @WithElementId
    Label snapshotConfigHeader;

    @UiField(provided = true)
    @Path(value = "clusters.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Cluster> clusterEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<GlusterVolumeSnapshotConfig>>> configsTable;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public GlusterClusterSnapshotConfigureOptionsPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    private void initEditors() {
        clusterEditor = new ListModelListBoxEditor<>(new NameRenderer<Cluster>());

        configsTable = new EntityModelCellTable<>(false, true);
        configsTable.setSelectionModel(new NoSelectionModel());

        configsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeSnapshotConfig>() {
            @Override
            public String getText(GlusterVolumeSnapshotConfig object) {
                return object.getParamName();
            }
        }, constants.volumeSnapshotConfigName(), "200px"); //$NON-NLS-1$

        Column<EntityModel<GlusterVolumeSnapshotConfig>, String> valueColumn = new Column<EntityModel<GlusterVolumeSnapshotConfig>, String>(new TextInputCell()) {
            @Override
            public String getValue(EntityModel<GlusterVolumeSnapshotConfig> object) {
                return object.getEntity().getParamValue();
            }
        };
        configsTable.addColumn(valueColumn, constants.volumeSnapshotConfigValue(), "100px"); //$NON-NLS-1$

        valueColumn.setFieldUpdater(new FieldUpdater<EntityModel<GlusterVolumeSnapshotConfig>, String>() {

            @Override
            public void update(int index, EntityModel<GlusterVolumeSnapshotConfig> object, String value) {
                object.getEntity().setParamValue(value);
            }
        });
    }

    private void localize() {
        clusterEditor.setLabel(constants.volumeClusterLabel());
        snapshotConfigHeader.setText(constants.snapshotConfigHeaderLabel());
    }

    @Override
    public void edit(final GlusterClusterSnapshotConfigModel object) {
        driver.edit(object);
        configsTable.asEditor().edit(object.getClusterConfigOptions());
    }

    @Override
    public GlusterClusterSnapshotConfigModel flush() {
        return driver.flush();
    }
}
