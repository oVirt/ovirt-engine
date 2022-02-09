package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.ui.common.CellTablePopupTableResources;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.network.BondedNetworkIcon;
import org.ovirt.engine.ui.common.widget.uicommon.vm.IconStatusPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;

public class HostNetworkInterfaceBondedListViewItem extends HostNetworkInterfaceListViewItem {

    private static final String INVALID_AD_PARTNER_MAC = "00:00:00:00:00:00";//$NON-NLS-1$
    private static final int MAX_SLAVES = 1000;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    private ExpandableListViewItem slavesExpand;
    private Container slavesContainer;

    public HostNetworkInterfaceBondedListViewItem(HostInterfaceLineModel entity) {
        // Since this is a bond entity.getInterface() will not be null.
        super(entity.getInterface().getName(), entity);
        expansionLinkContainer.add(createSlavesAdditionalInfo());
        checkBoxPanel.clear();
        checkBoxPanel.add(createBondInterfaceStatusPanel(isInterfaceUp()));
        slavesContainer = createSlavesContainer();
        slavesExpand.setDetails(slavesContainer);
        listGroupItem.add(slavesContainer);
    }

    @Override
    protected void cycleExpanded() {
        super.cycleExpanded();
        setSlavesExpanded(!getLogicalNetworkState());
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<HostInterfaceLineModel> originalViewItem) {
        super.restoreStateFromViewItem(originalViewItem);
        // Need to check because we could be coming from a non bond interface and restoring the state (expanded details)
        // from it into a bonded interface will not work.
        if (originalViewItem instanceof HostNetworkInterfaceBondedListViewItem) {
            setSlavesExpanded(((HostNetworkInterfaceBondedListViewItem) originalViewItem).getSlavesState());
        }
    }

