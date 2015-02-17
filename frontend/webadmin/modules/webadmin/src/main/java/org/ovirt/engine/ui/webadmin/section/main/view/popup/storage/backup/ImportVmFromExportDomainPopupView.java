package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportDiskData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmFromExportDomainPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.cell.CustomSelectionCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.IsProblematicImportVmColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractWebAdminImageResourceColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class ImportVmFromExportDomainPopupView extends AbstractModelBoundPopupView<ImportVmFromExportDomainModel> implements ImportVmFromExportDomainPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportVmFromExportDomainModel, ImportVmFromExportDomainPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportVmFromExportDomainPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> destClusterEditor;

    @UiField(provided = true)
    @Path(value = "cpuProfiles.selectedItem")
    ListModelListBoxEditor<CpuProfile> cpuProfileEditor;

    @UiField(provided = true)
    @Path(value = "clusterQuota.selectedItem")
    ListModelListBoxEditor<Object> destClusterQuotaEditor;

    @UiField(provided = true)
    @Path(value = "storage.selectedItem")
    ListModelListBoxEditor<Object> destStorageEditor;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    @Ignore
    Label message;

    @Ignore
    protected ListModelObjectCellTable<Object, ImportVmFromExportDomainModel> table;

    @Ignore
    private ListModelObjectCellTable<DiskImage, SearchableListModel> diskTable;

    @Ignore
    private ListModelObjectCellTable<VmNetworkInterface, SearchableListModel> nicTable;

    private ListModelObjectCellTable<String, VmAppListModel> appTable;

    @Ignore
    protected TabLayoutPanel subTabLayoutPanel = null;

    protected ImportVmFromExportDomainModel importModel;

    private ImportVmGeneralSubTabView generalView;

    boolean firstSelection = false;

    private CustomSelectionCell customSelectionCellFormatType;

    private CustomSelectionCell customSelectionCellStorageDomain;

    private CustomSelectionCell customSelectionCellQuota;

    private Column<DiskImage, String> storageDomainsColumn;

    private Column<DiskImage, String> quotaColumn;

    protected AbstractImageResourceColumn<Object> isObjectInSystemColumn;

    private final Driver driver = GWT.create(Driver.class);

    protected final ApplicationConstants constants;

    protected final ApplicationResources resources;

    @Inject
    public ImportVmFromExportDomainPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;
        this.resources = resources;

        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        localize(constants);
        driver.initialize(this);
        initTables();
    }

    private void initSubTabLayoutPanel() {
        if (subTabLayoutPanel == null) {
            subTabLayoutPanel = new TabLayoutPanel(CommonApplicationTemplates.TAB_BAR_HEIGHT, Unit.PX);
            subTabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {

                @Override
                public void onSelection(SelectionEvent<Integer> event) {
                    subTabLayoutPanelSelectionChanged(event.getSelectedItem());
                }
            });

            initGeneralSubTabView();

            ScrollPanel nicPanel = new ScrollPanel();
            nicPanel.add(nicTable);
            subTabLayoutPanel.add(nicPanel, constants.importVmNetworkIntefacesSubTabLabel());

            ScrollPanel diskPanel = new ScrollPanel();
            diskPanel.add(diskTable);
            subTabLayoutPanel.add(diskPanel, constants.importVmDisksSubTabLabel());

            initAppTable();
        }
    }

    protected void subTabLayoutPanelSelectionChanged(Integer selectedItem) {
        if (importModel != null) {
            importModel.setActiveDetailModel((HasEntity<?>) importModel.getDetailModels().get(selectedItem));
        }
    }

    protected void initGeneralSubTabView() {
        ScrollPanel generalPanel = new ScrollPanel();
        DetailModelProvider<VmListModel<Void>, VmGeneralModel> modelProvider =
                new DetailModelProvider<VmListModel<Void>, VmGeneralModel>() {
                    @Override
                    public VmGeneralModel getModel() {
                        return (VmGeneralModel) importModel.getDetailModels().get(0);
                    }

                    @Override
                    public void onSubTabSelected() {
                    }

                    @Override
                    public void onSubTabDeselected() {
                    }
                };
        generalView = new ImportVmGeneralSubTabView(modelProvider, constants);
        generalPanel.add(generalView);
        subTabLayoutPanel.add(generalPanel, constants.importVmGeneralSubTabLabel());
    }

    private void initTables() {
        initMainTable();
        initNicsTable();
        initDiskTable();
    }

    protected void initAppTable() {
        appTable = new ListModelObjectCellTable<>();

        appTable.addColumn(new AbstractTextColumnWithTooltip<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        }, constants.installedApp());

        appTable.getElement().getStyle().setPosition(Position.RELATIVE);

        ScrollPanel appPanel = new ScrollPanel();
        appPanel.add(appTable);
        subTabLayoutPanel.add(appPanel, constants.importVmApplicationslSubTabLabel());
    }

    protected void initMainTable() {
        this.table = new ListModelObjectCellTable<>();

        AbstractCheckboxColumn<Object> collapseSnapshotsColumn =
                new AbstractCheckboxColumn<Object>(new FieldUpdater<Object, Boolean>() {
            @Override
            public void update(int index, Object model, Boolean value) {
                        ((ImportVmData) model).getCollapseSnapshots().setEntity(value);
                        customSelectionCellFormatType.setEnabledWithToolTip(value,
                                constants.importAllocationModifiedCollapse());
                        diskTable.asEditor().edit(importModel.getImportDiskListModel());
                    }
                }) {
            @Override
            public Boolean getValue(Object model) {
                return ((ImportVmData) model).getCollapseSnapshots().getEntity();
            }

            @Override
            protected boolean canEdit(Object model) {
                return ((ImportVmData) model).getCollapseSnapshots().getIsChangable();
            }
                    @Override
                    protected String getDisabledMessage(Object model) {
                        return ((ImportVmData) model).getCollapseSnapshots().getChangeProhibitionReason();
                    }
        };
        table.addColumn(collapseSnapshotsColumn, constants.collapseSnapshots(), "10px"); //$NON-NLS-1$

        AbstractCheckboxColumn<Object> cloneVMColumn = new AbstractCheckboxColumn<Object>(new FieldUpdater<Object, Boolean>() {
            @Override
            public void update(int index, Object model, Boolean value) {
                ((ImportVmData) model).getClone().setEntity(value);
                table.asEditor().edit(importModel);
            }
        }) {
            @Override
            public Boolean getValue(Object model) {
                return ((ImportVmData) model).getClone().getEntity();
            }

            @Override
            protected boolean canEdit(Object model) {
                return ((ImportVmData) model).getClone().getIsChangable();
            }

            @Override
            protected String getDisabledMessage(Object model) {
                return ((ImportVmData) model).getClone().getChangeProhibitionReason();
            }
        };
        table.addColumn(cloneVMColumn, constants.cloneVM(), "50px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> nameColumn = new AbstractTextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return ((ImportVmData) object).getVm().getName();
            }
        };
        table.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> originColumn = new AbstractEnumColumn<Object, OriginType>() {
            @Override
            protected OriginType getRawValue(Object object) {
                return ((ImportVmData) object).getVm().getOrigin();
            }
        };
        table.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        table.addColumn(
                new AbstractWebAdminImageResourceColumn<Object>() {
                    @Override
                    public com.google.gwt.resources.client.ImageResource getValue(Object object) {
                        return new VmTypeColumn().getValue(((ImportVmData) object).getVm());
                    }
                }, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> memoryColumn = new AbstractTextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportVmData) object).getVm().getVmMemSizeMb()) + " MB"; //$NON-NLS-1$
            }
        };
        table.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> cpuColumn = new AbstractTextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportVmData) object).getVm().getNumOfCpus());
            }
        };
        table.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> archColumn = new AbstractTextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportVmData) object).getVm().getClusterArch());
            }
        };
        table.addColumn(archColumn, constants.architectureVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Object> diskColumn = new AbstractTextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportVmData) object).getVm().getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, constants.disksVm(), "50px"); //$NON-NLS-1$

        isObjectInSystemColumn = new AbstractImageResourceColumn<Object>() {
            @Override
            public ImageResource getValue(Object object) {
                return ((ImportVmData) object).isExistsInSystem() ? getCommonResources().logNormalImage() : null;
            }
        };
        table.addColumn(isObjectInSystemColumn, constants.vmInSetup(), "60px"); //$NON-NLS-1$

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ImportVmData selectedObject =
                        ((SingleSelectionModel<ImportVmData>) event.getSource()).getSelectedObject();
                customSelectionCellFormatType.setEnabledWithToolTip(selectedObject.getCollapseSnapshots()
                        .getEntity(),
                        constants.importAllocationModifiedCollapse());
                // diskTable.edit(importVmModel.getImportDiskListModel());
            }
        });

        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        splitLayoutPanel.add(sp);
        table.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initNicsTable() {
        nicTable = new ListModelObjectCellTable<>();
        nicTable.enableColumnResizing();
        AbstractTextColumnWithTooltip<VmNetworkInterface> nameColumn = new AbstractTextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicTable.addColumn(nameColumn, constants.nameInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmNetworkInterface> networkColumn = new AbstractTextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicTable.addColumn(networkColumn, constants.networkNameInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmNetworkInterface> profileColumn = new AbstractTextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        nicTable.addColumn(profileColumn, constants.profileNameInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicTable.addColumn(typeColumn, constants.typeInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmNetworkInterface> macColumn = new AbstractTextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicTable.addColumn(macColumn, constants.macInterface(), "150px"); //$NON-NLS-1$

        nicTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initDiskTable() {
        diskTable = new ListModelObjectCellTable<>();
        diskTable.enableColumnResizing();
        AbstractTextColumnWithTooltip<DiskImage> aliasColumn = new AbstractTextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        diskTable.addColumn(aliasColumn, constants.aliasDisk(), "100px"); //$NON-NLS-1$

        AbstractImageResourceColumn<DiskImage> bootableDiskColumn = new AbstractImageResourceColumn<DiskImage>() {
            @Override
            public ImageResource getValue(DiskImage object) {
                setTitle(object.isBoot() ? getDefaultTitle() : null);
                return object.isBoot() ? getDefaultImage() : null;
            }

            @Override
            public String getDefaultTitle() {
                return constants.bootableDisk();
            }

            @Override
            public ImageResource getDefaultImage() {
                return resources.bootableDiskIcon();
            }
        };
        diskTable.addColumnWithHtmlHeader(bootableDiskColumn, bootableDiskColumn.getHeaderHtml(), "30px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> sizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getSize();
            }
        };
        diskTable.addColumn(sizeColumn, constants.provisionedSizeDisk(), "130px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> actualSizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getActualSizeInBytes();
            }
        };
        diskTable.addColumn(actualSizeColumn, constants.sizeDisk(), "130px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<DiskImage> dateCreatedColumn = new AbstractFullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };
        diskTable.addColumn(dateCreatedColumn, constants.dateCreatedInterface(), "120px"); //$NON-NLS-1$

        diskTable.setSelectionModel(new NoSelectionModel<DiskImage>());

        addAllocationColumn();

        diskTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    protected void addAllocationColumn() {
        ArrayList<String> allocationTypes = new ArrayList<>();
        allocationTypes.add(constants.thinAllocation());
        allocationTypes.add(constants.preallocatedAllocation());

        customSelectionCellFormatType = new CustomSelectionCell(allocationTypes);
        customSelectionCellFormatType.setStyle(style.cellSelectBox());

        Column<DiskImage, String> allocationColumn = new Column<DiskImage, String>(
                customSelectionCellFormatType) {
            @Override
            public String getValue(DiskImage disk) {
                ImportDiskData importData =
                        importModel.getDiskImportData(disk.getId());
                if (importData == null) {
                    return "";
                }
                return new EnumRenderer<VolumeType>().render(VolumeType.forValue(importData.getSelectedVolumeType()
                        .getValue()));
            }
        };

        allocationColumn.setFieldUpdater(new FieldUpdater<DiskImage, String>() {
            @Override
            public void update(int index, DiskImage disk, String value) {
                VolumeType tempVolumeType = VolumeType.Sparse;
                if (value.equals(constants.thinAllocation())) {
                    tempVolumeType = VolumeType.Sparse;
                } else if (value.equals(constants.preallocatedAllocation())) {
                    tempVolumeType = VolumeType.Preallocated;
                }
                ImportDiskData importData =
                        importModel.getDiskImportData(disk.getId());
                if (importData != null) {
                    importData.setSelectedVolumeType(tempVolumeType);
                }
            }
        });

        diskTable.addColumn(allocationColumn, constants.allocationDisk(), "150px"); //$NON-NLS-1$
    }

    private void addStorageDomainsColumn() {
        customSelectionCellStorageDomain = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCellStorageDomain.setStyle(style.cellSelectBox());

        storageDomainsColumn = new Column<DiskImage, String>(customSelectionCellStorageDomain) {
            @Override
            public String getValue(DiskImage disk) {
                ImportDiskData importData = importModel.getDiskImportData(disk.getId());

                ArrayList<String> storageDomainsNameList = new ArrayList<>();
                StorageDomain selectedStorageDomain = null;
                if (importData != null && importData.getStorageDomains() != null) {
                    for (StorageDomain storageDomain : importData.getStorageDomains()) {
                        storageDomainsNameList.add(
                                new StorageDomainFreeSpaceRenderer<>().render(storageDomain));
                        if (importData.getSelectedStorageDomain() != null) {
                            if (storageDomain.getId().equals(importData.getSelectedStorageDomain().getId())) {
                                selectedStorageDomain = storageDomain;
                            }
                        }
                    }
                }
                ((CustomSelectionCell) getCell()).setOptions(storageDomainsNameList);
                if (!storageDomainsNameList.isEmpty()) {
                    if (selectedStorageDomain != null) {
                        return new StorageDomainFreeSpaceRenderer<>().render(selectedStorageDomain);
                    } else {
                        return storageDomainsNameList.get(0);
                    }
                }
                return "";
            }
        };

        storageDomainsColumn.setFieldUpdater(new FieldUpdater<DiskImage, String>() {
            @Override
            public void update(int index, DiskImage disk, String value) {
                String storageDomainName = value.substring(0, value.lastIndexOf(" (")); //$NON-NLS-1$
                importModel.getDiskImportData(disk.getId()).setSelectedStorageDomainString(storageDomainName);
                diskTable.asEditor().edit(importModel.getImportDiskListModel());
            }
        });

        diskTable.addColumn(storageDomainsColumn, constants.storageDomainDisk(), "180px"); //$NON-NLS-1$

    }

    private void addStorageQuotaColumn() {
        if (!importModel.isQuotaEnabled()) {
            return;
        }

        if (quotaColumn != null) {
            return;
        }

        customSelectionCellQuota = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCellQuota.setStyle(style.cellSelectBox());

        quotaColumn = new Column<DiskImage, String>(customSelectionCellQuota) {
            @Override
            public String getValue(DiskImage disk) {
                ImportDiskData importData = importModel.getDiskImportData(disk.getId());

                ArrayList<String> storageQuotaList = new ArrayList<>();
                Quota selectedQuota = null;
                if (importData != null && importData.getQuotaList() != null) {
                    for (Quota quota : importData.getQuotaList()) {
                        storageQuotaList.add(quota.getQuotaName());
                        if (importData.getSelectedQuota() != null) {
                            if (quota.getId().equals(importData.getSelectedQuota().getId())) {
                                selectedQuota = quota;
                            }
                        }
                    }
                }
                ((CustomSelectionCell) getCell()).setOptions(storageQuotaList);
                if (!storageQuotaList.isEmpty()) {
                    if (selectedQuota != null) {
                        return selectedQuota.getQuotaName();
                    } else {
                        return storageQuotaList.get(0);
                    }
                }
                return "";
            }
        };

        quotaColumn.setFieldUpdater(new FieldUpdater<DiskImage, String>() {
            @Override
            public void update(int index, DiskImage disk, String value) {
                importModel.getDiskImportData(disk.getId()).setSelectedQuotaString(value);
            }
        });
        diskTable.addColumn(quotaColumn, constants.quota(), "100px"); //$NON-NLS-1$
    }

    private void initListBoxEditors() {
        destClusterEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getName();
            }
        });
        destClusterQuotaEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
        destStorageEditor = new ListModelListBoxEditor<>(new StorageDomainFreeSpaceRenderer());

        cpuProfileEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<CpuProfile>() {

            @Override
            protected String renderNullSafe(CpuProfile object) {
                return object.getName();
            }
        });
    }

    private void localize(ApplicationConstants constants) {
        destClusterEditor.setLabel(constants.importVm_destCluster());
        destClusterQuotaEditor.setLabel(constants.importVm_destClusterQuota());
        destStorageEditor.setLabel(constants.defaultStorage());
        cpuProfileEditor.setLabel(constants.cpuProfileLabel());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(final ImportVmFromExportDomainModel object) {
        this.importModel = object;
        table.asEditor().edit(object);

        if (object.getDisksToConvert() != null) {
            table.addColumnAt(new IsProblematicImportVmColumn(object.getDisksToConvert()), "", "30px", 0); //$NON-NLS-1$ //$NON-NLS-2$
        }

        table.asEditor().edit(object);

        addStorageDomainsColumn();

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName.equals(object.ON_DISK_LOAD)) {
                    addStorageQuotaColumn();
                    table.redraw();
                    diskTable.asEditor().edit(object.getImportDiskListModel());
                } else if (args.propertyName.equals("Message")) { ////$NON-NLS-1$
                    message.setText(object.getMessage());
                }
            }
        });

        SingleSelectionModel<Object> selectionModel =
                (SingleSelectionModel<Object>) table.getSelectionModel();
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!firstSelection) {
                    object.setActiveDetailModel((HasEntity<?>) object.getDetailModels().get(0));
                    setGeneralViewSelection(((ImportEntityData<?>) object.getSelectedItem()).getEntity());
                    firstSelection = true;
                }
                splitLayoutPanel.clear();
                splitLayoutPanel.addSouth(subTabLayoutPanel, 230);
                ScrollPanel sp = new ScrollPanel();
                sp.add(table);
                splitLayoutPanel.add(sp);
                table.getElement().getStyle().setPosition(Position.RELATIVE);
                // subTabLayoutPanel.selectTab(0);
            }

        });

        initSubTabLayoutPanel();
        nicTable.asEditor().edit((SearchableListModel) object.getDetailModels().get(1));
        diskTable.asEditor().edit((SearchableListModel) object.getDetailModels().get(2));
        if (object.getDetailModels().size() > 3) {
            appTable.asEditor().edit((VmAppListModel) object.getDetailModels().get(3));
        }

        driver.edit(object);
    }

    protected void setGeneralViewSelection(Object selectedItem) {
        generalView.setMainTabSelectedItem((VM) selectedItem);
    }

    @Override
    public ImportVmFromExportDomainModel flush() {
        table.flush();
        nicTable.flush();
        diskTable.flush();
        if (appTable != null) {
            appTable.flush();
        }
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String checkboxEditor();

        String collapseEditor();

        String cellSelectBox();
    }

}
