package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.businessentities.network.TlvSpecificType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InterfacePropertiesAccessor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class ItemInfoPopup extends DecoratedPopupPanel {

    private final FlexTable contents = new FlexTable();
    private static final EnumRenderer<Ipv4BootProtocol> IPV4_RENDERER = new EnumRenderer<>();
    private static final EnumRenderer<Ipv6BootProtocol> IPV6_RENDERER = new EnumRenderer<>();

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final String BACKGROUND_COLOR = "#333333";//$NON-NLS-1$
    private static final String WHITE_TEXT_COLOR = "white";//$NON-NLS-1$
    private static final String TEXT_COLOR = "#c4c4c4";//$NON-NLS-1$
    private static final String BOND_OPTIONS_IP_SEPARATOR = ","; //$NON-NLS-1$

    private SafeHtml mgmtNetworkImage = safeHtmlFromTrustedString(resources.mgmtNetwork());
    private SafeHtml vmImage = safeHtmlFromTrustedString(resources.networkVm());
    private SafeHtml monitorImage = safeHtmlFromTrustedString(resources.networkMonitor());
    private SafeHtml migrationImage = safeHtmlFromTrustedString(resources.migrationNetwork());
    private SafeHtml glusterNwImage = safeHtmlFromTrustedString(resources.glusterNetwork());
    private SafeHtml unknownImage = safeHtmlFromTrustedString(resources.questionMarkImage());
    private SafeHtml notInSyncImage = safeHtmlFromTrustedString(resources.networkNotSyncImage());
    private SafeHtml alertImage = safeHtmlFromTrustedString(resources.alertImage());
    private SafeHtml defaultRouteImage = safeHtmlFromTrustedString(resources.defaultRouteNetwork());


    private SafeHtml safeHtmlFromTrustedString(ImageResource resource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resource).getHTML());
    }

    private static int defaultMtu = (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(
            ConfigValues.DefaultMTU);

    public ItemInfoPopup(NetworkPanelsStyle style) {
        super(true);
        contents.setCellPadding(5);
        contents.getElement().setClassName(style.fixedTable());
        setWidget(contents);
    }

    public final SafeHtml getTooltipContent(NetworkItemModel<?> item) {
        if (item instanceof LogicalNetworkModel) {
            showNetwork((LogicalNetworkModel) item);
        } else if (item instanceof NetworkInterfaceModel) {
            showNic((NetworkInterfaceModel) item);
        } else if (item instanceof NetworkLabelModel){
            return null;
        }

        return SafeHtmlUtils.fromTrustedString(this.getElement().getInnerHTML());

    }

    private void addRow(String label, String value) {
        int rowCount = contents.insertRow(contents.getRowCount());
        contents.setText(rowCount, 0, label + ": " + value); //$NON-NLS-1$
    }

    private void addRow(SafeHtml value) {
        int rowCount = contents.insertRow(contents.getRowCount());
        contents.setHTML(rowCount, 0, value);
    }

    private void showNetwork(LogicalNetworkModel networkModel) {
        boolean descriptionSectionHasValues = false;

        contents.removeAllRows();
        Network entity = networkModel.getNetwork();
        addRow(templates.titleSetupNetworkTooltip(networkModel.getName(),
                SafeStylesUtils.forTrustedBackgroundColor(BACKGROUND_COLOR)));

        // Not managed
        if (!networkModel.isManaged()) {
            addRow(templates.imageWithText(unknownImage, constants.unmanagedNetworkItemInfo()));
            addRow(SafeHtmlUtils.fromString(constants.unmanagedNetworkDescriptionItemInfo()));
            descriptionSectionHasValues = true;
        } else {
            if (networkModel.getErrorMessage() != null) {
                addRow(templates.imageTextSetupNetwork(alertImage, templates.maxWidthNteworkItemPopup(networkModel.getErrorMessage())));
                descriptionSectionHasValues = true;
            }
            // Description
            if (entity.getDescription() != null && !entity.getDescription().trim().equals("")) { //$NON-NLS-1$
                addRow(SafeHtmlUtils.fromString(entity.getDescription()));
                descriptionSectionHasValues = true;
            }
            // Not in sync
            if (!networkModel.isInSync()) {
                addSyncDiff(networkModel);
                descriptionSectionHasValues = true;
            }
        }

        if (descriptionSectionHasValues) {
            insertHorizontalLine();
        }

        boolean isDisplay = false;
        boolean isMigration = false;
        boolean isGluster = false;
        boolean isDefaultRoute = false;

        if (entity.getCluster() != null) {
            isDisplay = entity.getCluster().isDisplay();
            isMigration = entity.getCluster().isMigration();
            isGluster = entity.getCluster().isGluster();
            isDefaultRoute = entity.getCluster().isDefaultRoute();
        }

        // Usages
        if (networkModel.isManagement() || isDisplay || entity.isVmNetwork() || isMigration || isGluster || isDefaultRoute) {

            addRow(templates.strongTextWithColor(constants.usageItemInfo() + ":", //$NON-NLS-1$
                    SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR)));

            if (networkModel.isManagement()) {
                addRow(templates.imageTextSetupNetworkUsage(mgmtNetworkImage, constants.managementItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            if (isDisplay) {
                addRow(templates.imageTextSetupNetworkUsage(monitorImage, constants.displayItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            if (entity.isVmNetwork()) {
                addRow(templates.imageTextSetupNetworkUsage(vmImage, constants.vmItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            if (isMigration) {
                addRow(templates.imageTextSetupNetworkUsage(migrationImage, constants.migrationItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            if (isGluster) {
                addRow(templates.imageTextSetupNetworkUsage(glusterNwImage, constants.glusterNwItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            if (isDefaultRoute) {
                addRow(templates.imageTextSetupNetworkUsage(defaultRouteImage, constants.defaultRouteItemInfo(),
                        SafeStylesUtils.forTrustedColor(TEXT_COLOR)));
            }

            insertHorizontalLine();
        }

        // Mtu
        if (!entity.isExternal()) {
            addMtuInfo(entity.getMtu());
        }

        // Boot protocol and IP info
        if (networkModel.isAttached() && networkModel.isManaged()) {
            addBootProtoAndIpInfo(new InterfacePropertiesAccessor.FromNetworkAttachmentForModel(
                    networkModel.getNetworkAttachment(),
                    null,
                    networkModel.getVlanDevice() != null ?
                            networkModel.getVlanDevice() :
                            networkModel.getAttachedToNic().getOriginalIface())
            );
        }
    }

    /***
     *
     * @param networkModel must be managed
     */
    private void addSyncDiff(LogicalNetworkModel networkModel) {
        addRow(templates.imageTextSetupNetwork(notInSyncImage, templates.coloredText(constants.hostOutOfSync(),
                SafeStylesUtils.forTrustedColor(TEXT_COLOR))));
        SafeHtml safeHtml  = SafeHtmlUtils.fromTrustedString(generatePreviewSentence());
        addRow(safeHtml);
        List<ReportedConfiguration> panelParameters = filterSyncProperties(networkModel);
        Widget networkOutOfSyncPanel = new NetworkOutOfSyncPanel(panelParameters).outOfSyncTableAsWidget();
        contents.insertRow(contents.getRowCount());
        contents.setWidget(contents.getRowCount(), 0, networkOutOfSyncPanel);
    }

    private String generatePreviewSentence() {
        SafeHtml host = templates.strongTextWithColor(constants.hostForOutOfSyncSentence(),
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR));
        SafeHtml dc = templates.strongTextWithColor(constants.dcForOutOfSyncSentence(),
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR));
        SafeHtml outOfSyncPreviewSentence = templates.coloredText(constants.hostOutOfSyncPreviewSentence(),
                SafeStylesUtils.forTrustedColor(TEXT_COLOR));
        return templates.hostOutOfSyncPreviewSentence(host, outOfSyncPreviewSentence, dc).asString();
    }

    /***
     * will filter out all sync properties
     * @param networkModel must be managed
     */
    private List<ReportedConfiguration> filterSyncProperties(LogicalNetworkModel networkModel){
        ReportedConfigurations reportedConfigurations = networkModel.getReportedConfigurations();
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();
        return Linq.where(reportedConfigurationList, reportedConfiguration -> !reportedConfiguration.isInSync());
    }

    private void showNic(NetworkInterfaceModel nic) {
        contents.removeAllRows();
        VdsNetworkInterface entity = nic.getOriginalIface();
        addRow(templates.titleSetupNetworkTooltip(nic.getName(),
                SafeStylesUtils.forTrustedBackgroundColor(BACKGROUND_COLOR)));

        if (nic.getItems().isEmpty() && !nic.isBonded()) {
            insertHorizontalLine();
            addMtuInfo(entity.getMtu());
            addBootProtoAndIpInfo(new InterfacePropertiesAccessor.FromNic(entity, null));
        }
        if (nic instanceof BondNetworkInterfaceModel) {
            CreateOrUpdateBond createOrUpdateBond = ((BondNetworkInterfaceModel) nic).getCreateOrUpdateBond();
            String bondOptions = entity.getBondOptions();
            if (createOrUpdateBond != null) {
                bondOptions = createOrUpdateBond.getBondOptions();
            }
            addRow(constants.bondOptionsItemInfo(),
                    bondOptions.replace(BOND_OPTIONS_IP_SEPARATOR, BOND_OPTIONS_IP_SEPARATOR + " ")); //$NON-NLS-1$
        } else {
            addLldpInfo(nic);
        }
        if (nic.isVf()) {
            addRow(constants.physicalFunction(), nic.getPhysicalFunction());
        }

        HostNicVfsConfig vfsConfig = nic.getSetupModel().getVfConfig(nic.getOriginalIface().getId());
        if (nic.isSriovEnabled()) {
            addNonNullOrEmptyValueRow(constants.enabledVirtualFunctions(), String.valueOf(vfsConfig.getNumOfVfs()));
            addNonNullOrEmptyValueRow(constants.freeVirtualFunctions(), String.valueOf(vfsConfig.getNumOfFreeVfs()));
        }
    }

    private void addLldpInfo(NetworkInterfaceModel nic) {
        HostSetupNetworksModel model = nic.getSetupModel();
        String name = nic.getOriginalIface().getName();
        LldpInfo lldpInfo = model.getNetworkLldpByName(name);
        insertHorizontalLine();
        addRow(templates.strongTextWithColor(constants.linkLayerInfo(),
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR)));

        if (lldpInfo != null) {
            if (lldpInfo.isEnabled()) {
                List<Tlv> filteredTlvs =
                        lldpInfo.getTlvs().stream().filter(this::isTlvImportant).collect(Collectors.toList());
                if (!filteredTlvs.isEmpty()) {
                    filteredTlvs.stream().forEach(tlv -> tlv.getProperties().entrySet().stream()
                            .forEach(entry -> addRow(entry.getKey(), entry.getValue())));
                } else {
                    addRow(SafeHtmlUtils.fromSafeConstant(constants.noImportantLLDP()));
                }
            } else {
                addRow(SafeHtmlUtils.fromSafeConstant(constants.lldpInfoDisabled()));
            }
        } else {
            if (model.isNetworkLldpInfoPresent()) {
                addRow(SafeHtmlUtils.fromSafeConstant(constants.noLldpInfoAvailable()));
            } else {
                addRow(SafeHtmlUtils.fromSafeConstant(constants.fetchingLldpInfo()));
            }
        }
    }

    private boolean isTlvImportant(Tlv tlv) {
        return TlvSpecificType.PortDescription.isSameAsTlv(tlv)
                || TlvSpecificType.SystemName.isSameAsTlv(tlv)
                || TlvSpecificType.VlanName.isSameAsTlv(tlv)
                || TlvSpecificType.PortVlanID.isSameAsTlv(tlv)
                || TlvSpecificType.PortAndProtocolVlanID.isSameAsTlv(tlv)
                || TlvSpecificType.MaximumFrameSize.isSameAsTlv(tlv)
                || TlvSpecificType.LinkAggregation802_1.isSameAsTlv(tlv)
                || TlvSpecificType.LinkAggregation802_3.isSameAsTlv(tlv);
    }

    private void addBootProtoAndIpInfo(InterfacePropertiesAccessor accessor) {
        insertHorizontalLine();

        // IPv4
        addRow(templates.strongTextWithColor(constants.ipv4ItemInfo() + ":", //$NON-NLS-1$
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR)));
        if (accessor.isIpv4Available()) {
            Ipv4BootProtocol ipv4BootProtocol = accessor.getIpv4BootProtocol();
            addRow(constants.bootProtocolItemInfo(), IPV4_RENDERER.render(ipv4BootProtocol));
            addNonNullOrEmptyValueRow(constants.addressItemInfo(), accessor.getIpv4Address());
            addNonNullOrEmptyValueRow(constants.subnetItemInfo(), accessor.getIpv4Netmask());
            addNonNullOrEmptyValueRow(constants.gatewayItemInfo(), accessor.getIpv4Gateway());
        } else {
            addRow(SafeHtmlUtils.fromSafeConstant(constants.notAvailableLabel()));
        }

        // IPv6
        addRow(templates.strongTextWithColor(constants.ipv6ItemInfo() + ":", //$NON-NLS-1$
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR)));
        if (accessor.isIpv6Available()) {
            Ipv6BootProtocol ipv6BootProtocol = accessor.getIpv6BootProtocol();
            addRow(constants.bootProtocolItemInfo(), IPV6_RENDERER.render(ipv6BootProtocol));
            addNonNullOrEmptyValueRow(constants.addressItemInfo(), accessor.getIpv6Address());
            addNonNullOrEmptyValueRow(constants.prefixItemInfo(),
                    accessor.getIpv6Prefix() != null ? accessor.getIpv6Prefix().toString() : null);
            addNonNullOrEmptyValueRow(constants.gatewayItemInfo(), accessor.getIpv6Gateway());
        } else {
            addRow(SafeHtmlUtils.fromSafeConstant(constants.notAvailableLabel()));
        }
    }

    private void addMtuInfo(int mtuValue) {
        final String mtuValueString = mtuValue == 0 ? messages.defaultMtu(defaultMtu) : String.valueOf(mtuValue);
        final String mtu = templates.strongTextWithColor(constants.mtuItemInfo(),
                SafeStylesUtils.forTrustedColor(WHITE_TEXT_COLOR)).asString() + ": " + //$NON-NLS-1$
                templates.coloredText(mtuValueString, SafeStylesUtils.forTrustedColor(TEXT_COLOR)).asString();
        addRow(SafeHtmlUtils.fromTrustedString(mtu));
    }

    private void addNonNullOrEmptyValueRow(String label, String value) {
        if (StringHelper.isNotNullOrEmpty(value)) {
            addRow(label, value);
        }
    }

    private void insertHorizontalLine() {
        addRow(templates.horizontalLine());
    }
}
