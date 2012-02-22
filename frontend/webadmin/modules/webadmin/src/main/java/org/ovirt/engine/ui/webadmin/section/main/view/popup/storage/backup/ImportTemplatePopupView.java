package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateImportDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateImportInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.editor.IVdcQueryableCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class ImportTemplatePopupView extends WebAdminModelBoundPopupView<ImportTemplateModel> implements ImportTemplatePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportTemplateModel, ImportTemplatePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportTemplatePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> destClusterEditor;

    @UiField(provided = true)
    @Path(value = "destinationStorage.selectedItem")
    ListModelListBoxEditor<Object> destStorageEditor;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    @Ignore
    Label message;

    @Ignore
    private IVdcQueryableCellTable<VmTemplate, ImportTemplateModel> table;

    @Ignore
    private IVdcQueryableCellTable<DiskImage, TemplateImportDiskListModel> diskTable;

    @Ignore
    private IVdcQueryableCellTable<VmNetworkInterface, TemplateImportInterfaceListModel> nicTable;

    @Ignore
    TabLayoutPanel subTabLayoutpanel = null;

    private ImportTemplateModel object;

    private ImportTemplateGeneralSubTabView generalView;

    boolean firstSelection = false;

    @Inject
    public ImportTemplatePopupView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
        initTables();
        initSubTabLayoutPanel();
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
                            generalView.setMainTabSelectedItem((VmTemplate) object.getSelectedItem());
                        }
                    }
                }
            });
            ScrollPanel generalPanel = new ScrollPanel();
            DetailModelProvider<TemplateListModel, TemplateGeneralModel> modelProvider =
                    new DetailModelProvider<TemplateListModel, TemplateGeneralModel>() {
                        @Override
                        public TemplateGeneralModel getModel() {
                            return (TemplateGeneralModel) object.getDetailModels().get(0);
                        }

                        @Override
                        public void onSubTabSelected() {
                        }
                    };
            generalView = new ImportTemplateGeneralSubTabView(modelProvider);
            generalPanel.add(generalView);
            subTabLayoutpanel.add(generalPanel, "General");

            ScrollPanel nicPanel = new ScrollPanel();
            nicPanel.add(nicTable);
            subTabLayoutpanel.add(nicPanel, "Network Interfaces");

            ScrollPanel diskPanel = new ScrollPanel();
            diskPanel.add(diskTable);
            subTabLayoutpanel.add(diskPanel, "Virtual Disks");
        }
    }

    private void initTables() {
        initMainTable();
        initNicsTable();
        initDiskTable();
    }

    private void initMainTable() {
        this.table = new IVdcQueryableCellTable<VmTemplate, ImportTemplateModel>();

        TextColumnWithTooltip<VmTemplate> nameColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getname();
            }
        };
        table.addColumn(nameColumn, "Name", "150px");

        TextColumnWithTooltip<VmTemplate> originColumn = new EnumColumn<VmTemplate, OriginType>() {
            @Override
            protected OriginType getRawValue(VmTemplate object) {
                return object.getorigin();
            }
        };
        table.addColumn(originColumn, "Origin", "100px");

        TextColumnWithTooltip<VmTemplate> memoryColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getmem_size_mb()) + " MB";
            }
        };
        table.addColumn(memoryColumn, "Memory", "100px");

        TextColumnWithTooltip<VmTemplate> cpuColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getnum_of_cpus());
            }
        };
        table.addColumn(cpuColumn, "CPUs", "50px");

        TextColumnWithTooltip<VmTemplate> diskColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getDiskList().size());
            }
        };
        table.addColumn(diskColumn, "Disks", "50px");

        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        splitLayoutPanel.add(sp);
        table.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    private void initNicsTable() {
        nicTable = new IVdcQueryableCellTable<VmNetworkInterface, TemplateImportInterfaceListModel>();
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
        diskTable = new IVdcQueryableCellTable<DiskImage, TemplateImportDiskListModel>();
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

        TextColumnWithTooltip<DiskImage> typeColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_type().toString();
            }
        };
        diskTable.addColumn(typeColumn, "Type", "60px");

        TextColumnWithTooltip<DiskImage> formatColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getvolume_format().toString();
            }
        };
        diskTable.addColumn(formatColumn, "Format", "60px");

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        diskTable.addColumn(dateCreatedColumn, "Date Created", "100px");

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        diskTable.addColumn(allocationColumn, "Allocation", "60px");

        diskTable.getElement().getStyle().setPosition(Position.RELATIVE);
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

    private void localize(ApplicationConstants constants) {
        destClusterEditor.setLabel(constants.importVm_destCluster());
        destStorageEditor.setLabel(constants.importVm_destStorage());
    }

    @Override
    public void edit(final ImportTemplateModel object) {
        this.object = object;

        table.edit(object);
        // object.setActiveDetailModel(object.getDetailModels().get(0));
        SingleSelectionModel<IVdcQueryable> selectionModel =
                (SingleSelectionModel<IVdcQueryable>) table.getSelectionModel();
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!firstSelection) {
                    object.setActiveDetailModel(object.getDetailModels().get(0));
                    generalView.setMainTabSelectedItem((VmTemplate) object.getSelectedItem());
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
        nicTable.edit((TemplateImportInterfaceListModel) object.getDetailModels().get(1));
        diskTable.edit((TemplateImportDiskListModel) object.getDetailModels().get(2));
        Driver.driver.edit(object);
    }

    @Override
    public ImportTemplateModel flush() {
        table.flush();
        nicTable.flush();
        diskTable.flush();
        return Driver.driver.flush();
    }
}
