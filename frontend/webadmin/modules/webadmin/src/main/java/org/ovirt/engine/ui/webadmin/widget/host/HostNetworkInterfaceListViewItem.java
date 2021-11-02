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
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.RxTxTotalRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractIconTypeColumn;
import org.ovirt.engine.ui.common.widget.table.header.IconTypeHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.network.NetworkIcon;
import org.ovirt.engine.ui.common.widget.uicommon.vm.IconStatusPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVLan;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
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
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
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
    private static final String MAC_ADDRESS_WORD_BREAK = "break-word"; // $NON-NLS-1$
    private static final SafeHtml UNMANAGED_TOOLTIP_SAFE_HTML = SafeHtmlUtils.fromSafeConstant(
        constants.unmanagedNetworkItemInfo().concat(
        constants.lineBreak()).concat(
        constants.unmanagedNetworkDescriptionItemInfo())
    );

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

        detailedInfoContainer.add(createLogicalNetworkInfo());
        additionalInfoPanel.add(this.detailedInfoContainer);
    }

    private IsWidget createLogicalNetworkInfo() {
        VdsNetworkInterface vdsNetworkInterface = getNetworkInterface();
        Row networkRow = new Row();
        networkRow.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        networkRow.addStyleName(NETWORK_DATA_ROW);
        networkRow.add(createMacColumn(vdsNetworkInterface));
        networkRow.add(createRxColumn(vdsNetworkInterface));
        networkRow.add(createTxColumn(vdsNetworkInterface));
        networkRow.add(createSpeedColumn(vdsNetworkInterface));
        networkRow.add(createDropRateColumn(vdsNetworkInterface));
        return networkRow;
    }

    private IsWidget createMacColumn(VdsNetworkInterface hostInterface) {
        Column macCol = new Column(ColumnSize.SM_2);
        macCol.addStyleName(MAC_ADDRESS_WORD_BREAK);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()),
                SafeHtmlUtils.fromString(hostInterface.getMacAddress() != null ? hostInterface.getMacAddress()
                        : constants.unAvailablePropertyLabel()), dl);
        macCol.getElement().appendChild(dl);
        return macCol;
    }

    private IsWidget createRxColumn(VdsNetworkInterface hostInterface) {
        Column rxCol = new Column(ColumnSize.SM_3);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] {
                        hostInterface.getStatistics().getReceiveRate(),
                        hostInterface.hasSpeed() ? hostInterface.getSpeed().doubleValue() : 0.0 })), dl);
        dl.addClassName(Styles.PULL_LEFT);
        rxCol.getElement().appendChild(dl);
        FlowPanel divider = new FlowPanel();
        divider.addStyleName(RATE_DIVIDER);
        rxCol.add(divider);
        dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.rxTotal(), constants.bytes()),
                SafeHtmlUtils.fromString(totalRenderer.render(hostInterface.getStatistics().getReceivedBytes())), dl);
        dl.addClassName(Styles.PULL_LEFT);
        rxCol.getElement().appendChild(dl);
        return rxCol;
    }

    private IsWidget createTxColumn(VdsNetworkInterface hostInterface) {
        Column txCol = new Column(ColumnSize.SM_3);
        DListElement dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] {
                        hostInterface.getStatistics().getTransmitRate(),
                        hostInterface.hasSpeed() ? hostInterface.getSpeed().doubleValue() : 0.0 })), dl);
        dl.addClassName(Styles.PULL_LEFT);
        txCol.getElement().appendChild(dl);
        FlowPanel divider = new FlowPanel();
        divider.addStyleName(RATE_DIVIDER);
        txCol.add(divider);
        dl = Document.get().createDLElement();
        addDetailItem(templates.sub(constants.txTotal(), constants.bytes()),
                SafeHtmlUtils.fromString(totalRenderer.render(hostInterface.getStatistics().getTransmittedBytes())),
                dl);
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

    private IsWidget createDropRateColumn(VdsNetworkInterface hostInterface) {
        Column dropRateCol = new Column(ColumnSize.SM_2);
        dropRateCol.addStyleName(NIC_SPEED_DROP);
        dropRateCol.add(createDropRateIcon());
        Span valueSpan = new Span();
        valueSpan.getElement()
                .setInnerSafeHtml(templates.dropRate(
                hostInterface.getStatistics().getReceiveDrops().doubleValue() +
                      hostInterface.getStatistics().getTransmitDrops().doubleValue()
                ));
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
        if (containsUnmanaged(logicalNetworks)) {
            icons.add(createUnmanagedStatusPanel());
        }
        String logicalNetworksText = logicalNetworks.size() == 1 ? constants.logicalNetwork() :
            messages.logicalNetworks(logicalNetworks.size());
        logicalNetworkExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(logicalNetworksText), icons);
        getClickHandlerRegistrations().add(logicalNetworkExpand.addClickHandler(this));
        panel.add(logicalNetworkExpand);
        return panel;
    }

    private boolean containsOutOfSync(List<HostVLan> logicalNetworks) {
        return logicalNetworks.stream().anyMatch(this::isOutOfSync);
    }

    private boolean containsManagement(List<HostVLan> logicalNetworks) {
        return logicalNetworks.stream().anyMatch(this::isManagementNetwork);
    }

    private boolean containsUnmanaged(List<HostVLan> logicalNetworks) {
        return logicalNetworks.stream().anyMatch(iface -> !this.isManaged(iface));
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
                return isManagementNetwork(logicalNetwork) ? IconType.INSTITUTION : null;
            }

            @Override
            public SafeHtml getTooltip(HostVLan logicalNetwork) {
                return isManagementNetwork(logicalNetwork) ?
                        SafeHtmlUtils.fromSafeConstant(constants.managementNetworkLabel()) :
                        null;
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
                return isOutOfSync(logicalNetwork) ? IconType.CHAIN_BROKEN : null;
            }

            @Override
            public SafeHtml getTooltip(HostVLan logicalNetwork) {
                return isOutOfSync(logicalNetwork) ? SafeHtmlUtils.fromSafeConstant(constants.hostOutOfSync()) : null;
            }
        };
        IconTypeHeader syncHeader = new IconTypeHeader(IconType.CHAIN_BROKEN,
                ICON_COLOR, SafeHtmlUtils.fromSafeConstant(constants.hostOutOfSync()));

        sync.getCell().setColor(SafeHtmlUtils.fromSafeConstant(RED));
        logicalNetworkTable.addColumn(sync, syncHeader);
        logicalNetworkTable.setColumnWidth(sync, "40px"); // $NON-NLS-1$

        AbstractIconTypeColumn<HostVLan> unmanagedColumn = new AbstractIconTypeColumn<HostVLan>() {
            @Override
            public IconType getValue(HostVLan logicalNetwork) {
                return isManaged(logicalNetwork) ? null : IconType.QUESTION;
            }

            @Override
            public SafeHtml getTooltip(HostVLan logicalNetwork) {
                return isManaged(logicalNetwork) ? null : UNMANAGED_TOOLTIP_SAFE_HTML;
            }
        };
        SafeHtmlHeader unmanagedHeader = new SafeHtmlHeader(
            SafeHtmlUtils.fromSafeConstant(constants.unmanagedNetworkItemTitle()), UNMANAGED_TOOLTIP_SAFE_HTML);
        unmanagedColumn.getCell().setColor(SafeHtmlUtils.fromSafeConstant(ICON_COLOR));
        logicalNetworkTable.addColumn(unmanagedColumn, unmanagedHeader);
        logicalNetworkTable.setColumnWidth(unmanagedColumn, "90px"); // $NON-NLS-1$

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

    private boolean isManagementNetwork(HostVLan iface) {
        return hasInternalInterface(iface) && iface.getInterface().getIsManagement();
    }

    private boolean isOutOfSync(HostVLan iface) {
        if (!hasInternalInterface(iface)) {
            return false;
        }
        NetworkImplementationDetails networkImplementationDetails =
                iface.getInterface().getNetworkImplementationDetails();
        boolean managed = false;
        boolean sync = false;
        if (networkImplementationDetails != null) {
            managed = networkImplementationDetails.isManaged();
            sync = networkImplementationDetails.isInSync();
        }
        return managed && !sync;
    }

    private boolean isManaged(HostVLan iface) {
        return hasInternalInterface(iface) && hasNetworkImplementationDetails(iface) &&
            iface.getInterface().getNetworkImplementationDetails().isManaged();
    }

    private boolean hasNetworkImplementationDetails(HostVLan iface) {
        return iface.getInterface().getNetworkImplementationDetails() != null;
    }

    private boolean hasInternalInterface(HostVLan iface) {
        return iface != null && iface.getInterface() != null;
    }

    protected IsWidget createManagementStatusPanel() {
        WidgetTooltip tooltip = new WidgetTooltip(new IconStatusPanel(IconType.INSTITUTION));
        tooltip.setText(constants.managementNetworkLabel());
        return tooltip;
    }

    protected IsWidget createUnmanagedStatusPanel() {
        WidgetTooltip tooltip = new WidgetTooltip(new IconStatusPanel(IconType.QUESTION));
        tooltip.setHtml(UNMANAGED_TOOLTIP_SAFE_HTML);
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
