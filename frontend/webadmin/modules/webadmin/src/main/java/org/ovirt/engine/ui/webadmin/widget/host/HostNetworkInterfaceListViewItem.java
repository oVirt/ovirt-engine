package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.ui.common.CellTablePopupTableResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.RxTxTotalRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractIconTypeColumn;
import org.ovirt.engine.ui.common.widget.table.header.IconTypeHeader;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.network.NetworkIcon;
import org.ovirt.engine.ui.common.widget.uicommon.vm.IconStatusPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.renderer.HostVLanNameRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;

public class HostNetworkInterfaceListViewItem extends PatternflyListViewItem<HostInterfaceLineModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    protected static final RxTxRateRenderer rateRenderer = new RxTxRateRenderer();
    protected static final RxTxTotalRenderer totalRenderer = new RxTxTotalRenderer();
    private static final int MAX_LOGICAL_NETWORKS = 1000;
    private static final String ICON_COLOR = "#363636"; // $NON-NLS-1$
    private static final String EXPANSION_CONTAINER = "expansion-container"; // $NON-NLS-1$
    private static final String RATE_DIVIDER = "rate-divider"; // $NON-NLS-1$
    private static final String NIC_SPEED_DROP = "nic-speed-drop"; // $NON-NLS-1$
    private static final String NETWORK_DATA_ROW = "network-data-row"; // $NON-NLS-1$
    private static final String NETWORK_LIST_ITEM = "network-list-item"; // $NON-NLS-1$

    private ExpandableListViewItem logicalNetworkExpand;

    private Container logicalNetworkContainer;
    private Container detailedInfoContainer;
    protected FlowPanel expansionLinkContainer = new FlowPanel();
    protected Span interfaceIconSpan;

    public HostNetworkInterfaceListViewItem(String name, HostInterfaceLineModel entity) {
        super(name, entity);
        applyHostInterfaceSpecificStyles();
        expansionLinkContainer.add(createDetailAdditionalInfo());
        listGroupItem.add(expansionLinkContainer);
        logicalNetworkContainer = createLogicalNetworkContainer();
        logicalNetworkExpand.setDetails(logicalNetworkContainer);
        listGroupItem.add(logicalNetworkContainer);
        // Add handler for clicking on the row itself to cycle through the expansion slots. If there is only one
        // expansion then this is simply a toggle.
        addClickHandler(e -> {
            if (listGroupItem.equals(e.getSource())) {
                cycleExpanded();
            }
        });
    }

    private void applyHostInterfaceSpecificStyles() {
        // Modify some styling from a standard list view item to allow for what we are trying to achieve.
        listGroupItem.addStyleName(NETWORK_LIST_ITEM);
        expansionLinkContainer.addStyleName(EXPANSION_CONTAINER);
        descriptionPanel.getElement().getStyle().setWidth(10, Unit.PCT);
        descriptionPanel.removeStyleName(PatternflyConstants.PF_LIST_VIEW_DESCRIPTION);
    }

    protected void cycleExpanded() {
        setLogicalNetworkExpanded(!getLogicalNetworkState());
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<HostInterfaceLineModel> originalViewItem) {
        if (originalViewItem instanceof HostNetworkInterfaceListViewItem) {
            setLogicalNetworkExpanded(((HostNetworkInterfaceListViewItem) originalViewItem).getLogicalNetworkState());
        }
    }

    @Override
    protected IsWidget createIcon() {
        iconPanel.add(new NetworkIcon());
        return iconPanel;
    }

    protected boolean isInterfaceUp() {
        return getEntity().getInterfaces().get(0).getStatus() == InterfaceStatus.UP;
    }

    @Override
    protected IsWidget createBodyPanel(SafeHtml header, HostInterfaceLineModel entity) {
        checkBoxPanel.add(createInterfaceStatusPanel(isInterfaceUp()));
        descriptionHeaderPanel.getElement().setInnerSafeHtml(header);
        interfaceIconSpan = new Span();
        descriptionHeaderPanel.add(interfaceIconSpan);
        VdsNetworkInterface networkInterface = getNetworkInterface();
        if (networkInterface.getLabels() != null && !networkInterface.getLabels().isEmpty()) {
            interfaceIconSpan.add(createHasLabelStatusPanel());
        }
        createAdditionalInfoPanel();
        return bodyPanel;
    }

    protected void createAdditionalInfoPanel() {
        detailedInfoContainer = new Container();

        detailedInfoContainer.add(createLogicalNetworkInfo(getEntity().getInterfaces().get(0)));
        additionalInfoPanel.add(this.detailedInfoContainer);
    }

    private IsWidget createLogicalNetworkInfo(HostInterface hostInterface) {
        VdsNetworkInterface vdsNetworkInterface = getNetworkInterface();
        Row networkRow = new Row();
        networkRow.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        networkRow.addStyleName(NETWORK_DATA_ROW);
        networkRow.add(createMacColumn(vdsNetworkInterface));
        networkRow.add(createRxColumn(hostInterface));
        networkRow.add(createTxColumn(hostInterface));
        networkRow.add(createSpeedColumn(vdsNetworkInterface));
        networkRow.add(createDropRateColumn(hostInterface));
        return networkRow;
    }

    private IsWidget createMacColumn(VdsNetworkInterface hostInterface) {
        Column macCol = new Column(ColumnSize.SM_2);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()),
                SafeHtmlUtils.fromString(hostInterface.getMacAddress() != null ? hostInterface.getMacAddress()
                        : constants.unAvailablePropertyLabel()), dl);
        macCol.getElement().appendChild(dl);
        return macCol;
    }

    private IsWidget createRxColumn(HostInterface hostInterface) {
        Column rxCol = new Column(ColumnSize.SM_3);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { hostInterface.getRxRate(),
                        hostInterface.getSpeed().doubleValue() })), dl);
        dl.addClassName(Styles.PULL_LEFT);
        rxCol.getElement().appendChild(dl);
        FlowPanel divider = new FlowPanel();
        divider.addStyleName(RATE_DIVIDER);
        rxCol.add(divider);
        dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.rxTotal(), constants.bytes()),
                SafeHtmlUtils.fromString(totalRenderer.render(hostInterface.getRxTotal())), dl);
        dl.addClassName(Styles.PULL_LEFT);
        rxCol.getElement().appendChild(dl);
        return rxCol;
    }

    private IsWidget createTxColumn(HostInterface hostInterface) {
        Column txCol = new Column(ColumnSize.SM_3);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { hostInterface.getTxRate(),
                        hostInterface.getSpeed().doubleValue() })), dl);
        dl.addClassName(Styles.PULL_LEFT);
        txCol.getElement().appendChild(dl);
        FlowPanel divider = new FlowPanel();
        divider.addStyleName(RATE_DIVIDER);
        txCol.add(divider);
        dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.txTotal(), constants.bytes()),
                SafeHtmlUtils.fromString(totalRenderer.render(hostInterface.getTxTotal())), dl);
        dl.addClassName(Styles.PULL_LEFT);
        txCol.getElement().appendChild(dl);
        return txCol;
    }

    private IsWidget createSpeedColumn(VdsNetworkInterface hostInterface) {
        Column speedCol = new Column(ColumnSize.SM_2);
        speedCol.addStyleName(NIC_SPEED_DROP);
        speedCol.add(createSpeedIcon());
        Span valueSpan = new Span();
        if (hostInterface.hasSpeed()) {
            valueSpan.getElement().setInnerSafeHtml(
                    SafeHtmlUtils.fromString(templates.nicSpeed(hostInterface.getSpeed()).asString()));
        } else {
            valueSpan.setText(constants.unAvailablePropertyLabel());
        }
        speedCol.add(valueSpan);
        return speedCol;
    }

    private IsWidget createDropRateColumn(HostInterface hostInterface) {
        Column dropRateCol = new Column(ColumnSize.SM_2);
        dropRateCol.addStyleName(NIC_SPEED_DROP);
        dropRateCol.add(createDropRateIcon());
        Span valueSpan = new Span();
        valueSpan.getElement().setInnerSafeHtml(templates.dropRate(hostInterface.getRxDrop()
                + hostInterface.getTxDrop()));
        dropRateCol.add(valueSpan);
        return dropRateCol;
    }

    private IsWidget createDetailAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        List<HostVLan> logicalNetworks = calculateLogicalNetworks(getEntity());
        List<IsWidget> icons = new ArrayList<>();
        if (containsManagement(logicalNetworks)) {
            icons.add(createManagementStatusPanel());
        }
        if (containsOutOfSync(logicalNetworks)) {
            icons.add(createNeedsSyncStatusPanel());
        }
        String logicalNetworksText = logicalNetworks.size() == 1 ? constants.logicalNetwork() :
            messages.logicalNetworks(logicalNetworks.size());
        logicalNetworkExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(logicalNetworksText), icons);
        getClickHandlerRegistrations().add(logicalNetworkExpand.addClickHandler(this));
        panel.add(logicalNetworkExpand);
        return panel;
    }

    private boolean containsOutOfSync(List<HostVLan> logicalNetworks) {
        return logicalNetworks.stream().anyMatch(v -> isOutOfSync(v.getInterface().getNetworkImplementationDetails()));
    }

    private boolean containsManagement(List<HostVLan> logicalNetworks) {
        return logicalNetworks.stream().anyMatch(v -> v.getInterface().getIsManagement());
    }

    private List<HostVLan> calculateLogicalNetworks(HostInterfaceLineModel entity) {
        List<HostVLan> logicalNetworks = new ArrayList<>();
        VdsNetworkInterface mainInterface = getNetworkInterface();
        if (mainInterface != null && mainInterface.getNetworkName() != null && !mainInterface.getNetworkName().isEmpty()) {
            HostVLan main = new HostVLan();
            main.setInterface(mainInterface);
            main.setNetworkName(mainInterface.getNetworkName());
            main.setIpv4Address(mainInterface.getIpv4Address());
            main.setIpv6Address(mainInterface.getIpv6Address());
            logicalNetworks.add(main);
        }
        if (entity.getVLans() != null) {
            logicalNetworks.addAll(entity.getVLans());
        }
        return logicalNetworks;
    }

    private Container createLogicalNetworkContainer() {
        List<HostVLan> logicalNetworks = calculateLogicalNetworks(getEntity());
        Row content = new Row();
        Column gridColumn = new Column(ColumnSize.SM_12);
        content.add(gridColumn);
        Container container = createItemContainerPanel(content);

        CellTable<HostVLan> logicalNetworkTable = new CellTable<>(MAX_LOGICAL_NETWORKS,
                (Resources)GWT.create(CellTablePopupTableResources.class));
        logicalNetworkTable.getElement().addClassName(Styles.TABLE);
        logicalNetworkTable.getElement().addClassName(PatternflyConstants.PF_TABLE_STRIPED);
        logicalNetworkTable.getElement().addClassName(PatternflyConstants.PF_TABLE_BORDERED);
        ListDataProvider<HostVLan> logicalNetworkDataProvider = new ListDataProvider<>();
        logicalNetworkDataProvider.addDataDisplay(logicalNetworkTable);
        logicalNetworkDataProvider.setList(logicalNetworks);

        final HostVLanNameRenderer renderer = new HostVLanNameRenderer();

        //Setup columns
        AbstractIconTypeColumn<HostVLan> management = new AbstractIconTypeColumn<HostVLan>() {
            @Override
            public IconType getValue(HostVLan logicalNetwork) {
                if (logicalNetwork.getInterface() != null && logicalNetwork.getInterface().getIsManagement()) {
                    return IconType.INSTITUTION;
                }
                return null;
            }

            @Override
            public SafeHtml getTooltip(HostVLan logicalNetwork) {
                return SafeHtmlUtils.fromSafeConstant(constants.managementNetworkLabel());
            }
        };
        IconTypeHeader managementHeader = new IconTypeHeader(IconType.INSTITUTION,
                ICON_COLOR, SafeHtmlUtils.fromSafeConstant(constants.managementNetworkLabel()));

        management.getCell().setColor(SafeHtmlUtils.fromSafeConstant(ICON_COLOR));
        logicalNetworkTable.addColumn(management, managementHeader);
        logicalNetworkTable.setColumnWidth(management, "40px"); // $NON-NLS-1$

        AbstractIconTypeColumn<HostVLan> sync = new AbstractIconTypeColumn<HostVLan>() {
            @Override
            public IconType getValue(HostVLan logicalNetwork) {
                if (logicalNetwork != null && logicalNetwork.getInterface() != null &&
                        isOutOfSync(logicalNetwork.getInterface().getNetworkImplementationDetails())) {
                    return IconType.CHAIN_BROKEN;
                }
                return null;
            }

            @Override
            public SafeHtml getTooltip(HostVLan logicalNetwork) {
                return SafeHtmlUtils.fromSafeConstant(constants.hostOutOfSync());
            }
        };
        IconTypeHeader syncHeader = new IconTypeHeader(IconType.CHAIN_BROKEN,
                ICON_COLOR, SafeHtmlUtils.fromSafeConstant(constants.hostOutOfSync()));

        sync.getCell().setColor(SafeHtmlUtils.fromSafeConstant(RED));
        logicalNetworkTable.addColumn(sync, syncHeader);
        logicalNetworkTable.setColumnWidth(sync, "40px"); // $NON-NLS-1$

        TextColumn<HostVLan> vlan = new TextColumn<HostVLan>() {
            @Override
            public String getValue(HostVLan logicalNetwork) {
                if (logicalNetwork == null || logicalNetwork.getName() == null || logicalNetwork.getName().isEmpty()) {
                    return "";
                }
                return renderer.render(logicalNetwork);
            }
        };
        logicalNetworkTable.setColumnWidth(vlan, "175px"); // $NON-NLS-1$
        logicalNetworkTable.addColumn(vlan, constants.vlanInterface());

        TextColumn<HostVLan> networkName = new TextColumn<HostVLan>() {
            @Override
            public String getValue(HostVLan logicalNetwork) {
                return logicalNetwork.getNetworkName();
            }
        };
        logicalNetworkTable.setColumnWidth(networkName, "175px"); // $NON-NLS-1$
        logicalNetworkTable.addColumn(networkName, constants.networkNameInterface());

        TextColumn<HostVLan> ipv4 = new TextColumn<HostVLan>() {
            @Override
            public String getValue(HostVLan logicalNetwork) {
                return logicalNetwork.getIpv4Address() != null ?
                        logicalNetwork.getIpv4Address() : "";
            }
        };
        logicalNetworkTable.setColumnWidth(ipv4, "175px"); // $NON-NLS-1$
        logicalNetworkTable.addColumn(ipv4, constants.ipv4AddressInterface());

        TextColumn<HostVLan> ipv6 = new TextColumn<HostVLan>() {
            @Override
            public String getValue(HostVLan logicalNetwork) {
                return logicalNetwork.getIpv6Address() != null ?
                        logicalNetwork.getIpv6Address() : "";
            }
        };
        logicalNetworkTable.setColumnWidth(ipv6, "175px"); // $NON-NLS-1$
        logicalNetworkTable.addColumn(ipv6, constants.ipv6AddressInterface());
        TextColumn<HostVLan> emptyTail = new TextColumn<HostVLan>() {
            @Override
            public String getValue(HostVLan logicalNetwork) {
                return "";
            }
        };
        logicalNetworkTable.addColumn(emptyTail, "");

        gridColumn.add(logicalNetworkTable);
        return container;
    }

    private VdsNetworkInterface getNetworkInterface() {
        VdsNetworkInterface hostInterface =
                getEntity().getIsBonded() ? getEntity().getInterface() : getEntity().getInterfaces().get(0).getInterface();
        return hostInterface;
    }

    private boolean isOutOfSync(NetworkImplementationDetails networkImplementationDetails) {
        boolean managed = false;
        boolean sync = false;
        if (networkImplementationDetails != null) {
            managed = networkImplementationDetails.isManaged();
            sync = networkImplementationDetails.isInSync();
        }
        return managed && !sync;
    }

    protected IsWidget createManagementStatusPanel() {
        WidgetTooltip tooltip = new WidgetTooltip(new IconStatusPanel(IconType.INSTITUTION));
        tooltip.setText(constants.managementNetworkLabel());
        return tooltip;
    }

    protected IsWidget createDropRateIcon() {
        WidgetTooltip tooltip = new WidgetTooltip(new IconStatusPanel(IconType.ARROW_DOWN));
        tooltip.setText(constants.dropsInterface());
        return tooltip;
    }

    protected IsWidget createSpeedIcon() {
        WidgetTooltip tooltip = new WidgetTooltip(new IconStatusPanel(IconType.BOLT));
        tooltip.setText(constants.speedInterface());
        return tooltip;
    }

    protected IsWidget createNeedsSyncStatusPanel() {
        IconStatusPanel outOfSync = new IconStatusPanel(IconType.CHAIN_BROKEN);
        outOfSync.setColor(RED);
        WidgetTooltip tooltip = new WidgetTooltip(outOfSync);
        tooltip.setText(constants.hostOutOfSync());
        return tooltip;
    }

    protected IsWidget createHasLabelStatusPanel() {
        IconStatusPanel tagPanel = new IconStatusPanel(IconType.TAG);
        tagPanel.addStyleName(PatternflyConstants.LIST_VIEW_ICON_PANEL);
        WidgetTooltip tooltip = new WidgetTooltip(tagPanel);
        tooltip.setHtml(createLabelTooltipText(getNetworkInterface().getLabels()));
        return tooltip;
    }

    private SafeHtml createLabelTooltipText(Set<String> labels) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant(labels.stream().sorted().collect(Collectors.joining("<BR />"))); // $NON-NLS-1$
        return builder.toSafeHtml();
    }

    protected IsWidget createInterfaceStatusPanel(boolean isUp) {
        IconStatusPanel iconStatusPanel = new IconStatusPanel(
                isUp ? IconType.ARROW_CIRCLE_O_UP : IconType.ARROW_CIRCLE_O_DOWN);
        iconStatusPanel.addStyleName(DOUBLE_SIZE);
        iconStatusPanel.getElement().getStyle().setColor(isUp ? GREEN : RED);
        return iconStatusPanel;
    }

    @Override
    protected void hideAllDetails() {
        logicalNetworkExpand.toggleExpanded(false);
    }

    public boolean getLogicalNetworkState() {
        return logicalNetworkExpand.isActive();
    }

    public void setLogicalNetworkExpanded(boolean value) {
        logicalNetworkExpand.toggleExpanded(value);
        toggleExpanded();
    }

    @Override
    protected void toggleExpanded() {
        if (!logicalNetworkExpand.isActive()) {
            listGroupItem.removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            listGroupItem.addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    @Override
    protected void toggleExpanded(boolean expand) {
        setLogicalNetworkExpanded(expand);
    }
}
