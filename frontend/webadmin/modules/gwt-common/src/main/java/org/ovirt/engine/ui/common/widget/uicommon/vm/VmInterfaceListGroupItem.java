package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.common.widget.renderer.RxTxTotalRenderer;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.network.NetworkIcon;

import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class VmInterfaceListGroupItem extends PatternflyListViewItem<VmNetworkInterface> {

    private static final String COMMA_DELIMITER = ", "; // $NON-NLS-1$
    private static final String DANGER = "text-danger"; // $NON-NLS-1$
    private static final String ROTATE_270 = "fa-rotate-270"; //$NON-NLS-1$
    private static final String DL_HORIZONTAL = "dl-horizontal"; // $NON-NLS-1$
    private static final String NETWORK_DATA_ROW = "network-data-row"; // $NON-NLS-1$
    private static final String VM_NIC_OVERFLOW = "vm-nic-overflow"; // $NON-NLS-1$
    private static final String STATUS_ICON = "status-icon"; // $NON-NLS-1$
    private static final String VM_NIC_INFO_HEADING = "vm-nic-info-heading"; // $NON-NLS-1$
    private static final String VM_NIC_INFO_COLUMN = "vm-nic-info-column"; // $NON-NLS-1$
    private static final String VM_NIC_INFO_ROW = "vm-nic-info-row"; // $NON-NLS-1$
    private static final String NETWORK_LIST_ITEM = "network-list-item"; // $NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private ExpandableListViewItem infoExpand;
    private final List<VmGuestAgentInterface> allGuestAgentData;
    private Container detailedInfoContainer;
    protected FlowPanel expansionLinkContainer = new FlowPanel();

    public VmInterfaceListGroupItem(VmNetworkInterface networkInterface, List<VmGuestAgentInterface> allGuestAgentData,
            List<VmNicFilterParameter> networkFilterParameters) {
        super(networkInterface.getName(), networkInterface);
        applyVmInterfaceSpecificStyles();
        this.allGuestAgentData = allGuestAgentData;
        displayImportantNicInfo(networkInterface);
        listGroupItem.add(expansionLinkContainer);
        Container infoContainer = createInfoContainerPanel(networkInterface, networkFilterParameters);
        infoExpand.setDetails(infoContainer);
        listGroupItem.add(infoContainer);

    }

    private void applyVmInterfaceSpecificStyles() {
        listGroupItem.addStyleName(NETWORK_LIST_ITEM);
        descriptionPanel.getElement().getStyle().setWidth(10, Style.Unit.PCT);
        descriptionPanel.removeStyleName(PatternflyConstants.PF_LIST_VIEW_DESCRIPTION);
    }

    private Column createInfoColumn(String headerValue, boolean withBorder) {
        Column column = new Column(ColumnSize.MD_4);

        if (withBorder) {
            column.addStyleName(VM_NIC_INFO_COLUMN);
        }

        Div header = new Div();
        header.addStyleName(VM_NIC_INFO_HEADING);
        header.getElement().setInnerSafeHtml(SafeHtmlUtils.fromSafeConstant(headerValue));
        column.add(header);

        return column;
    }

    private void createNetworkFilterParametersColumn(List<VmNicFilterParameter> networkFilterParameters,
            Row content) {
        Column column = createInfoColumn(constants.networkFilterParametersLabel(), false);

        networkFilterParameters.forEach(parameter -> {
            DListElement dl = Document.get().createDLElement();
            dl.addClassName(DL_HORIZONTAL);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.nameNetworkFilterParameter()),
                    SafeHtmlUtils.fromString(parameter.getName()),
                    dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.valueNetworkFilterParameter()),
                    SafeHtmlUtils.fromString(parameter.getValue()),
                    dl);
            column.getElement().appendChild(dl);
        });

        content.add(column);
    }

    private Container createInfoContainerPanel(VmNetworkInterface networkInterface,
            List<VmNicFilterParameter> networkFilterParameters) {
        Row row = new Row();
        row.addStyleName(VM_NIC_INFO_ROW);

        createGeneralItemColumn(networkInterface, row);
        createStatisticsItemColumn(networkInterface, row);
        createNetworkFilterParametersColumn(networkFilterParameters, row);
        return createItemContainerPanel(row);
    }

    private void createGeneralItemColumn(VmNetworkInterface networkInterface, Row content) {
        Column column = createInfoColumn(constants.generalLabel(), true);

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.plugged()),
                renderPlugged(networkInterface.isPlugged()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()),
                SafeHtmlUtils.fromString(networkInterface.getNetworkName() != null ? networkInterface.getNetworkName() : ""), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.profileNameInterface()),
                SafeHtmlUtils.fromString(networkInterface.getVnicProfileName() != null ? networkInterface.getVnicProfileName() : ""), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.vmNetworkQosName()),
                SafeHtmlUtils.fromString(StringHelper.isNullOrEmpty(networkInterface.getQosName())
                    ? constants.notAvailableLabel() : networkInterface.getQosName()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.linkStateNetworkInterface()),
                renderLinkState(networkInterface.isLinked()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeInterface()),
                SafeHtmlUtils.fromString(VmInterfaceType.forValue(networkInterface.getType()).getDescription()), dl);
        addDetailItem(templates.sub(constants.speedInterface(), constants.mbps()),
                SafeHtmlUtils.fromString(String.valueOf(networkInterface.getSpeed())), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.portMirroring()),
                renderPortMirroring(networkInterface.isPortMirroring()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.guestInterfaceName()),
                SafeHtmlUtils.fromString(findGuestAgentDataForInterface(networkInterface).getInterfaceName() != null ?
                        findGuestAgentDataForInterface(networkInterface).getInterfaceName()
                        : constants.unAvailablePropertyLabel()), dl);
        column.getElement().appendChild(dl);
        content.add(column);
    }

    private SafeHtml renderPortMirroring(boolean portMirroring) {
        if (portMirroring) {
            return SafeHtmlUtils.fromString(constants.portMirroringEnabled());
        } else {
            return SafeHtmlUtils.fromString(constants.portMirroringDisabled());
        }
    }

    private SafeHtml renderLinkState(boolean linkState) {
        if (linkState) {
            return SafeHtmlUtils.fromString(constants.up());
        } else {
            return SafeHtmlUtils.fromString(constants.down());
        }
    }

    private SafeHtml renderPlugged(boolean pluggedStatus) {
        if (pluggedStatus) {
            return SafeHtmlUtils.fromString(constants.plugged());
        } else {
            return SafeHtmlUtils.fromString(constants.unplugged());
        }
    }

    private void createStatisticsItemColumn(VmNetworkInterface networkInterface, Row content) {
        Column column = createInfoColumn(constants.statistics(), true);

        RxTxRateRenderer rateRenderer = new RxTxRateRenderer();
        RxTxTotalRenderer totalRenderer = new RxTxTotalRenderer();

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { networkInterface.getStatistics().getReceiveRate(),
                        networkInterface.hasSpeed() ? networkInterface.getSpeed().doubleValue() : 0})), dl);

        addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                SafeHtmlUtils.fromString(rateRenderer.render(new Double[] { networkInterface.getStatistics().getTransmitRate(),
                        networkInterface.hasSpeed() ? networkInterface.getSpeed().doubleValue() : 0})), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.rxTotal()),
                SafeHtmlUtils.fromString(totalRenderer.render(networkInterface.getStatistics().getReceivedBytes())), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.txTotal()),
                SafeHtmlUtils.fromString(totalRenderer.render(networkInterface.getStatistics().getTransmittedBytes())), dl);
        addDetailItem(templates.sub(constants.dropsInterface(), constants.pkts()),
                SafeHtmlUtils.fromString(String.valueOf(networkInterface.getStatistics().getReceiveDropRate()
                        + networkInterface.getStatistics().getTransmitDropRate())), dl);
        column.getElement().appendChild(dl);
        content.add(column);
    }

    private boolean isInterfaceUp(VmNetworkInterface networkInterface) {
        return networkInterface != null ? networkInterface.isLinked() : false;
    }

    private boolean isCardPlugged(VmNetworkInterface networkInterface) {
        return networkInterface != null ? networkInterface.isPlugged() : false;
    }

    @Override
    protected IsWidget createBodyPanel(SafeHtml header, VmNetworkInterface networkInterface) {
        checkBoxPanel.add(createExpandIconPanel());
        iconPanel.add(createLinkStatusPanel(isInterfaceUp(networkInterface)));
        iconPanel.add(createCardPluggedStatusPanel(isCardPlugged(networkInterface)));
        iconPanel.add(new NetworkIcon());
        descriptionHeaderPanel.getElement().setInnerSafeHtml(header);
        return bodyPanel;
    }

    protected void displayImportantNicInfo(VmNetworkInterface networkInterface) {
        detailedInfoContainer = new Container();
        detailedInfoContainer.add(createNetworkMainInfo(networkInterface));

        additionalInfoPanel.add(detailedInfoContainer);
    }

    protected IsWidget createNetworkMainInfo(VmNetworkInterface networkInterface) {
        VmGuestAgentInterface guestAgentInterface = findGuestAgentDataForInterface(networkInterface);

        Row row = new Row();
        row.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        row.addStyleName(NETWORK_DATA_ROW);
        row.add(createNetworkNameColumn(networkInterface));
        row.add(createIpv4Column(guestAgentInterface));
        row.add(createIpv6Column(guestAgentInterface));
        row.add(createMacColumn(networkInterface));

        return row;
    }

    protected IsWidget createNetworkNameColumn(VmNetworkInterface networkInterface) {
        Column column = new Column(ColumnSize.SM_3);
        column.addStyleName(VM_NIC_OVERFLOW);
        DListElement dl = Document.get().createDLElement();

        SafeHtml name =
                SafeHtmlUtils.fromString(networkInterface.getNetworkName() != null ? networkInterface.getNetworkName()
                        : constants.unAvailablePropertyLabel());
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()), name, dl);
        column.getElement().appendChild(dl);

        WidgetTooltip tooltip = new WidgetTooltip(column);
        tooltip.setHtml(name);
        return tooltip;
    }

    protected IsWidget createIpv4Column(VmGuestAgentInterface guestAgentInterface) {
        Column column = new Column(ColumnSize.SM_4);
        column.addStyleName(VM_NIC_OVERFLOW);
        DListElement dl = Document.get().createDLElement();

        StringJoiner ipv4AddressJoiner = new StringJoiner(COMMA_DELIMITER);
        if (guestAgentInterface.getIpv4Addresses() != null) {
            ipv4AddressJoiner.add(String.join(COMMA_DELIMITER, guestAgentInterface.getIpv4Addresses()));
        }
        String ipv4Address = ipv4AddressJoiner.toString();
        if (ipv4Address.isEmpty()) {
            ipv4Address = constants.notAvailableLabel();
        }

        SafeHtml ipv4 = SafeHtmlUtils.fromString(ipv4Address);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv4VmGuestAgent()), ipv4, dl);
        column.getElement().appendChild(dl);

        WidgetTooltip tooltip = new WidgetTooltip(column);
        tooltip.setHtml(ipv4);
        return tooltip;
    }

    protected IsWidget createIpv6Column(VmGuestAgentInterface guestAgentInterface) {
        Column column = new Column(ColumnSize.SM_4);
        column.addStyleName(VM_NIC_OVERFLOW);
        DListElement dl = Document.get().createDLElement();

        StringJoiner ipv6AddressJoiner = new StringJoiner(COMMA_DELIMITER);
        if (guestAgentInterface.getIpv6Addresses() != null) {
            ipv6AddressJoiner.add(
                    String.join(COMMA_DELIMITER, guestAgentInterface.getIpv6Addresses()));
        }
        String ipv6Address = ipv6AddressJoiner.toString();
        if (ipv6Address.isEmpty()) {
            ipv6Address = constants.notAvailableLabel();
        }

        SafeHtml ipv6 = SafeHtmlUtils.fromString(ipv6Address);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv6VmGuestAgent()), ipv6, dl);
        column.getElement().appendChild(dl);

        WidgetTooltip tooltip = new WidgetTooltip(column);
        tooltip.setHtml(ipv6);
        return tooltip;
    }

    protected IsWidget createMacColumn(VmNetworkInterface networkInterface) {
        Column column = new Column(ColumnSize.SM_4);
        column.addStyleName(VM_NIC_OVERFLOW);
        DListElement dl = Document.get().createDLElement();

        SafeHtml mac = SafeHtmlUtils.fromString(networkInterface.getMacAddress());
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macVmGuestAgent()), mac, dl);
        column.getElement().appendChild(dl);

        WidgetTooltip tooltip = new WidgetTooltip(column);
        tooltip.setHtml(mac);
        return tooltip;
    }

    private IsWidget createLinkStatusPanel(boolean isLinked) {
        IconStatusPanel iconStatusPanel = new IconStatusPanel(isLinked ? IconType.ARROW_CIRCLE_O_UP :
            IconType.ARROW_CIRCLE_O_DOWN);
        iconStatusPanel.addStyleName(DOUBLE_SIZE);
        iconStatusPanel.addStyleName(STATUS_ICON);
        iconStatusPanel.getElement().getStyle().setColor(isLinked ? GREEN : RED);

        String tooltipText = isLinked ? constants.linkedNetworkInterface() : constants.unlinkedNetworkInterface();
        WidgetTooltip tooltip = new WidgetTooltip(iconStatusPanel);
        tooltip.setHtml(SafeHtmlUtils.fromString(tooltipText));
        return tooltip;
    }

    private IsWidget createExpandIconPanel() {
        infoExpand = new ExpandableListViewItem(SafeHtmlUtils.EMPTY_SAFE_HTML);
        getClickHandlerRegistrations().add(infoExpand.addClickHandler(this));

        return infoExpand;
    }

    private IsWidget createCardPluggedStatusPanel(boolean isPlugged) {
        Span linkStatusPanel = new Span();
        Span icon = new Span();
        icon.addStyleName(Styles.ICON_STACK);
        Italic plugItalic = new Italic();
        plugItalic.addStyleName(Styles.FONT_AWESOME_BASE);
        plugItalic.addStyleName(Styles.ICON_STACK_TOP);
        plugItalic.addStyleName(ROTATE_270);
        plugItalic.addStyleName(IconType.PLUG.getCssName());
        icon.add(plugItalic);
        if (!isPlugged) {
            Italic unplugged = new Italic();
            unplugged.addStyleName(Styles.FONT_AWESOME_BASE);
            unplugged.addStyleName(Styles.ICON_STACK_TOP);
            unplugged.addStyleName(DANGER);
            unplugged.addStyleName(IconType.BAN.getCssName());
            icon.add(unplugged);
        }
        linkStatusPanel.add(icon);
        linkStatusPanel.addStyleName(DOUBLE_SIZE);
        linkStatusPanel.addStyleName(STATUS_ICON);

        String tooltipText = isPlugged ? constants.pluggedNetworkInterface() : constants.unpluggedNetworkInterface();
        WidgetTooltip tooltip = new WidgetTooltip(linkStatusPanel);
        tooltip.setHtml(SafeHtmlUtils.fromString(tooltipText));
        return tooltip;
    }

    @Override
    protected IsWidget createIcon() {
        return iconPanel;
    }

    @Override
    protected void toggleExpanded() {
        if (!infoExpand.isActive()) {
            removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    @Override
    protected void toggleExpanded(boolean value) {
        // No-op for now as we don't have an expand all option.
    }

    @Override
    protected void hideAllDetails() {
        infoExpand.toggleExpanded(false);
    }

    public boolean getInfoState() {
        return infoExpand.isActive();
    }

    public void setInfoExpanded(boolean value) {
        infoExpand.toggleExpanded(value);
        toggleExpanded();
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<VmNetworkInterface> originalViewItem) {
        VmInterfaceListGroupItem original = (VmInterfaceListGroupItem) originalViewItem;
        setInfoExpanded(original.getInfoState());
    }

    private VmGuestAgentInterface findGuestAgentDataForInterface(VmNetworkInterface networkInterface) {
        return allGuestAgentData.stream()
                .filter(guestAgentIface -> Objects.nonNull(guestAgentIface.getMacAddress()))
                .filter(guestAgentIface -> Objects.equals(guestAgentIface.getMacAddress(),
                        networkInterface.getMacAddress()))
                .findAny()
                .orElse(new VmGuestAgentInterface());
    }
}
