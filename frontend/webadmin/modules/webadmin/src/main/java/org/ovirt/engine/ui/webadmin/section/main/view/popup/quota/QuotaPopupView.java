package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.Slider;
import org.ovirt.engine.ui.common.widget.form.Slider.SliderValueChange;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.cell.NullableButtonCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaUtilizationStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class QuotaPopupView extends AbstractModelBoundPopupView<QuotaModel> implements QuotaPopupPresenterWidget.ViewDef, SliderValueChange {

    private static final String GRACE_CLUSTER = "GRACE_CLUSTER"; //$NON-NLS-1$

    private static final String THRESHOLD_CLUSTER = "THRESHOLD_CLUSTER"; //$NON-NLS-1$

    private static final String GRACE_STORAGE = "GRACE_STORAGE"; //$NON-NLS-1$

    private static final String THRESHOLD_STORAGE = "THRESHOLD_STORAGE"; //$NON-NLS-1$

    private static final String MAX_COLOR = "#4E9FDD"; //$NON-NLS-1$

    private static final String MIN_COLOR = "#AFBF27"; //$NON-NLS-1$

    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<>(SizeConverter.SizeUnit.GiB);

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "copyPermissions.entity")
    @WithElementId("copyPermissions")
    EntityModelCheckBoxEditor copyPermissionsEditor;

    @UiField(provided = true)
    Slider clusterGraceSlider;

    @UiField(provided = true)
    Slider clusterThresholdSlider;

    @UiField(provided = true)
    Slider storageGraceSlider;

    @UiField(provided = true)
    Slider storageThresholdSlider;

    @UiField
    @Path(value = "globalClusterQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor globalClusterQuotaRadioButtonEditor;

    @UiField
    @Path(value = "specificClusterQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor specificClusterQuotaRadioButtonEditor;

    @UiField
    @Path(value = "globalStorageQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor globalStorageQuotaRadioButtonEditor;

    @UiField
    @Path(value = "specificStorageQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor specificStorageQuotaRadioButtonEditor;

    @UiField
    @Ignore
    ScrollPanel clusterQuotaTableContainer;

    @Ignore
    ListModelObjectCellTable<QuotaCluster, ListModel<QuotaCluster>> quotaClusterTable;

    @Ignore
    ListModelObjectCellTable<QuotaStorage, ListModel<QuotaStorage>> quotaStorageTable;

    @UiField
    @Ignore
    ScrollPanel storageQuotaTableContainer;

    private Column<QuotaCluster, Boolean> isClusterInQuotaColumn = null;
    private Column<QuotaStorage, Boolean> isStorageInQuotaColumn = null;

    private QuotaModel model;

    private boolean firstTime = false;

    ArrayList<Guid> selectedClusterGuid = new ArrayList<>();
    ArrayList<Guid> selectedStorageGuid = new ArrayList<>();

    interface Driver extends UiCommonEditorDriver<QuotaModel, QuotaPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, QuotaPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<QuotaPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public QuotaPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initCheckBoxes();
        initSliders();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        initTables();
    }

    private void initCheckBoxes() {
        copyPermissionsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initSliders() {
        clusterThresholdSlider = new Slider(2, 1, 100, 80, MIN_COLOR);
        clusterThresholdSlider.setSliderValueChange(THRESHOLD_CLUSTER, this);
        clusterGraceSlider = new Slider(2, 101, 200, 120, MAX_COLOR);
        clusterGraceSlider.setSliderValueChange(GRACE_CLUSTER, this);

        storageThresholdSlider = new Slider(2, 1, 100, 80, MIN_COLOR);
        storageThresholdSlider.setSliderValueChange(THRESHOLD_STORAGE, this);
        storageGraceSlider = new Slider(2, 101, 200, 120, MAX_COLOR);
        storageGraceSlider.setSliderValueChange(GRACE_STORAGE, this);
    }

    private void initTables() {
        initQuotaClusterTable();
        initQuotaStorageTable();
    }

    private void initQuotaStorageTable() {
        quotaStorageTable = new ListModelObjectCellTable<>();
        storageQuotaTableContainer.add(quotaStorageTable);

        isStorageInQuotaColumn = new Column<QuotaStorage, Boolean>(new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(QuotaStorage object) {
                if (selectedStorageGuid.contains(object.getStorageId()) || object.getStorageSizeGB() != null) {
                    if (!selectedStorageGuid.contains(object.getStorageId())) {
                        selectedStorageGuid.add(object.getStorageId());
                    }
                    return true;
                }
                return false;
            }
        };

        isStorageInQuotaColumn.setFieldUpdater((index, object, value) -> {
            if (value) {
                selectedStorageGuid.add(object.getStorageId());
                object.setStorageSizeGB(QuotaStorage.UNLIMITED);
            } else {
                selectedStorageGuid.remove(object.getStorageId());
                object.setStorageSizeGB(null);
            }
            if (model.getGlobalStorageQuota().getEntity()) {
                quotaStorageTable.asEditor().edit(model.getQuotaStorages());
            } else {
                quotaStorageTable.asEditor().edit(model.getAllDataCenterStorages());
            }
        });

        quotaStorageTable.addColumn(new AbstractTextColumn<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                if (object.getStorageName() == null || object.getStorageName().length() == 0) {
                    return constants.utlQuotaAllStoragesQuotaPopup();
                }
                return object.getStorageName();
            }
        }, constants.storageNameQuota(), "200px"); //$NON-NLS-1$

        quotaStorageTable.addColumn(new QuotaUtilizationStatusColumn<QuotaStorage>(), constants.empty(), "1px"); //$NON-NLS-1$

        quotaStorageTable.addColumn(new AbstractTextColumn<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                if (object.getStorageSizeGB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)) {
                    return messages.unlimitedStorageConsumption(object.getStorageSizeGBUsage() == 0 ?
                            "0" : //$NON-NLS-1$
                            diskSizeRenderer.render(object.getStorageSizeGBUsage()));
                } else {
                    return messages.limitedStorageConsumption(object.getStorageSizeGBUsage() == 0 ?
                            "0" : //$NON-NLS-1$
                            diskSizeRenderer.render(object.getStorageSizeGBUsage())
                            , object.getStorageSizeGB());
                }
            }
        }, constants.quota());

        Column<QuotaStorage, String> editColumn = new Column<QuotaStorage, String>(new NullableButtonCell()) {
            @Override
            public String getValue(QuotaStorage object) {
                if (model.getGlobalStorageQuota().getEntity()
                        || (model.getSpecificStorageQuota().getEntity() && selectedStorageGuid.contains(object.getStorageId()))) {
                    return constants.editCellQuota();
                }
                return null;
            }
        };
        editColumn.setFieldUpdater((index, object, value) -> model.editQuotaStorage(object));

        quotaStorageTable.addColumn(editColumn, constants.empty(), "100px"); //$NON-NLS-1$
    }

    private void initQuotaClusterTable() {
        quotaClusterTable = new ListModelObjectCellTable<>();
        clusterQuotaTableContainer.add(quotaClusterTable);

        isClusterInQuotaColumn = new Column<QuotaCluster, Boolean>(
                new CheckboxCell(true, true)) {
            @Override
            public Boolean getValue(QuotaCluster object) {
                if (selectedClusterGuid.contains(object.getClusterId())
                        || (object.getMemSizeMB() != null && object.getVirtualCpu() != null)) {
                    if (!selectedClusterGuid.contains(object.getClusterId())) {
                        selectedClusterGuid.add(object.getClusterId());
                    }
                    return true;
                }
                return false;
            }
        };

        isClusterInQuotaColumn.setFieldUpdater((index, object, value) -> {
            if (value) {
                selectedClusterGuid.add(object.getClusterId());
                object.setVirtualCpu(QuotaCluster.UNLIMITED_VCPU);
                object.setMemSizeMB(QuotaCluster.UNLIMITED_MEM);
            } else {
                selectedClusterGuid.remove(object.getClusterId());
                object.setVirtualCpu(null);
                object.setMemSizeMB(null);
            }
            if (model.getGlobalClusterQuota().getEntity()) {
                quotaClusterTable.asEditor().edit(model.getQuotaClusters());
            } else {
                quotaClusterTable.asEditor().edit(model.getAllDataCenterClusters());
            }
        });

        quotaClusterTable.addColumn(new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                if (object.getClusterName() == null || object.getClusterName().length() == 0) {
                    return constants.ultQuotaForAllClustersQuotaPopup();
                }
                return object.getClusterName();
            }
        }, constants.clusterNameQuota(), "200px"); //$NON-NLS-1$

        quotaClusterTable.addColumn(new QuotaUtilizationStatusColumn<QuotaCluster>(), constants.empty(), "1px"); //$NON-NLS-1$

        quotaClusterTable.addColumn(new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                if (object.getMemSizeMB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getMemSizeMB().equals(QuotaCluster.UNLIMITED_MEM)) {
                    return messages.unlimitedMemConsumption(object.getMemSizeMBUsage());
                } else {
                    return messages.limitedMemConsumption(object.getMemSizeMBUsage(), object.getMemSizeMB());
                }
            }
        }, constants.quotaOfMemQuota());

        quotaClusterTable.addColumn(new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                if (object.getVirtualCpu() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getVirtualCpu().equals(QuotaCluster.UNLIMITED_VCPU)) {
                    return messages.unlimitedVcpuConsumption(object.getVirtualCpuUsage());
                } else {
                    return messages.limitedVcpuConsumption(object.getVirtualCpuUsage(), object.getVirtualCpu());
                }
            }
        }, constants.quotaOfVcpuQuota());

        NullableButtonCell editCellButton = new NullableButtonCell();
        Column<QuotaCluster, String> editColumn = new Column<QuotaCluster, String>(editCellButton) {
            @Override
            public String getValue(QuotaCluster object) {
                if (model.getGlobalClusterQuota().getEntity()
                        || (model.getSpecificClusterQuota().getEntity() && selectedClusterGuid.contains(object.getClusterId()))) {
                    return constants.editCellQuota();
                }
                return null;
            }
        };

        quotaClusterTable.addColumn(editColumn, constants.empty(), "100px"); //$NON-NLS-1$
        editColumn.setFieldUpdater((index, object, value) -> model.editQuotaCluster(object));
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());
    }

    @Override
    public void edit(QuotaModel object) {
        this.model = object;
        if (!firstTime) {
            registerHandlers();
            firstTime = true;
            updateSliders();
        }

        quotaClusterTable.asEditor().edit(object.getQuotaClusters());
        quotaStorageTable.asEditor().edit(object.getQuotaStorages());
        driver.edit(object);
    }

    private void registerHandlers() {
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (Model.PROP_WINDOW.equals(propName) && model.getWindow() == null) {
                if (model.getSpecificClusterQuota().getEntity()) {
                    quotaClusterTable.asEditor().edit(model.getAllDataCenterClusters());
                } else {
                    quotaClusterTable.asEditor().edit(model.getQuotaClusters());
                }
                if (model.getSpecificStorageQuota().getEntity()) {
                    quotaStorageTable.asEditor().edit(model.getAllDataCenterStorages());
                } else {
                    quotaStorageTable.asEditor().edit(model.getQuotaStorages());
                }
            }
        });

        model.getSpecificClusterQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (model.getSpecificClusterQuota().getEntity()) {
                quotaClusterTable.insertColumn(0, isClusterInQuotaColumn);
                quotaClusterTable.setColumnWidth(isClusterInQuotaColumn, "30px"); //$NON-NLS-1$
                quotaClusterTable.asEditor().edit(model.getAllDataCenterClusters());
            } else {
                quotaClusterTable.removeColumn(isClusterInQuotaColumn);
                quotaClusterTable.asEditor().edit(model.getQuotaClusters());
            }
        });

        model.getSpecificStorageQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (model.getSpecificStorageQuota().getEntity()) {
                quotaStorageTable.insertColumn(0, isStorageInQuotaColumn);
                quotaStorageTable.setColumnWidth(isStorageInQuotaColumn, "30px"); //$NON-NLS-1$
                quotaStorageTable.asEditor().edit(model.getAllDataCenterStorages());
            } else {
                quotaStorageTable.removeColumn(isStorageInQuotaColumn);
                quotaStorageTable.asEditor().edit(model.getQuotaStorages());
            }
        });
    }

    @Override
    public QuotaModel flush() {
        quotaClusterTable.flush();
        quotaStorageTable.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void updateSliders() {
        clusterThresholdSlider.setValue(model.getThresholdCluster().getEntity());
        clusterGraceSlider.setValue(model.getGraceCluster().getEntity() + 100);
        storageThresholdSlider.setValue(model.getThresholdStorage().getEntity());
        storageGraceSlider.setValue(model.getGraceStorage().getEntity() + 100);
    }

    @Override
    public void onSliderValueChange(String name, int value) {
        if (name.equals(THRESHOLD_CLUSTER)) {
            model.getThresholdCluster().setEntity(value);
        } else if (name.equals(GRACE_CLUSTER)) {
            model.getGraceCluster().setEntity(value - 100);
        } else if (name.equals(THRESHOLD_STORAGE)) {
            model.getThresholdStorage().setEntity(value);
        } else if (name.equals(GRACE_STORAGE)) {
            model.getGraceStorage().setEntity(value - 100);
        }
    }

}
