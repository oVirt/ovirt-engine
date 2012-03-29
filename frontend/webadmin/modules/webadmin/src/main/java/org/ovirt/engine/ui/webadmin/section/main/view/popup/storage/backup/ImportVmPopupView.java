package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OriginType;
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
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
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
    @Path(value = "destinationStorage.selectedItem")
    ListModelListBoxEditor<Object> destStorageEditor;

    @UiField(provided = true)
    @Path(value = "collapseSnapshots.entity")
    EntityModelCheckBoxEditor collapseSnapshotEditor;

    @UiField(provided = true)
    @Path(value = "isSingleDestStorage.entity")
    EntityModelCheckBoxEditor isSingleDestStorageEditor;

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

    private CustomSelectionCell customSelectionCell;

    private boolean numOfVmsGreaterThan1;

    private CustomSelectionCell customSelectionCellStorageDomain;

    private Column<DiskImage, String> storageDomainsColumn;

    private final ApplicationConstants constants;

    @Inject
    public ImportVmPopupView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;

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
        isSingleDestStorageEditor.addContentWidgetStyleName(style.checkboxEditor());
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
            generalView = new ImportVmGeneralSubTabView(modelProvider);
            generalPanel.add(generalView);
            subTabLayoutpanel.add(generalPanel, "General");

            ScrollPanel nicPanel = new ScrollPanel();
            nicPanel.add(nicTable);
            subTabLayoutpanel.add(nicPanel, "Network Interfaces");

            ScrollPanel diskPanel = new ScrollPanel();
            diskPanel.add(diskTable);
            subTabLayoutpanel.add(diskPanel, "Virtual Disks");

            ScrollPanel appPanel = new ScrollPanel();
            appPanel.add(appTable);
            subTabLayoutpanel.add(appPanel, "Applications");
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
        }, "Installed Applications");

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
        table.addColumn(nameColumn, "Name", "150px");

        TextColumnWithTooltip<VM> originColumn = new EnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getorigin();
            }
        };
        table.addColumn(originColumn, "Origin", "100px");

        table.addColumn(new VmTypeColumn(), "", "30px");

        TextColumnWithTooltip<VM> memoryColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getvm_mem_size_mb()) + " MB";
            }
        };
        table.addColumn(memoryColumn, "Memory", "100px");

        TextColumnWithTooltip<VM> cpuColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getnum_of_cpus());
            }
        };
        table.addColumn(cpuColumn, "CPUs", "50px");

        TextColumnWithTooltip<VM> diskColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, "Disks", "50px");

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
        nicTable.addColumn(nameColumn, "Name", "150px");

        TextColumnWithTooltip<VmNetworkInterface> networkColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicTable.addColumn(networkColumn, "Network Name", "150px");

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicTable.addColumn(typeColumn, "Type", "150px");

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicTable.addColumn(macColumn, "MAC", "150px");

        nicTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initDiskTable() {
        diskTable = new IVdcQueryableCellTable<DiskImage, VmImportDiskListModel>();
        TextColumnWithTooltip<DiskImage> nameColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return "Disk " + object.getinternal_drive_mapping();
            }
        };
        diskTable.addColumn(nameColumn, "Name", "100px");

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getsize();
            }
        };
        diskTable.addColumn(sizeColumn, "Size", "100px");

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getactual_size();
            }
        };
        diskTable.addColumn(actualSizeColumn, "Actual Size", "100px");

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        diskTable.addColumn(dateCreatedColumn, "Date Created", "100px");

        diskTable.setSelectionModel(new NoSelectionModel<DiskImage>());

        ArrayList<String> allocationTypes = new ArrayList<String>();
        allocationTypes.add(constants.thisAllocation());
        allocationTypes.add(constants.preallocatedAllocation());

        customSelectionCell = new CustomSelectionCell(allocationTypes);
        customSelectionCell.setEnabledWithToolTip(false, constants.importAllocationModifiedCollapse());
        customSelectionCell.setStyle(style.cellSelectBox());

        Column<DiskImage, String> allocationColumn = new Column<DiskImage, String>(
                customSelectionCell) {
            @Override
            public String getValue(DiskImage object) {
                return new EnumRenderer<VolumeType>().render(VolumeType.forValue(object.getvolume_type().getValue()));
            }
        };

        allocationColumn.setFieldUpdater(new FieldUpdater<DiskImage, String>() {

            @Override
            public void update(int index, DiskImage disk, String value) {
                VolumeType tempVolumeType = VolumeType.Sparse;
                if (value.equals(constants.thisAllocation())) {
                    tempVolumeType = VolumeType.Sparse;
                } else if (value.equals(constants.preallocatedAllocation())) {
                    tempVolumeType = VolumeType.Preallocated;
                }
                object.VolumeType_SelectedItemChanged(disk,
                        tempVolumeType);
            }
        });

        diskTable.addColumn(allocationColumn, "Allocation", "80px");
        diskTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void addStorageDomainsColumn(final ImportVmModel object) {
        ArrayList<String> storageDomains = new ArrayList<String>();
        for (Object storageDomain : object.getAllDestinationStorage().getItems()) {
            storageDomains.add(((storage_domains) storageDomain).getstorage_name());
        }
        Collections.sort(storageDomains);

        customSelectionCellStorageDomain = new CustomSelectionCell(storageDomains);
        customSelectionCellStorageDomain.setEnabledWithToolTip(false,
                constants.importVmTemplateSingleStorageCheckedLabel());
        customSelectionCellStorageDomain.setStyle(style.cellSelectBox());

        if (storageDomainsColumn != null) {
            diskTable.removeColumn(storageDomainsColumn);
        }

        storageDomainsColumn = new Column<DiskImage, String>(customSelectionCellStorageDomain) {
            @Override
            public String getValue(DiskImage disk) {
                ArrayList<String> storageDomains =
                        object.getAvailableStorageDomainsByDiskId(disk.getId());
                if (storageDomains != null && !(Boolean) object.getCollapseSnapshots().getEntity()) {
                    ((CustomSelectionCell) getCell()).setOptions(storageDomains);
                }
                return object.getStorageNameById(disk.getstorage_ids().get(0));
            }
        };

        storageDomainsColumn.setFieldUpdater(new FieldUpdater<DiskImage, String>() {

            @Override
            public void update(int index, DiskImage disk, String value) {
                object.DestinationStorage_SelectedItemChanged(disk, value);
            }
        });

        diskTable.addColumn(storageDomainsColumn, "Storage Domain", "100px");
    }

    private void initListBoxEditors() {
        destClusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
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
        isSingleDestStorageEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void localize(ApplicationConstants constants) {
        destClusterEditor.setLabel(constants.importVm_destCluster());
        destStorageEditor.setLabel(constants.singleDestinationStorage());
        collapseSnapshotEditor.setLabel(constants.importVm_collapseSnapshots());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(final ImportVmModel object) {
        this.object = object;
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsMissingStorages".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    message.getElement().setInnerHTML(
                            object.getIsMissingStorages() ? constants.importMissingStorages() : "");
                }
            }
        });

        image.setVisible(false);
        object.getCollapseSnapshots().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("Message".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    message.setText(object.getCollapseSnapshots().getMessage());
                    if (object.getCollapseSnapshots().getMessage() != null) {
                        image.setVisible(true);
                    } else {
                        image.setVisible(false);
                    }
                    table.flush();
                    table.edit(object);
                }
                if (!numOfVmsGreaterThan1 && ((ArrayList<IVdcQueryable>) object.getItems()).size() > 1) {
                    customSelectionCell.setEnabledWithToolTip(false, constants.importAllocationModifiedSingleVM());
                    numOfVmsGreaterThan1 = true;
                    diskTable.flush();
                    diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
                }
                if (!numOfVmsGreaterThan1) {
                    if ((Boolean) object.getCollapseSnapshots().getEntity()) {
                        customSelectionCell.setEnabledWithToolTip(true, "");
                        diskTable.flush();
                        diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
                    } else {
                        customSelectionCell.setEnabledWithToolTip(false, constants.importAllocationModifiedCollapse());
                        diskTable.flush();
                        diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
                    }
                }
            }
        });

        table.addColumnAt(new IsProblematicImportVmColumn(object.getProblematicItems()), "", "30px", 0);

        table.edit(object);

        object.getAllDestinationStorage().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                addStorageDomainsColumn(object);
            }
        });

        object.getIsSingleDestStorage().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Boolean isSingleDestStorage = (Boolean) object.getIsSingleDestStorage().getEntity();
                object.getDestinationStorage().setIsChangable(isSingleDestStorage);
                diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
            }
        });

        object.getDestinationStorage().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                UpdateStorageDomainCells();
            }
        });

        object.getImportDiskListModel().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("DiskStorageMap".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    UpdateStorageDomainCells();
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
            }
        });
        nicTable.edit((VmImportInterfaceListModel) object.getDetailModels().get(1));
        diskTable.edit((VmImportDiskListModel) object.getDetailModels().get(2));
        appTable.edit((VmAppListModel) object.getDetailModels().get(3));
        Driver.driver.edit(object);

    }

    private void UpdateStorageDomainCells() {
        Boolean isSingleDestStorage = (Boolean) object.getIsSingleDestStorage().getEntity();
        String toolTip = isSingleDestStorage ? "" : constants.importVmTemplateSingleStorageCheckedLabel();

        if (customSelectionCellStorageDomain != null) {
            customSelectionCellStorageDomain.setEnabledWithToolTip(!isSingleDestStorage, toolTip);
        }

        diskTable.edit(object.getImportDiskListModel());
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
