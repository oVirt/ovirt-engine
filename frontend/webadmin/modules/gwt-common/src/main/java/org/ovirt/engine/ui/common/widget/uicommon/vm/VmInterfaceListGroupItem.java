package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.List;
import java.util.StringJoiner;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

public class VmInterfaceListGroupItem extends PatternflyListViewItem<VmNetworkInterface> {

    private static final String COMMA_DELIMITER = ", "; // $NON-NLS-1$
    private static final String DANGER = "text-danger"; // $NON-NLS-1$
    private static final String ROTATE_270 = "fa-rotate-270"; //$NON-NLS-1$
    private static final String DL_HORIZONTAL = "dl-horizontal"; // $NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private ExpandableListViewItem generalExpand;
    private ExpandableListViewItem statisticsExpand;
    private ExpandableListViewItem guestAgentExpand;
    private ExpandableListViewItem networkFilterParameterExpand;
    private final List<VmGuestAgentInterface> allGuestAgentData;

    public VmInterfaceListGroupItem(VmNetworkInterface networkInterface, List<VmGuestAgentInterface> allGuestAgentData,
            List<VmNicFilterParameter> networkFilterParameters) {
        super(networkInterface.getName(), networkInterface);
        this.allGuestAgentData = allGuestAgentData;
        listGroupItem.addStyleName(PatternflyConstants.PF_LIST_VIEW_TOP_ALIGN);
        listGroupItem.addStyleName(PatternflyConstants.PF_LIST_VIEW_STACKED);
        Container generalInfoContainer = createGeneralItemContainerPanel(networkInterface);
        generalExpand.setDetails(generalInfoContainer);
        listGroupItem.add(generalInfoContainer);
        Container statisticsContainer = createStatisticsItemContainerPanel(networkInterface);
        statisticsExpand.setDetails(statisticsContainer);
        listGroupItem.add(statisticsContainer);
        Container guestAgentContainer = createGuestAgentContainerPanel(networkInterface, allGuestAgentData);
        guestAgentExpand.setDetails(guestAgentContainer);
        listGroupItem.add(guestAgentContainer);
        Container networkFilterParameterContainer =
                createNetworkFilterParametersContainerPanel(networkFilterParameters);
        networkFilterParameterExpand.setDetails(networkFilterParameterContainer);
        listGroupItem.add(networkFilterParameterContainer);
        displayImportantNicInfo(networkInterface);
    }

