package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.Objects;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ClusterAdditionalStatusColumn extends EntityAdditionalStatusColumn<Cluster> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final String[] cpus = new String[]{"Intel Conroe Family", //$NON-NLS-1$
                                                      "Intel Penryn Family", //$NON-NLS-1$
                                                      "AMD Opteron G1", //$NON-NLS-1$
                                                      "AMD Opteron G2", //$NON-NLS-1$
                                                      "AMD Opteron G3", //$NON-NLS-1$
                                                      "Intel Nehalem IBRS Family", //$NON-NLS-1$
                                                      "Intel Westmere IBRS Family", //$NON-NLS-1$
                                                      "Intel SandyBridge IBRS Family", //$NON-NLS-1$
                                                      "Intel Haswell-noTSX IBRS Family", //$NON-NLS-1$
                                                      "Intel Haswell IBRS Family", //$NON-NLS-1$
                                                      "Intel Broadwell-noTSX IBRS Family", //$NON-NLS-1$
                                                      "Intel Broadwell IBRS Family", //$NON-NLS-1$
                                                      "Intel Skylake Client IBRS Family", //$NON-NLS-1$
                                                      "Intel Skylake Server IBRS Family", //$NON-NLS-1$
                                                      "AMD EPYC IBPB"}; //$NON-NLS-1$
    private static final String[] versions = new String[]{"4.2",  //$NON-NLS-1$
                                                          "4.1",  //$NON-NLS-1$
                                                          "4.0",  //$NON-NLS-1$
                                                          "3.6"}; //$NON-NLS-1$

    private boolean isDeprecated(Cluster object) {
        for (String version : versions) {
            if (version.equals(object.getCompatibilityVersion().toString())) {
                for (String cpu : cpus) {
                    if (cpu.equals(object.getCpuName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public SafeHtml getEntityValue(Cluster object) {
        ArrayList<SafeHtml> images = new ArrayList<>();

        if (object.isClusterCompatibilityLevelUpgradeNeeded()
                || isDeprecated(object)
                || object.hasHostWithMissingCpuFlags()
                || isCpuConfigurationOutdated(object)) {
            images.add(getImageSafeHtml(IconType.EXCLAMATION));
        }

        if (!object.getHostNamesOutOfSync().isEmpty()) {
            images.add(templates.brokenLinkRed());
        }

        if (!object.isManaged()) {
            images.add(getImageSafeHtml(resources.container()));
        }

        if (object.getClusterHostsAndVms() != null && object.getClusterHostsAndVms().getHostsWithUpdateAvailable() > 0) {
            images.add(getImageSafeHtml(resources.updateAvailableImage()));
        }

        if (images.isEmpty()) {
          return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
        SafeHtmlBuilder entityImages = new SafeHtmlBuilder();
        entityImages.append(images.remove(0));
        for (SafeHtml safehtml : images) {
            entityImages.appendHtmlConstant(constants.space());
            entityImages.append(safehtml);
        }
        return templates.image(entityImages.toSafeHtml());
    }

    @Override
    public SafeHtml getEntityTooltip(Cluster object) {
        ArrayList<SafeHtml> tooltips = new ArrayList<>();

        if (object.isClusterCompatibilityLevelUpgradeNeeded()) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .append(getImageSafeHtml(IconType.EXCLAMATION))
                .appendHtmlConstant(constants.space())
                .appendHtmlConstant(constants.clusterLevelUpgradeNeeded());
            tooltips.add(tooltip.toSafeHtml());
        }

        if (isDeprecated(object)) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .append(getImageSafeHtml(IconType.EXCLAMATION))
                .appendHtmlConstant(constants.space())
                .appendEscaped(messages.cpuDeprecationWarning(object.getCpuName()));
            tooltips.add(tooltip.toSafeHtml());
        }

        if (!object.getHostNamesOutOfSync().isEmpty()) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .append(hostListText(object));
            tooltips.add(tooltip.toSafeHtml());
        }

        if (isCpuConfigurationOutdated(object)) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .append(getImageSafeHtml(IconType.EXCLAMATION))
                .appendHtmlConstant(constants.space())
                .appendEscaped(constants.clusterCpuConfigurationOutdatedWarning());
            tooltips.add(tooltip.toSafeHtml());
        }

        if (object.hasHostWithMissingCpuFlags()) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .append(getImageSafeHtml(IconType.EXCLAMATION))
                .appendHtmlConstant(constants.space())
                .appendEscaped(constants.clusterHasHostWithMissingCpuFlagsWarning());
            tooltips.add(tooltip.toSafeHtml());
        }

        if (!object.isManaged()) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .appendHtmlConstant(constants.integratedWithContainerPlatform());
            tooltips.add(tooltip.toSafeHtml());
        }

        if (object.getClusterHostsAndVms() != null && object.getClusterHostsAndVms().getHostsWithUpdateAvailable() > 0) {
            SafeHtmlBuilder tooltip = new SafeHtmlBuilder()
                .appendHtmlConstant(constants.clusterHasUpgradableHosts());
            tooltips.add(tooltip.toSafeHtml());
        }

        if (tooltips.isEmpty()) {
          return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
        SafeHtmlBuilder entityTooltip = new SafeHtmlBuilder();
        entityTooltip.append(tooltips.remove(0));
        for (SafeHtml safeHtml : tooltips) {
            entityTooltip.appendHtmlConstant(constants.lineBreak());
            entityTooltip.appendHtmlConstant(constants.lineBreak());
            entityTooltip.append(safeHtml);
        }
        return entityTooltip.toSafeHtml();
    }

    private SafeHtml hostListText(Cluster object) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder()
            .append(templates.brokenLinkRed())
            .appendHtmlConstant(constants.space())
            .appendEscaped(constants.hostsOutOfSyncWarning())
            .appendHtmlConstant(constants.lineBreak())
            .appendEscapedLines(object.getHostNamesOutOfSync());
        return builder.toSafeHtml();
    }

    private boolean isCpuConfigurationOutdated(Cluster cluster) {
        return !Objects.equals(cluster.getCpuVerb(), cluster.getConfiguredCpuVerb());
    }

    @Override
    protected Cluster getEntityObject(Cluster object) {
        return object;
    }
}
