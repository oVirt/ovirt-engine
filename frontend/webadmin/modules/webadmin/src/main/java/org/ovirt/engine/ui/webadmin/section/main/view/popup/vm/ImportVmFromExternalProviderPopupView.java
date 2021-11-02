package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRendererWithNull;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportNetworkData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExternalProviderModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportGeneralModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.ImportVmFromExternalProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportVmGeneralSubTabView;
import org.ovirt.engine.ui.webadmin.widget.table.cell.CustomSelectionCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class ImportVmFromExternalProviderPopupView extends AbstractModelBoundPopupView<ImportVmFromExternalProviderModel> implements ImportVmFromExternalProviderPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ImportVmFromExternalProviderModel, ImportVmFromExternalProviderPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportVmFromExternalProviderPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Cluster> destClusterEditor;

    @UiField(provided = true)
    @Path(value = "cpuProfiles.selectedItem")
    ListModelListBoxEditor<CpuProfile> cpuProfileEditor;

    @UiField(provided = true)
    @Path(value = "clusterQuota.selectedItem")
    ListModelListBoxEditor<Quota> destClusterQuotaEditor;

    @UiField(provided = true)
    @Path(value = "storage.selectedItem")
    ListModelListBoxEditor<StorageDomain> destStorageEditor;

    @UiField(provided = true)
    @Path(value = "allocation.selectedItem")
    protected ListModelListBoxEditor<VolumeType> disksAllocationEditor;

    @UiField(provided = true)
    @Path(value = "iso.selectedItem")
    @WithElementId("iso")
    public ListModelListBoxOnlyEditor<RepoImage> cdImageEditor;

    @UiField(provided = true)
    @Path(value = "attachDrivers.entity")
    @WithElementId("attachDrivers")
    public EntityModelCheckBoxEditor attachDriversEditor;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    Label winWithoutVirtioMessage;

    @Ignore
    ListModelObjectCellTable<ImportVmData, ImportVmFromExternalProviderModel> table;

    @Ignore
    ListModelObjectCellTable<DiskImage, SearchableListModel> diskTable;

    @Ignore
    ListModelObjectCellTable<VmNetworkInterface, SearchableListModel> nicTable;

    @Ignore
    protected TabLayoutPanel subTabLayoutPanel = null;

    boolean firstSelection = false;

    private ImportVmGeneralSubTabView generalView;

    private CustomSelectionCell customSelectionCellNetwork;

    protected ImportVmFromExternalProviderModel importModel;

    private final Driver driver = GWT.create(Driver.class);

    protected final ApplicationConstants constants;

    protected final ApplicationResources resources;

    protected final ApplicationMessages messages;

    @Inject
    public ImportVmFromExternalProviderPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus);
        this.constants = constants;
        this.resources = resources;
        this.messages = messages;

        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        applyStyles();
        localize(constants);
        initTables();
        driver.initialize(this);
    }

    protected void applyStyles() {
        attachDriversEditor.addContentWidgetContainerStyleName(style.cdAttachedLabelWidth());
    }

    private void initTables() {
        initMainTable();
        initNicsTable();
        initDiskTable();
    }

    protected void initMainTable() {
        this.table = new ListModelObjectCellTable<>();

        AbstractCheckboxColumn<ImportVmData> cloneVMColumn = new AbstractCheckboxColumn<ImportVmData>((index, model, value) -> {
            model.getClone().setEntity(value);
            table.asEditor().edit(importModel);
        }) {
            @Override
            public Boolean getValue(ImportVmData model) {
                return model.getClone().getEntity();
            }

            @Override
            protected boolean canEdit(ImportVmData model) {
                return model.getClone().getIsChangable();
            }

            @Override
            protected String getDisabledMessage(ImportVmData model) {
                return model.getClone().getChangeProhibitionReason();
            }
        };
        table.addColumn(cloneVMColumn, constants.cloneVM(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> nameColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData object) {
                return object.getName();
            }
        };
        table.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> originColumn = new AbstractEnumColumn<ImportVmData, OriginType>() {
            @Override
            protected OriginType getRawValue(ImportVmData object) {
                return object.getVm().getOrigin();
            }
        };
        table.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        table.addColumn(
                new AbstractSafeHtmlColumn<ImportVmData>() {
                    @Override
                    public SafeHtml getValue(ImportVmData object) {
                        return VmTypeColumn.getRenderedValue(object.getVm());
                    }
                }, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> memoryColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData object) {
                return messages.megabytes(String.valueOf(object.getVm().getVmMemSizeMb()));
            }
        };
        table.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> cpuColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData object) {
                return String.valueOf(object.getVm().getNumOfCpus());
            }
        };
        table.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> archColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData object) {
                return String.valueOf(object.getVm().getClusterArch());
            }
        };
        table.addColumn(archColumn, constants.architectureVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<ImportVmData> diskColumn = new AbstractTextColumn<ImportVmData>() {
            @Override
            public String getValue(ImportVmData object) {
                return String.valueOf(object.getVm().getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, constants.disksVm(), "50px"); //$NON-NLS-1$

        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        splitLayoutPanel.add(sp);
        table.getElement().getStyle().setPosition(Position.RELATIVE);
    }


    private void localize(ApplicationConstants constants) {
        destClusterEditor.setLabel(constants.importVm_destCluster());
        destClusterQuotaEditor.setLabel(constants.importVm_destClusterQuota());
        destStorageEditor.setLabel(constants.storageDomainDisk());
        cpuProfileEditor.setLabel(constants.cpuProfileLabel());
        disksAllocationEditor.setLabel(constants.allocationDisk());
        attachDriversEditor.setLabel(constants.attachVirtioDrivers());
    }

    protected void initListBoxEditors() {
        destClusterEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Cluster>() {
            @Override
            public String renderNullSafe(Cluster object) {
                return object.getName();
            }
        });
        destClusterQuotaEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Quota>() {
            @Override
            public String renderNullSafe(Quota object) {
                return object.getQuotaName();
            }
        });
        destStorageEditor = new ListModelListBoxEditor<>(new StorageDomainFreeSpaceRenderer());

        cpuProfileEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<CpuProfile>() {

            @Override
            protected String renderNullSafe(CpuProfile object) {
                return object.getName();
            }
        });

        disksAllocationEditor = new ListModelListBoxEditor<>(new EnumRendererWithNull<VolumeType>(constants.autoDetect()));

        attachDriversEditor = new EntityModelCheckBoxEditor(Align.LEFT);
        cdImageEditor = new ListModelListBoxOnlyEditor<>(new NullSafeRenderer<RepoImage>() {
            @Override
            protected String renderNullSafe(RepoImage object) {
                return object.getName();
            }
        });
    }

    @Override
    public void edit(final ImportVmFromExternalProviderModel importModel) {
        this.importModel = importModel;
        table.asEditor().edit(importModel);

        importModel.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.equals(ImportVmFromExternalProviderModel.ON_DISK_LOAD)) {
                table.asEditor().edit(table.asEditor().flush());
            } else if (args.propertyName.equals("Message")) { ////$NON-NLS-1$
                message.setText(importModel.getMessage());
            } else if (args.propertyName.equals("WinWithoutVirtioMessage")) { ////$NON-NLS-1$
                winWithoutVirtioMessage.setText(importModel.getWinWithoutVirtioMessage());
            }
        });

        SingleSelectionModel<Object> selectionModel =
                (SingleSelectionModel<Object>) table.getSelectionModel();
        selectionModel.addSelectionChangeHandler(event -> {
            if (!firstSelection) {
                importModel.setActiveDetailModel((HasEntity<?>) importModel.getDetailModels().get(0));
                setGeneralViewSelection(((ImportEntityData) importModel.getSelectedItem()).getEntity());
                firstSelection = true;
            }
            splitLayoutPanel.clear();
            splitLayoutPanel.addSouth(subTabLayoutPanel, 230);
            ScrollPanel sp = new ScrollPanel();
            sp.add(table);
            splitLayoutPanel.add(sp);
            table.getElement().getStyle().setPosition(Position.RELATIVE);
        });

        initSubTabLayoutPanel();
        nicTable.asEditor().edit((SearchableListModel) importModel.getDetailModels().get(1));
        diskTable.asEditor().edit((SearchableListModel) importModel.getDetailModels().get(2));

        driver.edit(importModel);
    }

    private void addNetworkColumn() {
        customSelectionCellNetwork = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCellNetwork.setStyle(style.cellSelectBox());

        Column<VmNetworkInterface, String> networkColumn = new Column<VmNetworkInterface, String>(customSelectionCellNetwork) {
            @Override
            public String getValue(VmNetworkInterface iface) {
                ImportNetworkData importNetworkData = importModel.getNetworkImportData(iface);
                List<String> networkNames = importNetworkData.getNetworkNames();
                ((CustomSelectionCell) getCell()).setOptions(networkNames);
                if (networkNames.isEmpty()) {
                    return ""; //$NON-NLS-1$
                }
                String selectedNetworkName = importNetworkData.getSelectedNetworkName();
                return selectedNetworkName != null ? selectedNetworkName : networkNames.get(0);
            }
        };

        networkColumn.setFieldUpdater((index, iface, value) -> {
            importModel.getNetworkImportData(iface).setSelectedNetworkName(value);
            nicTable.asEditor().edit(importModel.getImportNetworkInterfaceListModel());
        });

        nicTable.addColumn(networkColumn, constants.networkNameInterface(), "150px"); //$NON-NLS-1$
    }

    private void addNetworkProfileColumn() {
        customSelectionCellNetwork = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCellNetwork.setStyle(style.cellSelectBox());

        Column<VmNetworkInterface, String> profileColumn = new Column<VmNetworkInterface, String>(customSelectionCellNetwork) {
            @Override
            public String getValue(VmNetworkInterface iface) {
                ImportNetworkData importNetworkData = importModel.getNetworkImportData(iface);
                List<String> networkProfileNames = new ArrayList<>();
                for (VnicProfileView networkProfile : importNetworkData.getFilteredNetworkProfiles()) {
                    networkProfileNames.add(networkProfile.getName());
                }
                ((CustomSelectionCell) getCell()).setOptions(networkProfileNames);
                if (networkProfileNames.isEmpty()) {
                    return ""; //$NON-NLS-1$
                }
                VnicProfileView selectedNetworkProfile = importModel.getNetworkImportData(iface).getSelectedNetworkProfile();
                return selectedNetworkProfile != null ? selectedNetworkProfile.getName() : networkProfileNames.get(0);
            }
        };

        profileColumn.setFieldUpdater((index, iface, value) -> importModel.getNetworkImportData(iface).setSelectedNetworkProfile(value));

        nicTable.addColumn(profileColumn, constants.profileNameInterface(), "150px"); //$NON-NLS-1$
    }

    protected void setGeneralViewSelection(Object selectedItem) {
        generalView.setMainSelectedItem((VM) selectedItem);
    }

    private void initSubTabLayoutPanel() {
        if (subTabLayoutPanel == null) {
            subTabLayoutPanel = new TabLayoutPanel(CommonApplicationTemplates.TAB_BAR_HEIGHT, Unit.PX);
            subTabLayoutPanel.addSelectionHandler(event -> subTabLayoutPanelSelectionChanged(event.getSelectedItem()));

            initGeneralSubTabView();

            ScrollPanel nicPanel = new ScrollPanel();
            nicPanel.add(nicTable);
            subTabLayoutPanel.add(nicPanel, constants.importVmNetworkIntefacesSubTabLabel());

            ScrollPanel diskPanel = new ScrollPanel();
            diskPanel.add(diskTable);
            subTabLayoutPanel.add(diskPanel, constants.importVmDisksSubTabLabel());
        }
    }

    protected void subTabLayoutPanelSelectionChanged(Integer selectedItem) {
        if (importModel != null) {
            importModel.setActiveDetailModel((HasEntity<?>) importModel.getDetailModels().get(selectedItem));
        }
    }

    protected void initGeneralSubTabView() {
        ScrollPanel generalPanel = new ScrollPanel();
        DetailModelProvider<ImportVmModel, VmImportGeneralModel> modelProvider =
                new DetailModelProvider<ImportVmModel, VmImportGeneralModel>() {
                    @Override
                    public VmImportGeneralModel getModel() {
                        VmImportGeneralModel model = (VmImportGeneralModel) importModel.getDetailModels().get(0);
                        model.setSource(ImportSource.VMWARE);
                        return model;
                    }

                    @Override
                    public void onSubTabSelected() {
                    }

                    @Override
                    public void onSubTabDeselected() {
                    }

                    @Override
                    public void activateDetailModel() {
                    }

                    @Override
                    public ImportVmModel getMainModel() {
                        // Not used, here to satisfy interface contract.
                        return null;
                    }
                };
        generalView = new ImportVmGeneralSubTabView(modelProvider);
        modelProvider.getModel().clearAndRegisterNameAndOsListeners();
        generalPanel.add(generalView);
        subTabLayoutPanel.add(generalPanel, constants.importVmGeneralSubTabLabel());
    }

    @Override
    public ImportVmFromExternalProviderModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void initNicsTable() {
        nicTable = new ListModelObjectCellTable<>();
        nicTable.enableColumnResizing();
        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicTable.addColumn(nameColumn, constants.nameInterface(), "125px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> originalNetworkNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getRemoteNetworkName();
            }
        };
        nicTable.addColumn(originalNetworkNameColumn, constants.originalNetworkNameInterface(), "160px"); //$NON-NLS-1$

        addNetworkColumn();
        addNetworkProfileColumn();

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicTable.addColumn(typeColumn, constants.typeInterface(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> macColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicTable.addColumn(macColumn, constants.macInterface(), "150px"); //$NON-NLS-1$

        nicTable.getElement().getStyle().setPosition(Position.RELATIVE);

        nicTable.setSelectionModel(new NoSelectionModel<VmNetworkInterface>());
    }

    private void initDiskTable() {
        diskTable = new ListModelObjectCellTable<>();
        diskTable.enableColumnResizing();
        AbstractTextColumn<DiskImage> aliasColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        diskTable.addColumn(aliasColumn, constants.aliasDisk(), "300px"); //$NON-NLS-1$

        AbstractImageResourceColumn<DiskImage> bootableDiskColumn = new AbstractImageResourceColumn<DiskImage>() {
            @Override
            public ImageResource getValue(DiskImage object) {
                boolean isBoot = !object.getDiskVmElements().isEmpty() && object.getDiskVmElements().iterator().next().isBoot();
                return isBoot ? getDefaultImage() : null;
            }

            @Override
            public ImageResource getDefaultImage() {
                return resources.bootableDiskIcon();
            }

            @Override
            public SafeHtml getTooltip(DiskImage object) {
                if (!object.getDiskVmElements().isEmpty() && object.getDiskVmElements().iterator().next().isBoot()) {
                    return SafeHtmlUtils.fromSafeConstant(constants.bootableDisk());
                }
                return null;
            }
        };
        diskTable.addColumn(bootableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.bootableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.bootableDisk())),
                        "30px"); //$NON-NLS-1$

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

        diskTable.setSelectionModel(new NoSelectionModel<DiskImage>());

        diskTable.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    interface WidgetStyle extends CssResource {
        String cellSelectBox();

        String cdAttachedLabelWidth();
    }
}