    private Container createNetworkFilterParametersContainerPanel(List<VmNicFilterParameter> networkFilterParameters) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);

        networkFilterParameters.stream().forEach(parameter -> {
            DListElement dl = Document.get().createDLElement();
            dl.addClassName(DL_HORIZONTAL);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.nameNetworkFilterParameter()),
                    parameter.getName(),
                    dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.valueNetworkFilterParameter()),
                    parameter.getValue(),
                    dl);
            column.getElement().appendChild(dl);
        });

        return createItemContainerPanel(content);
    }

    private Container createGeneralItemContainerPanel(VmNetworkInterface networkInterface) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.plugged()),
                renderPlugged(networkInterface.isPlugged()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()),
                networkInterface.getNetworkName(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.profileNameInterface()),
                networkInterface.getVnicProfileName(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.vmNetworkQosName()),
                StringHelper.isNullOrEmpty(networkInterface.getQosName())
                    ? constants.notAvailableLabel() : networkInterface.getQosName(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.linkStateNetworkInterface()),
                renderLinkState(networkInterface.isLinked()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeInterface()),
                VmInterfaceType.forValue(networkInterface.getType()).getDescription(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()), networkInterface.getMacAddress(), dl);
        addDetailItem(templates.sub(constants.speedInterface(), constants.mbps()),
                String.valueOf(networkInterface.getSpeed()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.portMirroring()),
                renderPortMirroring(networkInterface.isPortMirroring()), dl);
        column.getElement().appendChild(dl);
        return createItemContainerPanel(content);
    }

    private String renderPortMirroring(boolean portMirroring) {
        if (portMirroring) {
            return constants.portMirroringEnabled();
        } else {
            return constants.portMirroringDisabled();
        }
    }

    private String renderLinkState(boolean linkState) {
        if (linkState) {
            return constants.up();
        } else {
            return constants.down();
        }
    }

    private String renderPlugged(boolean pluggedStatus) {
        if (pluggedStatus) {
            return constants.plugged();
        } else {
            return constants.unplugged();
        }
    }

    private Container createStatisticsItemContainerPanel(VmNetworkInterface networkInterface) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);
        RxTxRateRenderer rateRenderer = new RxTxRateRenderer();
        RxTxTotalRenderer totalRenderer = new RxTxTotalRenderer();

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                rateRenderer.render(new Double[] { networkInterface.getStatistics().getReceiveRate(),
                        networkInterface.hasSpeed() ? networkInterface.getSpeed().doubleValue() : 0}), dl);

        addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                rateRenderer.render(new Double[] { networkInterface.getStatistics().getTransmitRate(),
                        networkInterface.hasSpeed() ? networkInterface.getSpeed().doubleValue() : 0}), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.rxTotal()),
                totalRenderer.render(networkInterface.getStatistics().getReceivedBytes()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.txTotal()),
                totalRenderer.render(networkInterface.getStatistics().getTransmittedBytes()), dl);
        addDetailItem(templates.sub(constants.dropsInterface(), constants.pkts()),
                String.valueOf(networkInterface.getStatistics().getReceiveDropRate()
                        + networkInterface.getStatistics().getTransmitDropRate()), dl);
        column.getElement().appendChild(dl);
        return createItemContainerPanel(content);
    }

    private Container createGuestAgentContainerPanel(VmNetworkInterface networkInterface,
            List<VmGuestAgentInterface> allGuestAgentData) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);
        for (VmGuestAgentInterface guestAgentInterface: allGuestAgentData) {
            if (guestAgentInterface.getMacAddress() != null
                    && guestAgentInterface.getMacAddress().equals(networkInterface.getMacAddress())) {
                DListElement dl = Document.get().createDLElement();
                dl.addClassName(DL_HORIZONTAL);
                addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.nameVmGuestAgent()),
                        guestAgentInterface.getInterfaceName(), dl);
                addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv4VmGuestAgent()),
                        guestAgentInterface.getIpv4Addresses() != null ?
                        String.join(COMMA_DELIMITER, guestAgentInterface.getIpv4Addresses())
                        : constants.notAvailableLabel(), dl);
                addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv6VmGuestAgent()),
                        guestAgentInterface.getIpv6Addresses() != null ?
                        String.join(COMMA_DELIMITER, guestAgentInterface.getIpv6Addresses())
                        : constants.notAvailableLabel(), dl);
                addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macVmGuestAgent()),
                        guestAgentInterface.getMacAddress(), dl);
                column.getElement().appendChild(dl);
            }
        }
        if (allGuestAgentData.isEmpty()) {
            Span noDataSpan = new Span();
            noDataSpan.setText(constants.notAvailableLabel());
            column.add(noDataSpan);
        }
        return createItemContainerPanel(content);
    }

    private void createAdditionalInfoPanel() {
        additionalInfoPanel.add(createGeneralAdditionalInfo());
        additionalInfoPanel.add(createStatisticsAdditionalInfo());
        additionalInfoPanel.add(createGuestAgentAdditionalInfo());
        additionalInfoPanel.add(createNetworkFilterParameterAdditionalInfo());
    }

    private IsWidget createNetworkFilterParameterAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        networkFilterParameterExpand = new ExpandableListViewItem(constants.networkFilterParametersLabel());
        getClickHandlerRegistrations().add(networkFilterParameterExpand.addClickHandler(this));
        panel.add(networkFilterParameterExpand);
        return panel;
    }

    private IsWidget createGeneralAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        generalExpand = new ExpandableListViewItem(constants.generalLabel());
        getClickHandlerRegistrations().add(generalExpand.addClickHandler(this));
        panel.add(generalExpand);
        return panel;
    }

    private IsWidget createStatisticsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        statisticsExpand = new ExpandableListViewItem(constants.statistics());
        getClickHandlerRegistrations().add(statisticsExpand.addClickHandler(this));
        panel.add(statisticsExpand);
        return panel;
    }

    private IsWidget createGuestAgentAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        guestAgentExpand = new ExpandableListViewItem(constants.guestAgentData());
        getClickHandlerRegistrations().add(guestAgentExpand.addClickHandler(this));
        panel.add(guestAgentExpand);
        return panel;
    }

    private boolean isInterfaceUp(VmNetworkInterface networkInterface) {
        return networkInterface != null ? networkInterface.isLinked() : false;
    }

    private boolean isCardPlugged(VmNetworkInterface networkInterface) {
        return networkInterface != null ? networkInterface.isPlugged() : false;
    }

    @Override
    protected IsWidget createBodyPanel(String header, VmNetworkInterface networkInterface) {
        checkBoxPanel.add(createLinkStatusPanel(isInterfaceUp(networkInterface)));
        checkBoxPanel.add(createCardPluggedStatusPanel(isCardPlugged(networkInterface)));
        descriptionHeaderPanel.getElement().setInnerText(header);
        createAdditionalInfoPanel();
        return bodyPanel;
    }

    protected void displayImportantNicInfo(VmNetworkInterface networkInterface) {
        addNetworkMainInfo(networkInterface, statusPanel);
    }

    protected void addNetworkMainInfo(VmNetworkInterface networkInterface, HasWidgets targetPanel) {
        DListElement dl = Document.get().createDLElement();
        FlowPanel infoPanel = new FlowPanel();
        StringJoiner ipv4AddressJoiner = new StringJoiner(COMMA_DELIMITER);
        StringJoiner ipv6AddressJoiner = new StringJoiner(COMMA_DELIMITER);
        for (VmGuestAgentInterface guestAgentInterface: allGuestAgentData) {
            if (guestAgentInterface.getMacAddress() != null
                    && networkInterface.getMacAddress().equals(guestAgentInterface.getMacAddress())) {
                if (guestAgentInterface.getIpv4Addresses() != null) {
                    ipv4AddressJoiner.add(
                            String.join(COMMA_DELIMITER, guestAgentInterface.getIpv4Addresses()));
                }
                if (guestAgentInterface.getIpv6Addresses() != null) {
                    ipv6AddressJoiner.add(
                            String.join(COMMA_DELIMITER, guestAgentInterface.getIpv6Addresses()));
                }
            }
        }
        String ipv4Address = ipv4AddressJoiner.toString();
        if (ipv4Address.isEmpty()) {
            ipv4Address = constants.notAvailableLabel();
        }
        String ipv6Address = ipv6AddressJoiner.toString();
        if (ipv6Address.isEmpty()) {
            ipv6Address = constants.notAvailableLabel();
        }
        addStackedDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()),
            networkInterface.getNetworkName() != null ? networkInterface.getNetworkName()
                    : constants.unAvailablePropertyLabel(), dl);
        addStackedDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv4VmGuestAgent()), ipv4Address, dl);
        addStackedDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv6VmGuestAgent()), ipv6Address, dl);
        addStackedDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macVmGuestAgent()),
                networkInterface.getMacAddress(), dl);
        infoPanel.getElement().appendChild(dl);
        targetPanel.add(infoPanel);
    }

    private IsWidget createLinkStatusPanel(boolean isLinked) {
        IconStatusPanel iconStatusPanel = new IconStatusPanel(isLinked ? IconType.ARROW_CIRCLE_O_UP :
            IconType.ARROW_CIRCLE_O_DOWN);
        iconStatusPanel.addStyleName(DOUBLE_SIZE);
        iconStatusPanel.getElement().getStyle().setColor(isLinked ? GREEN : RED);

        String tooltipText = isLinked ? constants.linkedNetworkInterface() : constants.unlinkedNetworkInterface();
        WidgetTooltip tooltip = new WidgetTooltip(iconStatusPanel);
        tooltip.setHtml(SafeHtmlUtils.fromString(tooltipText));
        return tooltip;
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

        String tooltipText = isPlugged ? constants.pluggedNetworkInterface() : constants.unpluggedNetworkInterface();
        WidgetTooltip tooltip = new WidgetTooltip(linkStatusPanel);
        tooltip.setHtml(SafeHtmlUtils.fromString(tooltipText));
        return tooltip;
    }

    @Override
    protected IsWidget createIcon() {
        iconPanel.add(new NetworkIcon());
        return iconPanel;
    }

    @Override
    protected void toggleExpanded() {
        if (!generalExpand.isActive() && !statisticsExpand.isActive()
                && !guestAgentExpand.isActive() && !networkFilterParameterExpand.isActive()) {
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
        generalExpand.toggleExpanded(false);
        statisticsExpand.toggleExpanded(false);
        guestAgentExpand.toggleExpanded(false);
        networkFilterParameterExpand.toggleExpanded(false);
    }

    public boolean getGeneralState() {
        return generalExpand.isActive();
    }

    public void setGeneralExpanded(boolean value) {
        generalExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getStatisticsState() {
        return statisticsExpand.isActive();
    }

    public void setStatisticsExpanded(boolean value) {
        statisticsExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getGuestAgentState() {
        return guestAgentExpand.isActive();
    }

    public void setGuestAgentExpanded(boolean value) {
        guestAgentExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getNetworkFilterParametersState() {
        return networkFilterParameterExpand.isActive();
    }

    public void setNetworkFilterParametersExpanded(boolean value) {
        networkFilterParameterExpand.toggleExpanded(value);
        toggleExpanded();
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<VmNetworkInterface> originalViewItem) {
        VmInterfaceListGroupItem original = (VmInterfaceListGroupItem) originalViewItem;
        setGeneralExpanded(original.getGeneralState());
        setStatisticsExpanded(original.getStatisticsState());
        setGuestAgentExpanded(original.getGuestAgentState());
        setNetworkFilterParametersExpanded(original.getNetworkFilterParametersState());
    }
}
