package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CellTablePopupTableResources;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class VmSnapshotListViewItem extends PatternflyListViewItem<Snapshot> {

    private static final String DL_HORIZONTAL = "dl-horizontal"; // $NON-NLS-1$
    private static final String VM_NIC_INFO_COLUMN = "vm-nic-info-column"; // $NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final FullDateTimeRenderer dateRenderer = new FullDateTimeRenderer();
    private static final DiskSizeRenderer<Long> sizeRenderer = new DiskSizeRenderer<>(SizeConverter.SizeUnit.BYTES);

    private static final FullDateTimeRenderer fullDateTimeRenderer = new FullDateTimeRenderer();

    private ExpandableListViewItem generalExpand;
    private ExpandableListViewItem disksExpand;
    private ExpandableListViewItem nicsExpand;
    private ExpandableListViewItem installedAppsExpand;

    public VmSnapshotListViewItem(String name, Snapshot snapshot, VmSnapshotListModel listModel,
            SnapshotModel snapshotModel) {
        super(name, snapshot);
        Container generalInfoContainer = createGeneralItemContainerPanel(snapshot, listModel);
        generalExpand.setDetails(generalInfoContainer);
        listGroupItem.add(generalInfoContainer);
        updateValues(snapshotModel);
    }

    private Container createInstalledAppsItemContainerPanel(List<String> appList) {
        Row row = new Row();
        Column column = new Column(ColumnSize.MD_12);
        row.add(column);
        Container container = createItemContainerPanel(row);
        for (String appName: appList) {
            column.getElement().setInnerHTML(appName);
            row = new Row();
            column = new Column(ColumnSize.MD_12);
            row.add(column);
            container.add(row);
        }
        if (appList.isEmpty()) {
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }
        return container;
    }

    private Container createNicsItemContainerPanel(List<VmNetworkInterface> nics) {
        RxTxRateRenderer rateRenderer = new RxTxRateRenderer();
        Row content = new Row();
        Container container = createItemContainerPanel(content);
        int i = 0;
        for (VmNetworkInterface nic: nics) {
            if (i % 4 == 0 && i > 0) {
                content = new Row();
                container.add(content);
            }
            Column column = new Column(calculateColSize(i));
            column.addStyleName(VM_NIC_INFO_COLUMN);
            content.add(column);
            DListElement dl = Document.get().createDLElement();
            dl.addClassName(DL_HORIZONTAL);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.nameInterface()),
                    SafeHtmlUtils.fromString(nic.getName()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()),
                    SafeHtmlUtils.fromString(nic.getNetworkName() == null ? constants.unAvailablePropertyLabel() :
                            nic.getNetworkName()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.profileNameInterface()),
                    SafeHtmlUtils.fromString(nic.getVnicProfileName() == null ? messages.emptyProfile().asString() :
                            nic.getVnicProfileName()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeInterface()),
                    SafeHtmlUtils.fromString(VmInterfaceType.forValue(nic.getType()).getDescription()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()),
                    SafeHtmlUtils.fromString(nic.getMacAddress()), dl);
            addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                    SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { nic.getStatistics().getReceiveRate(),
                            nic.hasSpeed() ? nic.getSpeed().doubleValue() : 0})), dl);
            addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                    SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { nic.getStatistics().getTransmitRate(),
                            nic.hasSpeed() ? nic.getSpeed().doubleValue() : 0})), dl);
            addDetailItem(templates.sub(constants.dropsInterface(), constants.pkts()),
                    SafeHtmlUtils.fromString(String.valueOf(nic.getStatistics().getReceiveDrops() != null ?
                            nic.getStatistics().getReceiveDrops() : ""
                            + nic.getStatistics().getTransmitDrops())), dl);
            column.getElement().appendChild(dl);
            i++;
        }
        if (nics.isEmpty()) {
            Column column = new Column(ColumnSize.MD_12);
            content.add(column);
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }
        return container;
    }

    private Container createDisksItemContainerPanel(List<DiskImage> diskImages) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);
        Container container = createItemContainerPanel(content);

        CellTable<DiskImage> disksTable = new CellTable<>(1000,
                (CellTable.Resources)GWT.create(CellTablePopupTableResources.class));

        disksTable.setWidth("98%"); // $NON-NLS-1$

        AbstractTextColumn<DiskImage> statusDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return getImageStatus(object.getImageStatus());
            }
        };
        disksTable.addColumn(statusDisk, constants.statusDisk());

        AbstractTextColumn<DiskImage> aliasDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        disksTable.addColumn(aliasDisk, constants.aliasDisk());

        AbstractTextColumn<DiskImage> provisionedSizeDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(sizeRenderer.render(object.getSize()));
            }
        };
        disksTable.addColumn(provisionedSizeDisk, constants.provisionedSizeDisk());

        AbstractTextColumn<DiskImage> sizeDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(sizeRenderer.render(object.getActualSizeInBytes()));
            }
        };
        disksTable.addColumn(sizeDisk, constants.sizeDisk());

        AbstractTextColumn<DiskImage> allocationDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(VolumeType.forValue(object.getVolumeType().getValue()));
            }
        };
        disksTable.addColumn(allocationDisk, constants.allocationDisk());

        AbstractTextColumn<DiskImage> interfaceDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return getInterface(object);
            }
        };
        disksTable.addColumn(interfaceDisk, constants.interfaceDisk());

        AbstractTextColumn<DiskImage> creationDateDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return dateRenderer.render(object.getCreationDate());
            }
        };
        disksTable.addColumn(creationDateDisk, constants.creationDateDisk());

        AbstractTextColumn<DiskImage> diskSnapshotIDDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(object.getImageId());
            }
        };
        disksTable.addColumn(diskSnapshotIDDisk, constants.diskSnapshotIDDisk());

        AbstractTextColumn<DiskImage> typeDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return String.valueOf(object.getDiskStorageType());
            }
        };
        disksTable.addColumn(typeDisk, constants.typeDisk());

        AbstractTextColumn<DiskImage> descriptionDisk = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return StringHelper.isNotNullOrEmpty(object.getDiskDescription()) ? object.getDiskDescription()
                        : constants.notAvailableLabel();
            }
        };
        disksTable.addColumn(descriptionDisk, constants.descriptionDisk());

        if (!diskImages.isEmpty()) {
            column.add(disksTable);
        } else {
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }

        disksTable.setRowData(diskImages);
        disksTable.getElement().getStyle().setMarginBottom(15, Style.Unit.PX);
        return container;
    }

    private String getInterface(DiskImage image) {
        if (image.getDiskVmElements().size() == 1) {
            return image.getDiskVmElements().iterator().next().getDiskInterface().toString();
        }
        return constants.notAvailableLabel();
    }

    private String getImageStatus(ImageStatus status) {
        switch (status) {
        case OK:
            return constants.up();
        case LOCKED:
            return constants.imageLocked();
        case ILLEGAL:
            return constants.illegalStatus();
        default:
            return constants.notAvailableLabel();
        }
    }

    private Container createGeneralItemContainerPanel(Snapshot snapshot, VmSnapshotListModel listModel) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.dateSnapshot()),
                SafeHtmlUtils.fromString(getCreateDateString(snapshot)), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.statusSnapshot()),
                SafeHtmlUtils.fromString(snapshot.getStatus().name()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.memorySnapshot()),
                SafeHtmlUtils.fromString(String.valueOf(snapshot.containsMemory())), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.descriptionSnapshot()),
                SafeHtmlUtils.fromTrustedString(getDescription(snapshot)), dl);
        VM entity = listModel.getEntity();
        if (entity != null) {
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.definedMemoryVm()),
                    SafeHtmlUtils.fromString(messages.megabytes(String.valueOf(entity.getVmMemSizeMb()))), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.physMemGauranteedVm()),
                    SafeHtmlUtils.fromString(messages.megabytes(String.valueOf(entity.getMinAllocatedMem()))), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.numOfCpuCoresVm()),
                    SafeHtmlUtils.fromString(messages.cpuInfoLabel(entity.getNumOfCpus(),
                            entity.getNumOfSockets(), entity.getCpuPerSocket(),
                            entity.getThreadsPerCpu())), dl);
        }
        column.getElement().appendChild(dl);
        return createItemContainerPanel(content);
    }

    private String getDescription(Snapshot snapshot) {
        String description = SafeHtmlUtils.fromString(snapshot.getDescription()).asString();

        if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
            List<String> previewedItems = new ArrayList<>(Arrays.asList(constants.vmConfiguration()));
            previewedItems.addAll(Linq.getDiskAliases(snapshot.getDiskImages()));
            description = messages.snapshotPreviewing(
                    description, String.join(", ", previewedItems)); //$NON-NLS-1$
        } else if (snapshot.getType() == SnapshotType.STATELESS) {
            description = description + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (snapshot.getType() == SnapshotType.PREVIEW) {
            description = constants.snapshotDescriptionActiveVmBeforePreview();
        } else if (snapshot.getType() == SnapshotType.ACTIVE) {
            description = constants.snapshotDescriptionActiveVm();
        } else if (snapshot.getType() == SnapshotType.REGULAR && !snapshot.getDiskImages().isEmpty()) {
            description = messages.snapshotPreviewing(
                    description, String.join(", ", Linq.getDiskAliases(snapshot.getDiskImages()))); //$NON-NLS-1$
        } else if (snapshot.isVmConfigurationBroken()) {
            description = description + " (" + constants.brokenVmConfiguration() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return description;
    }

    private String getCreateDateString(Snapshot snapshot) {
        if (snapshot.getType() == SnapshotType.ACTIVE) {
            return constants.currentSnapshotLabel();
        }
        return fullDateTimeRenderer.render(snapshot.getCreationDate());
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<Snapshot> originalViewItem) {
        VmSnapshotListViewItem original = (VmSnapshotListViewItem) originalViewItem;
        setGeneralExpanded(original.getGeneralState());
        setDisksExpanded(original.getDisksState());
        setNicsExpanded(original.getNicsState());
        setInstalledAppsExpanded(original.getInstalledAppsState());
    }

    @Override
    protected IsWidget createIcon() {
        Span iconSpan = new Span();
        iconSpan.addStyleName(Styles.FONT_AWESOME_BASE);
        if (SnapshotStatus.IN_PREVIEW.equals(getEntity().getStatus())) {
            iconSpan.addStyleName(IconType.EYE.getCssName());
        } else if (SnapshotStatus.LOCKED.equals(getEntity().getStatus())) {
            iconSpan.addStyleName(IconType.LOCK.getCssName());
        } else if (getEntity().containsMemory()) {
            iconSpan.add(createSnapshotWithMemoryIcon());
        } else {
            iconSpan.addStyleName(IconType.CAMERA.getCssName());
        }
        iconSpan.addStyleName(PatternflyConstants.PF_LIST_VIEW_ICON_SM);
        iconPanel.add(iconSpan);
        return iconPanel;
    }

    private Span createSnapshotWithMemoryIcon() {
        Span holder = new Span();
        holder.setStyleName(Styles.ICON_STACK);
        holder.getElement().getStyle().setLeft(-0.2, Style.Unit.EM);
        holder.getElement().getStyle().setTop(-0.2, Style.Unit.EM);

        Italic cameraItalic = new Italic();
        cameraItalic.addStyleName(Styles.FONT_AWESOME_BASE);
        cameraItalic.addStyleName(IconType.CAMERA.getCssName());
        cameraItalic.getElement().getStyle().setFontSize(1, Style.Unit.EM);

        Italic memoryItalic = new Italic();
        memoryItalic.addStyleName(Styles.FONT_AWESOME_BASE);
        memoryItalic.addStyleName(Styles.ICON_STACK_TOP);
        memoryItalic.addStyleName(PatternflyConstants.PFICON_MEMORY);
        memoryItalic.getElement().getStyle().setTop(-4, Style.Unit.PX);
        memoryItalic.getElement().getStyle().setLeft(18, Style.Unit.PX);
        memoryItalic.getElement().getStyle().setColor("#0088ce"); //$NON-NLS-1$
        memoryItalic.getElement().getStyle().setFontSize(11, Style.Unit.PX);
        memoryItalic.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);

        holder.add(cameraItalic);
        holder.add(memoryItalic);

        Tooltip memoryInfoToolTip = new Tooltip(memoryItalic);
        memoryInfoToolTip.setTitle(constants.snapshotContainsMemory());
        memoryInfoToolTip.setPlacement(Placement.RIGHT);
        holder.add(memoryInfoToolTip);
        return holder;
    }

    @Override
    protected IsWidget createBodyPanel(SafeHtml header, Snapshot entity) {
        descriptionHeaderPanel.getElement().setInnerSafeHtml(header);
        createAdditionalInfoPanel();
        return bodyPanel;
    }

    private void createAdditionalInfoPanel() {
        additionalInfoPanel.add(createGeneralAdditionalInfo());
        additionalInfoPanel.add(createDisksAdditionalInfo());
        additionalInfoPanel.add(createNicsAdditionalInfo());
        additionalInfoPanel.add(createAppsAdditionalInfo());
    }

    private IsWidget createGeneralAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        generalExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(constants.generalLabel()),
                IconType.EYE.getCssName());
        getClickHandlerRegistrations().add(generalExpand.addClickHandler(this));
        panel.add(generalExpand);
        return panel;
    }

    private IsWidget createDisksAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        disksExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(constants.disksLabel()),
                IconType.DATABASE.getCssName());
        getClickHandlerRegistrations().add(disksExpand.addClickHandler(this));
        panel.add(disksExpand);
        return panel;
    }

    private IsWidget createNicsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        nicsExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(constants.nicsLabel()),
                PatternflyIconType.PF_NETWORK.getCssName());
        getClickHandlerRegistrations().add(nicsExpand.addClickHandler(this));
        panel.add(nicsExpand);
        return panel;
    }

    private IsWidget createAppsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        installedAppsExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(constants.applicationsLabel()),
                IconType.NEWSPAPER_O.getCssName());
        getClickHandlerRegistrations().add(installedAppsExpand.addClickHandler(this));
        panel.add(installedAppsExpand);
        return panel;
    }

    @Override
    protected void hideAllDetails() {
        generalExpand.toggleExpanded(false);
        disksExpand.toggleExpanded(false);
        nicsExpand.toggleExpanded(false);
        installedAppsExpand.toggleExpanded(false);
    }

    @Override
    protected void toggleExpanded() {
        if (!generalExpand.isActive() && !disksExpand.isActive() && !nicsExpand.isActive()
                && !installedAppsExpand.isActive()) {
            removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    @Override
    protected void toggleExpanded(boolean expand) {
        // No-op for now as we don't have an expand all option.
    }

    public boolean getGeneralState() {
        return generalExpand.isActive();
    }

    public void setGeneralExpanded(boolean value) {
        generalExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getDisksState() {
        return disksExpand.isActive();
    }

    public void setDisksExpanded(boolean value) {
        disksExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getNicsState() {
        return nicsExpand.isActive();
    }

    public void setNicsExpanded(boolean value) {
        nicsExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getInstalledAppsState() {
        return installedAppsExpand.isActive();
    }

    public void setInstalledAppsExpanded(boolean value) {
        installedAppsExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public void updateValues(SnapshotModel snapshotModel) {
        Container currentDetails = disksExpand.getDetails();
        if (currentDetails != null) {
            listGroupItem.remove(currentDetails);
        }
        Container disksInfoContainer = createDisksItemContainerPanel(snapshotModel.getDisks());
        disksExpand.setDetails(disksInfoContainer);
        disksExpand.toggleExpanded(disksExpand.isActive());
        listGroupItem.add(disksInfoContainer);

        currentDetails = nicsExpand.getDetails();
        if (currentDetails != null) {
            listGroupItem.remove(currentDetails);
        }
        Container nicsInfoContainer = createNicsItemContainerPanel(snapshotModel.getNics());
        nicsExpand.setDetails(nicsInfoContainer);
        nicsExpand.toggleExpanded(nicsExpand.isActive());
        listGroupItem.add(nicsInfoContainer);

        currentDetails = installedAppsExpand.getDetails();
        if (currentDetails != null) {
            listGroupItem.remove(currentDetails);
        }
        Container installedAppsInfoContainer = createInstalledAppsItemContainerPanel(snapshotModel.getApps());
        installedAppsExpand.setDetails(installedAppsInfoContainer);
        installedAppsExpand.toggleExpanded(installedAppsExpand.isActive());
        listGroupItem.add(installedAppsInfoContainer);
    }
}
