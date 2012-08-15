package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.IVdcQueryableCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.IsObjectInSystemColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CustomSelectionCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.IsProblematicImportVmColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class ImportVmPopupView extends AbstractModelBoundPopupView<ImportVmModel> implements ImportVmPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportVmModel, ImportVmPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportVmPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> destClusterEditor;

    @UiField(provided = true)
    @Path(value = "clusterQuota.selectedItem")
    ListModelListBoxEditor<Object> destClusterQuotaEditor;

    @UiField(provided = true)
    @Path(value = "storage.selectedItem")
    ListModelListBoxEditor<Object> destStorageEditor;

    @UiField(provided = true)
    @Path(value = "collapseSnapshots.entity")
    EntityModelCheckBoxEditor collapseSnapshotEditor;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    @Ignore
    Label message;

    @UiField
    Image image;

    @Ignore
    private IVdcQueryableCellTable<VM, ImportVmModel> table;

    @Ignore
    private IVdcQueryableCellTable<DiskImage, VmImportDiskListModel> diskTable;

    @Ignore
    private IVdcQueryableCellTable<VmNetworkInterface, VmImportInterfaceListModel> nicTable;

    private IVdcQueryableCellTable<String, VmAppListModel> appTable;

    @Ignore
    TabLayoutPanel subTabLayoutpanel = null;

    private ImportVmModel object;

    private ImportVmGeneralSubTabView generalView;

    boolean firstSelection = false;

    private CustomSelectionCell customSelectionCellFormatType;

    private CustomSelectionCell customSelectionCellStorageDomain;

    private CustomSelectionCell customSelectionCellQuota;

    private Column<DiskImage, String> storageDomainsColumn;

    private Column<DiskImage, String> quotaColumn;

    private IsObjectInSystemColumn<VM> isObjectInSystemColumn;

    private final ApplicationConstants constants;

    private final ApplicationResources resources;

    @Inject
    public ImportVmPopupView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources);

        this.constants = constants;
        this.resources = resources;

        initListBoxEditors();
        initCheckboxes();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        localize(constants);
        Driver.driver.initialize(this);
        initTables();
        initSubTabLayoutPanel();
        addStyles();
    }

    private void addStyles() {
        collapseSnapshotEditor.addContentWidgetStyleName(style.collapseEditor());
    }

    private void initSubTabLayoutPanel() {
        if (subTabLayoutpanel == null) {
            subTabLayoutpanel = new TabLayoutPanel(20, Unit.PX);
            subTabLayoutpanel.addSelectionHandler(new SelectionHandler<Integer>() {

                @Override
                public void onSelection(SelectionEvent<Integer> event) {
                    if (object != null) {
                        object.setActiveDetailModel(object.getDetailModels().get(event.getSelectedItem()));
                        if (event.getSelectedItem() == 0) {
                            generalView.setMainTabSelectedItem((VM) object.getSelectedItem());
                        }
                    }
                }
            });
            ScrollPanel generalPanel = new ScrollPanel();
            DetailModelProvider<VmListModel, VmGeneralModel> modelProvider =
                    new DetailModelProvider<VmListModel, VmGeneralModel>() {
                        @Override
                        public VmGeneralModel getModel() {
                            return (VmGeneralModel) object.getDetailModels().get(0);
                        }

                        @Override
                        public void onSubTabSelected() {
                        }
                    };
            generalView = new ImportVmGeneralSubTabView(modelProvider, constants);
            generalPanel.add(generalView);
            subTabLayoutpanel.add(generalPanel, constants.importVmGeneralSubTabLabel());

            ScrollPanel nicPanel = new ScrollPanel();
            nicPanel.add(nicTable);
            subTabLayoutpanel.add(nicPanel, constants.importVmNetworkIntefacesSubTabLabel());

            ScrollPanel diskPanel = new ScrollPanel();
            diskPanel.add(diskTable);
            subTabLayoutpanel.add(diskPanel, constants.importVmDisksSubTabLabel());

            ScrollPanel appPanel = new ScrollPanel();
            appPanel.add(appTable);
            subTabLayoutpanel.add(appPanel, constants.importVmApplicationslSubTabLabel());
        }
    }

    private void initTables() {
        initMainTable();
        initNicsTable();
        initDiskTable();
        initAppTable();
    }

    private void initAppTable() {
        appTable = new IVdcQueryableCellTable<String, VmAppListModel>();

        appTable.addColumn(new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        }, constants.installedApp());

        appTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initMainTable() {
        this.table = new IVdcQueryableCellTable<VM, ImportVmModel>();

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        table.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> originColumn = new EnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getorigin();
            }
        };
        table.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        table.addColumn(new VmTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> memoryColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getvm_mem_size_mb()) + " MB"; //$NON-NLS-1$
            }
        };
        table.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> cpuColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getnum_of_cpus());
            }
        };
        table.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> diskColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, constants.disksVm(), "50px"); //$NON-NLS-1$

        isObjectInSystemColumn = new IsObjectInSystemColumn<VM>();
        table.addColumn(isObjectInSystemColumn, constants.vmInSetup(), "60px"); //$NON-NLS-1$

        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        splitLayoutPanel.add(sp);
        table.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initNicsTable() {
        nicTable = new IVdcQueryableCellTable<VmNetworkInterface, VmImportInterfaceListModel>();
        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicTable.addColumn(nameColumn, constants.nameInterface(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> networkColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicTable.addColumn(networkColumn, constants.networkNameInterface(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicTable.addColumn(typeColumn, constants.typeInterface(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicTable.addColumn(macColumn, constants.macInterface(), "150px"); //$NON-NLS-1$

        nicTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initDiskTable() {
        diskTable = new IVdcQueryableCellTable<DiskImage, VmImportDiskListModel>();
        TextColumnWithTooltip<DiskImage> nameColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias(); //$NON-NLS-1$
            }
        };
        diskTable.addColumn(nameColumn, constants.nameDisk(), "100px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getsize();
            }
        };
        diskTable.addColumn(sizeColumn, constants.provisionedSizeDisk(), "100px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getactual_size();
            }
        };
        diskTable.addColumn(actualSizeColumn, constants.sizeDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        diskTable.addColumn(dateCreatedColumn, constants.dateCreatedInterface(), "100px"); //$NON-NLS-1$

        diskTable.setSelectionModel(new NoSelectionModel<DiskImage>());

        ArrayList<String> allocationTypes = new ArrayList<String>();
        allocationTypes.add(constants.thinAllocation());
        allocationTypes.add(constants.preallocatedAllocation());

        customSelectionCellFormatType = new CustomSelectionCell(allocationTypes);
        customSelectionCellFormatType.setStyle(style.cellSelectBox());

        Column<DiskImage, String> allocationColumn = new Column<DiskImage, String>(
                customSelectionCellFormatType) {
            @Override
            public String getValue(DiskImage disk) {
                ImportData importData =
                        object.getDiskImportData(disk.getId());
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
                ImportData importData =
                        object.getDiskImportData(disk.getId());
                if (importData != null) {
                    importData.setSelectedVolumeType(tempVolumeType);
                }
            }
        });

        diskTable.addColumn(allocationColumn, constants.allocationDisk(), "80px"); //$NON-NLS-1$
        diskTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void addStorageDomainsColumn() {
        customSelectionCellStorageDomain = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCellStorageDomain.setStyle(style.cellSelectBox());

        storageDomainsColumn = new Column<DiskImage, String>(customSelectionCellStorageDomain) {
            @Override
            public String getValue(DiskImage disk) {
                ImportData importData = object.getDiskImportData(disk.getId());

                ArrayList<String> storageDomainsNameList = new ArrayList<String>();
                storage_domains selectedStorageDomain = null;
                if (importData != null && importData.getStorageDomains() != null) {
                    for (storage_domains storageDomain : importData.getStorageDomains()) {
                        storageDomainsNameList.add(storageDomain.getstorage_name());
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
                        return selectedStorageDomain.getstorage_name();
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
                object.getDiskImportData(disk.getId()).setSelectedStorageDomainString(value);
                diskTable.edit(object.getImportDiskListModel());
            }
        });

        diskTable.addColumn(storageDomainsColumn, constants.storageDomainDisk(), "100px"); //$NON-NLS-1$

    }

    private void addStorageQuotaColumn() {
        if (!object.isQuotaEnabled()) {
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
                ImportData importData = object.getDiskImportData(disk.getId());

                ArrayList<String> storageQuotaList = new ArrayList<String>();
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
                object.getDiskImportData(disk.getId()).setSelectedQuotaString(value);
            }
        });
        diskTable.addColumn(quotaColumn, constants.quota(), "100px"); //$NON-NLS-1$
    }

    private void initListBoxEditors() {
        destClusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });
        destClusterQuotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
        destStorageEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    private void initCheckboxes() {
        collapseSnapshotEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void localize(ApplicationConstants constants) {
        destClusterEditor.setLabel(constants.importVm_destCluster());
        destClusterQuotaEditor.setLabel(constants.importVm_destClusterQuota());
        destStorageEditor.setLabel(constants.defaultStorage());
        collapseSnapshotEditor.setLabel(constants.importVm_collapseSnapshots());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(final ImportVmModel object) {
        this.object = object;
        isObjectInSystemColumn.setInSetup(object);
        table.edit(object);

        image.setVisible(false);
        table.addColumnAt(new IsProblematicImportVmColumn(object.getDisksToConvert()), "", "30px", 0); //$NON-NLS-1$ //$NON-NLS-2$

        table.edit(object);

        addStorageDomainsColumn();

        object.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs
                        && ((PropertyChangedEventArgs) args).PropertyName.equals(object.ON_DISK_LOAD)) {
                    addStorageQuotaColumn();
                    diskTable.edit(object.getImportDiskListModel());
                    customSelectionCellFormatType.setEnabledWithToolTip(
                            (Boolean) object.getCollapseSnapshots().getEntity(),
                            constants.importAllocationModifiedCollapse());
                    if (object.getDisksToConvert().size() > 0) {
                        image.setVisible(true);
                    }
                } else if (args instanceof PropertyChangedEventArgs
                        && ((PropertyChangedEventArgs) args).PropertyName.equals("Message")) { ////$NON-NLS-1$
                    message.setText(object.getMessage());
                }
            }
        });

        SingleSelectionModel<IVdcQueryable> selectionModel =
                (SingleSelectionModel<IVdcQueryable>) table.getSelectionModel();
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!firstSelection) {
                    object.setActiveDetailModel(object.getDetailModels().get(0));
                    generalView.setMainTabSelectedItem((VM) object.getSelectedItem());
                    firstSelection = true;
                }
                splitLayoutPanel.clear();
                splitLayoutPanel.addSouth(subTabLayoutpanel, 230);
                ScrollPanel sp = new ScrollPanel();
                sp.add(table);
                splitLayoutPanel.add(sp);
                table.getElement().getStyle().setPosition(Position.RELATIVE);
                subTabLayoutpanel.selectTab(0);
            }
        });
        nicTable.edit((VmImportInterfaceListModel) object.getDetailModels().get(1));
        diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
        appTable.edit((VmAppListModel) object.getDetailModels().get(3));

        Driver.driver.edit(object);
    }

    @Override
    public ImportVmModel flush() {
        table.flush();
        nicTable.flush();
        diskTable.flush();
        appTable.flush();
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String checkboxEditor();

        String collapseEditor();

        String cellSelectBox();
    }
}
