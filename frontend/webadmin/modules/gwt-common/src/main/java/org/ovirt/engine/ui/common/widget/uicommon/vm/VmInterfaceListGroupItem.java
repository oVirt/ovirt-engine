package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.List;

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
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class VmInterfaceListGroupItem extends PatternflyListViewItem<VmNetworkInterface> {

    private static final String DANGER = "text-danger"; // $NON-NLS-1$
    private static final String FA_2X = "fa-2x"; // $NON-NLS-1$
    private static final String ROTATE_270 = "fa-rotate-270"; //$NON-NLS-1$
    private static final String DL_HORIZONTAL = "dl-horizontal"; // $NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private ExpandableListViewItem generalExpand;
    private ExpandableListViewItem statisticsExpand;
    private ExpandableListViewItem guestAgentExpand;

    public VmInterfaceListGroupItem(VmNetworkInterface networkInterface, List<VmGuestAgentInterface> allGuestAgentData) {
        super(networkInterface.getName(), networkInterface);
        Container generalInfoContainer = createGeneralItemContainerPanel(networkInterface);
        generalExpand.setDetails(generalInfoContainer);
        add(generalInfoContainer);
        Container statisticsContainer = createStatisticsItemContainerPanel(networkInterface);
        statisticsExpand.setDetails(statisticsContainer);
        add(statisticsContainer);
        Container guestAgentContainer = createGuestAgentContainerPanel(networkInterface, allGuestAgentData);
        guestAgentExpand.setDetails(guestAgentContainer);
        add(guestAgentContainer);
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
                StringUtils.isEmpty(networkInterface.getQosName())
                    ? constants.notAvailableLabel() : networkInterface.getQosName(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.linkStateNetworkInterface()),
                renderLinkState(networkInterface.isLinked()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeInterface()),
                VmInterfaceType.forValue(networkInterface.getType()).getDescription(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()), networkInterface.getMacAddress(), dl);
        addDetailItem(templates.sub(constants.speedInterface(), constants.mbps()),
                String.valueOf(networkInterface.getSpeed()), dl);
        column.getElement().appendChild(dl);
        return createItemContainerPanel(content);
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

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                rateRenderer.render(new Double[] { networkInterface.getStatistics().getReceiveRate(),
                        networkInterface.getSpeed().doubleValue() }), dl);
        addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                rateRenderer.render(new Double[] { networkInterface.getStatistics().getTransmitRate(),
                        networkInterface.getSpeed().doubleValue() }), dl);
        addDetailItem(templates.sub(constants.rxTotal(), constants.mbps()),
                networkInterface.getStatistics().getReceivedBytes() != null ?
                        String.valueOf(networkInterface.getStatistics().getReceivedBytes()) :
                            constants.notAvailableLabel(), dl);
        addDetailItem(templates.sub(constants.txTotal(), constants.mbps()),
                networkInterface.getStatistics().getTransmittedBytes() != null ?
                        String.valueOf(networkInterface.getStatistics().getTransmittedBytes()) :
                            constants.notAvailableLabel(), dl);
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
                        String.join(", ", guestAgentInterface.getIpv4Addresses()) : constants.notAvailableLabel(), dl); // $NON-NLS-1$
                addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.ipv6VmGuestAgent()),
                        guestAgentInterface.getIpv6Addresses() != null ?
                        String.join(", ", guestAgentInterface.getIpv6Addresses()) : constants.notAvailableLabel(), dl); // $NON-NLS-1$
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

    private IsWidget createAdditionalInfoPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO);
        panel.add(createGeneralAdditionalInfo());
        panel.add(createStatisticsAdditionalInfo());
        panel.add(createGuestAgentAdditionalInfo());
        return panel;
    }

    private IsWidget createGeneralAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        generalExpand = new ExpandableListViewItem(constants.generalLabel(), IconType.EYE.getCssName());
        getClickHandlerRegistrations().add(generalExpand.addClickHandler(this));
        panel.add(generalExpand);
        return panel;
    }

    private IsWidget createStatisticsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        statisticsExpand = new ExpandableListViewItem(constants.statistics(), IconType.BAR_CHART.getCssName());
        getClickHandlerRegistrations().add(statisticsExpand.addClickHandler(this));
        panel.add(statisticsExpand);
        return panel;
    }

    private IsWidget createGuestAgentAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        guestAgentExpand = new ExpandableListViewItem(constants.guestAgentData(), IconType.HEART.getCssName());
        getClickHandlerRegistrations().add(guestAgentExpand.addClickHandler(this));
        panel.add(guestAgentExpand);
        return panel;
    }

    @Override
    protected IsWidget createBodyPanel(String header, VmNetworkInterface networkInterface) {
        FlowPanel bodyPanel = new FlowPanel();
        bodyPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_BODY);
        FlowPanel descriptionPanel = new FlowPanel();
        descriptionPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_DESCRIPTION);
        FlowPanel headerPanel = new FlowPanel();
        headerPanel.getElement().setInnerHTML(header);
        headerPanel.addStyleName(Styles.LIST_GROUP_ITEM_HEADING);
        descriptionPanel.add(headerPanel);
        FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName(Styles.LIST_GROUP_ITEM_TEXT);
        statusPanel.add(createLinkStatusPanel(networkInterface != null ? networkInterface.isLinked() : false));
        statusPanel.add(createCardPluggedStatusPanel(networkInterface != null ? networkInterface.isPlugged() : false));
        descriptionPanel.add(statusPanel);
        bodyPanel.add(descriptionPanel);
        bodyPanel.add(createAdditionalInfoPanel());
        return bodyPanel;
    }

    private IsWidget createLinkStatusPanel(boolean isLinked) {
        Span linkStatusPanel = new Span();
        Span icon = new Span();
        icon.addStyleName(Styles.ICON_STACK);
        icon.addStyleName(FA_2X);
        Italic plugItalic = new Italic();
        plugItalic.addStyleName(Styles.FONT_AWESOME_BASE);
        plugItalic.addStyleName(Styles.ICON_STACK_TOP);
        icon.add(plugItalic);
        if (isLinked) {
            plugItalic.addStyleName(IconType.ARROW_CIRCLE_O_UP.getCssName());
        } else {
            plugItalic.addStyleName(IconType.ARROW_CIRCLE_O_DOWN.getCssName());
        }
        linkStatusPanel.add(icon);
        return linkStatusPanel;
    }

    private IsWidget createCardPluggedStatusPanel(boolean isPlugged) {
        Span linkStatusPanel = new Span();
        Span icon = new Span();
        icon.addStyleName(Styles.ICON_STACK);
        icon.addStyleName(FA_2X);
        Italic plugItalic = new Italic();
        plugItalic.addStyleName(Styles.FONT_AWESOME_BASE);
        plugItalic.addStyleName(Styles.ICON_STACK_TOP);
        plugItalic.addStyleName(ROTATE_270);
        plugItalic.addStyleName(IconType.PLUG.getCssName());
        icon.add(plugItalic);
        if (!isPlugged) {
            Italic unplugged = new Italic();
            unplugged.addStyleName(Styles.FONT_AWESOME_BASE);
            unplugged.addStyleName(Styles.ICON_STACK_BASE);
            unplugged.addStyleName(DANGER);
            unplugged.addStyleName(IconType.BAN.getCssName());
            icon.add(unplugged);
        }
        linkStatusPanel.add(icon);
        return linkStatusPanel;
    }

    @Override
    protected IsWidget createIconPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_LEFT);
        Span iconSpan = new Span();
        iconSpan.addStyleName(PatternflyConstants.PFICON);
        iconSpan.addStyleName(PatternflyConstants.PFICON_NETWORK);
        iconSpan.addStyleName(PatternflyConstants.PF_LIST_VIEW_ICON_SM);
        panel.add(iconSpan);
        return panel;
    }

    @Override
    protected void toggleExpanded() {
        if (!generalExpand.isActive() && !statisticsExpand.isActive() && !guestAgentExpand.isActive()) {
            removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    @Override
    protected void hideAllDetails() {
        generalExpand.toggleExpanded(false);
        statisticsExpand.toggleExpanded(false);
        guestAgentExpand.toggleExpanded(false);
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

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<VmNetworkInterface> originalViewItem) {
        VmInterfaceListGroupItem original = (VmInterfaceListGroupItem) originalViewItem;
        setGeneralExpanded(original.getGeneralState());
        setStatisticsExpanded(original.getStatisticsState());
        setGuestAgentExpanded(original.getGuestAgentState());
    }
}
