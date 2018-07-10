package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.DoubleEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.BrickAdvancedDetailsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.BrickAdvancedDetailsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class BrickAdvancedDetailsPopupView extends AbstractModelBoundPopupView<BrickAdvancedDetailsModel> implements BrickAdvancedDetailsPopupPresenterWidget.ViewDef {

    private static final String DEFAULT_WIDTH = "120px"; //$NON-NLS-1$

    interface Driver extends UiCommonEditorDriver<BrickAdvancedDetailsModel, BrickAdvancedDetailsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, BrickAdvancedDetailsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<BrickAdvancedDetailsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    @Path(value = "brick.entity")
    @WithElementId
    StringEntityModelLabelEditor brickEditor;

    @UiField(provided = true)
    @Path(value = "brickProperties.status.entity")
    @WithElementId
    EntityModelLabelEditor<GlusterStatus> statusEditor;

    @UiField
    @Path(value = "brickProperties.port.entity")
    @WithElementId
    IntegerEntityModelLabelEditor portEditor;

    @UiField
    @Path(value = "brickProperties.rdmaPort.entity")
    @WithElementId
    IntegerEntityModelLabelEditor rdmaPortEditor;

    @UiField
    @Path(value = "brickProperties.pid.entity")
    @WithElementId
    IntegerEntityModelLabelEditor pidEditor;

    @UiField
    @Path(value = "brickProperties.totalSize.entity")
    @WithElementId
    DoubleEntityModelLabelEditor totalSizeEditor;

    @UiField
    @Path(value = "brickProperties.freeSize.entity")
    @WithElementId
    DoubleEntityModelLabelEditor freeSizeEditor;

    @UiField
    @Path(value = "brickProperties.confirmedFreeSize.entity")
    @WithElementId
    DoubleEntityModelLabelEditor confirmedFreeSizeEditor;

    @UiField
    @Path(value = "brickProperties.vdoSavings.entity")
    @WithElementId
    IntegerEntityModelLabelEditor vdoSavingsEditor;

    @UiField
    @Path(value = "brickProperties.device.entity")
    @WithElementId
    StringEntityModelLabelEditor deviceEditor;

    @UiField
    @Path(value = "brickProperties.blockSize.entity")
    @WithElementId
    IntegerEntityModelLabelEditor blockSizeEditor;

    @UiField
    @Path(value = "brickProperties.mountOptions.entity")
    @WithElementId
    StringEntityModelTextAreaLabelEditor mountOptionsEditor;

    @UiField
    @Path(value = "brickProperties.fileSystem.entity")
    @WithElementId
    StringEntityModelLabelEditor fileSystemEditor;

    @UiField
    @WithElementId
    DialogTab clientsTab;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<GlusterClientInfo>>> clientsTable;

    @UiField
    @WithElementId
    DialogTab memoryStatsTab;

    @UiField
    @Path(value = "memoryStatistics.totalAllocated.entity")
    @WithElementId
    IntegerEntityModelLabelEditor totalAllocatedEditor;

    @UiField
    @Path(value = "memoryStatistics.freeBlocks.entity")
    @WithElementId
    IntegerEntityModelLabelEditor freeBlocksEditor;

    @UiField
    @Path(value = "memoryStatistics.freeFastbin.entity")
    @WithElementId
    IntegerEntityModelLabelEditor freeFastbinBlocksEditor;

    @UiField
    @Path(value = "memoryStatistics.mmappedBlocks.entity")
    @WithElementId
    IntegerEntityModelLabelEditor mmappedBlocksEditor;

    @UiField
    @Path(value = "memoryStatistics.spaceAllocatedMmapped.entity")
    @WithElementId
    IntegerEntityModelLabelEditor spaceAllocatedMmappedEditor;

    @UiField
    @Path(value = "memoryStatistics.maxTotalAllocated.entity")
    @WithElementId
    IntegerEntityModelLabelEditor maxTotalAllocatedEditor;

    @UiField
    @Path(value = "memoryStatistics.spaceFreedFastbin.entity")
    @WithElementId
    IntegerEntityModelLabelEditor spaceFreedFastbinEditor;

    @UiField
    @Path(value = "memoryStatistics.totalAllocatedSpace.entity")
    @WithElementId
    IntegerEntityModelLabelEditor totalAllocatedSpaceEditor;

    @UiField
    @Path(value = "memoryStatistics.totalFreeSpace.entity")
    @WithElementId
    IntegerEntityModelLabelEditor totalFreeSpaceEditor;

    @UiField
    @Path(value = "memoryStatistics.releasableFreeSpace.entity")
    @WithElementId
    IntegerEntityModelLabelEditor releasableFreeSpaceEditor;

    @UiField
    @WithElementId
    DialogTab memoryPoolsTab;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<Mempool>>> memoryPoolsTable;

    @UiField
    @Ignore
    Alert message;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public BrickAdvancedDetailsPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableColumns();
        driver.initialize(this);
    }

    private void initEditors() {
        statusEditor = new EntityModelLabelEditor<>(new EnumRenderer<GlusterStatus>());
        clientsTable = new EntityModelCellTable<>(false, true);
        memoryPoolsTable = new EntityModelCellTable<>(false, true);
        memoryPoolsTable.setWidth("1200px"); //$NON-NLS-1$
    }

    private void initTableColumns() {
        clientsTable.addColumn(new AbstractEntityModelTextColumn<GlusterClientInfo>() {
            @Override
            public String getText(GlusterClientInfo entity) {
                return entity.getHostname();
            }
        }, constants.clientBrickAdvancedLabel(), DEFAULT_WIDTH);

        clientsTable.addColumn(new AbstractEntityModelTextColumn<GlusterClientInfo>() {
            @Override
            public String getText(GlusterClientInfo entity) {
                return String.valueOf(entity.getClientPort());
            }
        }, constants.clientPortBrickAdvancedLabel(), DEFAULT_WIDTH);

        clientsTable.addColumn(new AbstractEntityModelTextColumn<GlusterClientInfo>() {
            @Override
            public String getText(GlusterClientInfo entity) {
                return String.valueOf(entity.getBytesRead());
            }
        }, constants.bytesReadBrickAdvancedLabel(), "160px"); //$NON-NLS-1$

        clientsTable.addColumn(new AbstractEntityModelTextColumn<GlusterClientInfo>() {
            @Override
            public String getText(GlusterClientInfo entity) {
                return String.valueOf(entity.getBytesWritten());
            }
        }, constants.bytesWrittenBrickAdvancedLabel(), "160px"); //$NON-NLS-1$

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return entity.getName();
            }
        }, constants.nameBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getHotCount());
            }
        }, constants.hotCountBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getColdCount());
            }
        }, constants.coldCountBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getPadddedSize());
            }
        }, constants.paddedSizeBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getAllocCount());
            }
        }, constants.allocatedCountBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getMaxAlloc());
            }
        }, constants.maxAllocatedBrickAdvancedLabel(), "180px"); //$NON-NLS-1$

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getPoolMisses());
            }
        }, constants.poolMissesBrickAdvancedLabel(), DEFAULT_WIDTH);

        memoryPoolsTable.addColumn(new AbstractEntityModelTextColumn<Mempool>() {
            @Override
            public String getText(Mempool entity) {
                return String.valueOf(entity.getMaxStdAlloc());
            }
        }, constants.maxStdAllocatedBrickAdvancedLabel(), "200px"); //$NON-NLS-1$
    }

    @Override
    public void edit(BrickAdvancedDetailsModel object) {
        driver.edit(object);
        clientsTable.asEditor().edit(object.getClients());
        memoryPoolsTable.asEditor().edit(object.getMemoryPools());
    }

    @Override
    public BrickAdvancedDetailsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
        this.message.setVisible(StringHelper.isNotNullOrEmpty(message));
    }
}
