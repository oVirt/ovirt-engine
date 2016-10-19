package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.widget.dialog.TooltippedIcon;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterface;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;

public class BondPanel extends HostInterfaceHorizontalPanel {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    private static final String INVALID_AD_PARTNER_MAC = "00:00:00:00:00:00";//$NON-NLS-1$

    public BondPanel(HostInterfaceLineModel lineModel, VDS vds) {
        super();

        clear();

        Style style = getElement().getStyle();
        style.setBorderColor("white"); //$NON-NLS-1$
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);

        if (lineModel.getIsBonded()) {
            Bond bond = (Bond) lineModel.getInterface();
            TooltippedIcon bondIcon = createBondIcon(lineModel);
            add(bondIcon);

            boolean isAdPartnerMacValid = isAdPartnerMacValid(bond, vds);
            setCellWidth(bondIcon, isAdPartnerMacValid ? "20%" : "15%"); //$NON-NLS-1$ //$NON-NLS-2$
            addAdPartnerMacIfNeeded(bond, vds, isAdPartnerMacValid);

            // Bond name
            add(new InterfaceLabel(lineModel.getInterface()));
        } else {
            add(new Label("")); //$NON-NLS-1$
        }
    }

    private TooltippedIcon createBondIcon(HostInterfaceLineModel lineModel) {
        InterfaceStatus interfaceStatus = lineModel.getInterface().getStatistics().getStatus();
        ImageResource image = InterfaceStatus.UP.equals(interfaceStatus) ?
                resources.splitUpImage() : resources.splitDownImage();
        SafeHtml bondPropertiesMessage = createBondTooltipMessage(lineModel, interfaceStatus);
        SafeHtml bondMessage = templates.italicWordWrapMaxWidthWithBoldTitle(constants.bondProperties(), bondPropertiesMessage);
        TooltippedIcon bondIcon = new TooltippedIcon(bondMessage, image, image);
        return bondIcon;
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
        return StringUtils.join(bondProperties, "\n");//$NON-NLS-1$
    }

    private void addAdPartnerMacIfNeeded(Bond bond, VDS vds, boolean isAdPartnerMacValid) {
        if (!isAdPartnerMacValid) {
            ImageResource noPartnerMacImage = resources.alertImage();
            String noPartnerMacMessage = constants.bondInMode4HasNoPartnerMac();

            TooltippedIcon bondInMode4HasNoPartnerMacIcon = new TooltippedIcon(
                    templates.italicWordWrapMaxWidth(noPartnerMacMessage), noPartnerMacImage, noPartnerMacImage);
            add(bondInMode4HasNoPartnerMacIcon);
            setCellWidth(bondInMode4HasNoPartnerMacIcon, "5%"); //$NON-NLS-1$
        }
    }

    private boolean isAdPartnerMacValid(Bond bond, VDS vds){
        String partnerMac = bond.getAdPartnerMac();
        boolean isAdPartnerMacEmpty = partnerMac == null || partnerMac.isEmpty() || partnerMac.equals(INVALID_AD_PARTNER_MAC);
        boolean isIfcUp = InterfaceStatus.UP.equals(bond.getStatistics().getStatus());
        boolean isBond4 = BondMode.BOND4.equals(BondMode.parseBondMode(bond.getBondOptions()));
        boolean isAdPartnerSupportedForCluster = (Boolean)AsyncDataProvider.getInstance().getConfigValuePreConverted(
                ConfigurationValues.AdPartnerMacSupported, vds.getClusterCompatibilityVersion().getValue());

        return !isAdPartnerMacEmpty || !isIfcUp || !isBond4 || !isAdPartnerSupportedForCluster;
    }

}
