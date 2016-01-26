package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractNullableNumberColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;

public class VmInterfaceInfoPanel extends TabLayoutPanel {

    private final VmInterfaceListModel vmInterfaceListModel;

    private EntityModelCellTable<ListModel> statisticsTable;
    private EntityModelCellTable<ListModel> guestAgentDataTable;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public VmInterfaceInfoPanel(VmInterfaceListModel vmInterfaceListModel) {
        super(CommonApplicationTemplates.TAB_BAR_HEIGHT, Unit.PX);

        this.vmInterfaceListModel = vmInterfaceListModel;

        initPanel();
        addStyle();
    }

    private void initPanel() {

        // Initialize Tables
        initStatitsticsTable();
        initGuestAgentDataTable();

        // Add Tabs
        add(new ScrollPanel(statisticsTable), constants.statistics());
        add(new ScrollPanel(guestAgentDataTable), constants.guestAgentData());
    }

    public void updatePanel(VmNetworkInterface nic) {
        updateTabsData(nic);
    }

    private void addStyle() {
        getElement().getStyle().setPosition(Position.STATIC);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateTabsData(VmNetworkInterface nic) {
        statisticsTable.setRowData((List) Arrays.asList(nic));
        guestAgentDataTable.setRowData(vmInterfaceListModel.getSelectionGuestAgentData() != null ? (List) vmInterfaceListModel.getSelectionGuestAgentData()
                : new ArrayList<EntityModel>());
    }

    private void initStatitsticsTable() {
        statisticsTable = new EntityModelCellTable<>(false, true);
        statisticsTable.enableColumnResizing();

        AbstractTextColumn<VmNetworkInterface> rxColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getReceiveRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };

        statisticsTable.addColumn(rxColumn,
                templates.sub(constants.rxRate(), constants.mbps()), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> txColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getTransmitRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };

        statisticsTable.addColumn(txColumn,
                templates.sub(constants.txRate(), constants.mbps()), "100px"); //$NON-NLS-1$

        AbstractNullableNumberColumn<VmNetworkInterface> totalRxColumn = new AbstractNullableNumberColumn<VmNetworkInterface>() {
            @Override
            protected Number getRawValue(VmNetworkInterface object) {
                return object != null ? object.getStatistics().getReceivedBytes() : null;
            }
        };

        statisticsTable.addColumn(totalRxColumn, templates.sub(constants.rxTotal(), constants.bytes()), "150px"); //$NON-NLS-1$

        AbstractNullableNumberColumn<VmNetworkInterface> totalTxColumn = new AbstractNullableNumberColumn<VmNetworkInterface>() {
            @Override
            protected Number getRawValue(VmNetworkInterface object) {
                return object != null ? object.getStatistics().getTransmittedBytes() : null;
            }
        };

        statisticsTable.addColumn(totalTxColumn, templates.sub(constants.txTotal(), constants.bytes()), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> dropsColumn = new AbstractSumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                Double receiveDropRate = object != null ? object.getStatistics().getReceiveDropRate() : null;
                Double transmitDropRate = object != null ? object.getStatistics().getTransmitDropRate() : null;
                return new Double[] { receiveDropRate, transmitDropRate };
            }
        };
        statisticsTable.addColumn(dropsColumn,
                templates.sub(constants.dropsInterface(), constants.pkts()), "100px"); //$NON-NLS-1$

        statisticsTable.setRowData(new ArrayList<EntityModel>());
        statisticsTable.setWidth("100%", true); //$NON-NLS-1$
        statisticsTable.setSelectionModel(new NoSelectionModel());
    }

    private void initGuestAgentDataTable() {
        guestAgentDataTable = new EntityModelCellTable<>(false, true);
        guestAgentDataTable.enableColumnResizing();

        AbstractTextColumn<VmGuestAgentInterface> nameColumn = new AbstractTextColumn<VmGuestAgentInterface>() {

            @Override
            public String getValue(VmGuestAgentInterface object) {
                return object.getInterfaceName();
            }

        };
        guestAgentDataTable.addColumn(nameColumn,
                constants.nameVmGuestAgent(), "100px"); //$NON-NLS-1$

        AbstractSafeHtmlColumn<VmGuestAgentInterface> ipv4Column =
                new AbstractSafeHtmlColumn<VmGuestAgentInterface>() {
                    @Override
                    public SafeHtml getValue(VmGuestAgentInterface object) {

                        if (object.getIpv4Addresses() == null || object.getIpv4Addresses().size() == 0) {
                            return SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
                        }

                        if (object.getIpv4Addresses().size() == 1) {
                            return SafeHtmlUtils.fromTrustedString(object.getIpv4Addresses().get(0));
                        }

                        return SafeHtmlUtils.fromTrustedString(messages.addressesVmGuestAgent(object.getIpv4Addresses()
                                .size()));
                    }

                    @Override
                    public SafeHtml getTooltip(VmGuestAgentInterface object) {
                        return SafeHtmlUtils.fromTrustedString(StringUtils.join(object.getIpv4Addresses(), ", ")); //$NON-NLS-1$
                    }

                };
        guestAgentDataTable.addColumn(ipv4Column,
                constants.ipv4VmGuestAgent(), "100px"); //$NON-NLS-1$

        AbstractSafeHtmlColumn<VmGuestAgentInterface> ipv6Column =
                new AbstractSafeHtmlColumn<VmGuestAgentInterface>() {
                    @Override
                    public SafeHtml getValue(VmGuestAgentInterface object) {

                        if (object.getIpv6Addresses() == null || object.getIpv6Addresses().size() == 0) {
                            return SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
                        }

                        if (object.getIpv6Addresses().size() == 1) {
                            return SafeHtmlUtils.fromTrustedString(object.getIpv6Addresses().get(0));
                        }

                        return SafeHtmlUtils.fromTrustedString(messages.addressesVmGuestAgent(object.getIpv6Addresses()
                                .size()));
                    }

                    @Override
                    public SafeHtml getTooltip(VmGuestAgentInterface object) {
                        return SafeHtmlUtils.fromTrustedString(StringUtils.join(object.getIpv6Addresses(), ", ")); //$NON-NLS-1$
                    }
                };

        guestAgentDataTable.addColumn(ipv6Column,
                constants.ipv6VmGuestAgent(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmGuestAgentInterface> macColumn = new AbstractTextColumn<VmGuestAgentInterface>() {

            @Override
            public String getValue(VmGuestAgentInterface object) {
                return object.getMacAddress();
            }

        };
        guestAgentDataTable.addColumn(macColumn,
                constants.macVmGuestAgent(), "150px"); //$NON-NLS-1$

        guestAgentDataTable.setRowData(new ArrayList<EntityModel>());
        guestAgentDataTable.setWidth("100%", true); //$NON-NLS-1$
        guestAgentDataTable.setSelectionModel(new NoSelectionModel());
    }
}