    private IsWidget createSlavesAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        List<HostInterface> slaves = getEntity().getInterfaces();
        slavesExpand = new ExpandableListViewItem(SafeHtmlUtils.fromString(messages.slaves(slaves.size())));
        getClickHandlerRegistrations().add(slavesExpand.addClickHandler(this));
        panel.add(slavesExpand);
        return panel;
    }

    private Container createSlavesContainer() {
        Row content = new Row();
        Column gridColumn = new Column(ColumnSize.SM_12);
        content.add(gridColumn);
        Container container = createItemContainerPanel(content);

        CellTable<HostInterface> slavesTable = new CellTable<>(MAX_SLAVES,
                (Resources)GWT.create(CellTablePopupTableResources.class));

        slavesTable.getElement().addClassName(Styles.TABLE);
        slavesTable.getElement().addClassName(PatternflyConstants.PF_TABLE_STRIPED);
        slavesTable.getElement().addClassName(PatternflyConstants.PF_TABLE_BORDERED);
        ListDataProvider<HostInterface> logicalNetworkDataProvider = new ListDataProvider<>();
        logicalNetworkDataProvider.addDataDisplay(slavesTable);
        logicalNetworkDataProvider.setList(getEntity().getInterfaces());

        //Setup columns
        TextColumn<HostInterface> name = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return hostInterface.getName();
            }
        };
        slavesTable.addColumn(name, constants.nameInterface());

        TextColumn<HostInterface> macAddress = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return hostInterface.getInterface().getMacAddress() != null ?
                        hostInterface.getInterface().getMacAddress() : constants.unAvailablePropertyLabel();
            }
        };
        slavesTable.addColumn(macAddress, constants.macInterface());

        TextColumn<HostInterface> speed = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return hostInterface.getInterface().hasSpeed() ? String.valueOf(hostInterface.getInterface().getSpeed())
                        : constants.unAvailablePropertyLabel();
            }
        };
        slavesTable.addColumn(speed, constants.speedInterface());

        TextColumn<HostInterface> rxRate = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return rateRenderer.render(new Double[] { hostInterface.getRxRate(),
                    hostInterface.getSpeed().doubleValue() });
            }
        };
        slavesTable.addColumn(rxRate, templates.sub(constants.rxRate(), constants.mbps()));

        TextColumn<HostInterface> txRate = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return rateRenderer.render(new Double[] { hostInterface.getTxRate(),
                    hostInterface.getSpeed().doubleValue() });
            }
        };
        slavesTable.addColumn(txRate, templates.sub(constants.txRate(), constants.mbps()));

        TextColumn<HostInterface> rxTotal = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return totalRenderer.render(hostInterface.getRxTotal());
            }
        };
        slavesTable.addColumn(rxTotal, templates.sub(constants.rxTotal(), constants.bytes()));

        TextColumn<HostInterface> txTotal = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return totalRenderer.render(hostInterface.getTxTotal());
            }
        };
        slavesTable.addColumn(txTotal, templates.sub(constants.txTotal(), constants.bytes()));

        TextColumn<HostInterface> dropRate = new TextColumn<HostInterface>() {
            @Override
            public String getValue(HostInterface hostInterface) {
                return String.valueOf(hostInterface.getRxDrop().add(hostInterface.getTxDrop()));
            }
        };
        slavesTable.addColumn(dropRate, templates.sub(constants.dropsInterface(), constants.pkts()));

        gridColumn.add(slavesTable);
        return container;
    }

    @Override
    protected boolean isInterfaceUp() {
        return getEntity().getInterface().getStatistics().getStatus() == InterfaceStatus.UP;
    }

    @Override
    protected IsWidget createIcon() {
        InterfaceStatus interfaceStatus = getEntity().getInterface().getStatistics().getStatus();
        SafeHtml bondPropertiesMessage = createBondTooltipMessage(getEntity(), interfaceStatus);
        SafeHtml bondMessage = templates.italicWordWrapMaxWidthWithBoldTitle(constants.bondProperties(), bondPropertiesMessage);
        WidgetTooltip iconTooltip = new WidgetTooltip(new BondedNetworkIcon());
        iconTooltip.setHtml(bondMessage);
        iconPanel.add(iconTooltip);
        return iconPanel;
    }

    protected IsWidget createBondInterfaceStatusPanel(boolean isUp) {
        Bond bond = (Bond) getEntity().getInterface();
        boolean isAdPartnerMacValid = isAdPartnerMacValid(bond);
        boolean isAdAggregatorIdValid = isAdAggregatorIdValid(bond, getEntity());
        BondMode bondMode = BondMode.parseBondMode(bond.getBondOptions());
        if (BondMode.BOND4.equals(bondMode) && (!isAdPartnerMacValid || !isAdAggregatorIdValid)) {
            IconStatusPanel iconStatusPanel = new IconStatusPanel(PatternflyConstants.PFICON_WARNING_TRIANGLE_O,
                    PatternflyConstants.PFICON);
            iconStatusPanel.addStyleName(DOUBLE_SIZE);
            WidgetTooltip tooltip = new WidgetTooltip(iconStatusPanel);
            StringBuffer message = new StringBuffer();
            if (!isAdPartnerMacValid) {
                message.append(constants.bondInMode4HasNoPartnerMac());
            }
            if (!isAdPartnerMacValid && !isAdAggregatorIdValid) {
                message.append(" ");//$NON-NLS-1$
            }
            if (!isAdAggregatorIdValid) {
                message.append(constants.bondInMode4HasInvalidAggregatorId());
            }
            tooltip.setHtml(templates.italicWordWrapMaxWidth(message.toString()));
            return tooltip;
        } else {
            return super.createInterfaceStatusPanel(isUp);
        }
    }

    @Override
    protected void hideAllDetails() {
        super.hideAllDetails();
        slavesExpand.toggleExpanded(false);
    }

    @Override
    protected void toggleExpanded() {
        if (!slavesExpand.isActive() && !getLogicalNetworkState()) {
            listGroupItem.removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            listGroupItem.addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    public boolean getSlavesState() {
        return slavesExpand.isActive();
    }

    public void setSlavesExpanded(boolean value) {
        slavesExpand.toggleExpanded(value);
        toggleExpanded();
    }

    private SafeHtml createBondTooltipMessage(HostInterfaceLineModel lineModel, InterfaceStatus interfaceStatus) {
        Bond bond = (Bond) lineModel.getInterface();
        StringBuilder bondProperties = new StringBuilder(messages.bondStatus(interfaceStatus.toString()));
        boolean isBond4 = BondMode.BOND4.equals(BondMode.parseBondMode(bond.getBondOptions()));

        if (InterfaceStatus.UP.equals(interfaceStatus) && isBond4) {
            bondProperties.append("\n").append(createActiveBondTooltipMessage(bond, lineModel));//$NON-NLS-1$
        }
        if ((bond.getActiveSlave() != null) && (bond.getActiveSlave().length() > 0)) {
            bondProperties.append("\n").append(messages.bondActiveSlave(bond.getActiveSlave()));//$NON-NLS-1$
        }
        return new SafeHtmlBuilder().appendEscapedLines(bondProperties.toString()).toSafeHtml();
    }

    private String createActiveBondTooltipMessage(Bond bond, HostInterfaceLineModel lineModel) {
        List<String> bondProperties = new ArrayList<>();
        String adPartnerMac = Objects.toString(bond.getAdPartnerMac(), "");
        bondProperties.add(messages.bondAdPartnerMac(adPartnerMac));
        String adAggregatorId = Objects.toString(bond.getAdAggregatorId(), "");
        bondProperties.add(messages.bondAdAggregatorId(adAggregatorId));

        for (HostInterface nic : lineModel.getInterfaces()) {
            String nicName = nic.getName();
            String nicAggregatorId = Objects.toString(nic.getInterface().getAdAggregatorId(), "");
            bondProperties.add(messages.bondSlaveAdAggregatorId(nicName, nicAggregatorId));
        }
        return String.join("\n", bondProperties);//$NON-NLS-1$
    }

    private boolean isAdPartnerMacValid(Bond bond){
        String partnerMac = bond.getAdPartnerMac();
        boolean isAdPartnerMacEmpty = partnerMac == null || partnerMac.isEmpty() || partnerMac.equals(INVALID_AD_PARTNER_MAC);
        boolean isIfcUp = InterfaceStatus.UP.equals(bond.getStatistics().getStatus());
        boolean isBond4 = BondMode.BOND4.equals(BondMode.parseBondMode(bond.getBondOptions()));

        return !isAdPartnerMacEmpty || !isIfcUp || !isBond4;
    }

    private boolean isAdAggregatorIdValid(Bond bond, HostInterfaceLineModel lineModel) {
        Integer aggregatorId = bond.getAdAggregatorId();
        if (aggregatorId == null) {
            return false;
        }
        for (HostInterface nic : lineModel.getInterfaces()) {
            if (!aggregatorId.equals(nic.getInterface().getAdAggregatorId())) {
                return false;
            }
        }
        return true;
    }

}
