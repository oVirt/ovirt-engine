package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.widget.dialog.TooltippedIcon;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class BondPanel extends HostInterfaceHorizontalPanel {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static final String INVALID_AD_PARTNER_MAC = "00:00:00:00:00:00";//$NON-NLS-1$

    public BondPanel(HostInterfaceLineModel lineModel, VDS vds) {
        super();

        clear();

        Style style = getElement().getStyle();
        style.setBorderColor("white"); //$NON-NLS-1$
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);

        if (lineModel.getIsBonded()) {
            // Bond icon
            Image bondIcon;
            Bond bond = (Bond) lineModel.getInterface();
            if (InterfaceStatus.UP.equals(lineModel.getInterface().getStatistics().getStatus())) {
                bondIcon = new Image(resources.splitUpImage());
            } else {
                bondIcon = new Image(resources.splitDownImage());
            }

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
