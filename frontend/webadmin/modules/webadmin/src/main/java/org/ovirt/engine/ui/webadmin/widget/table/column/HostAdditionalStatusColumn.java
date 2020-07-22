package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;

public class HostAdditionalStatusColumn extends EntityAdditionalStatusColumn<VDS> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();


    @Override
    public SafeHtml getEntityValue(VDS object) {
        List<SafeHtml> imagesHtml = new ArrayList(getSafeHtmlStringMap(object).keySet());
        if (! imagesHtml.isEmpty()) {
            return MultiImageColumnHelper.getValue(imagesHtml);
        }
        return null;
    }

    @Override
    public SafeHtml getEntityTooltip(VDS object) {
        Map<SafeHtml, String> imagesToText = getSafeHtmlStringMap(object);
        if (!imagesToText.isEmpty()) {
            return MultiImageColumnHelper.getTooltip(imagesToText);
        }
        return null;
    }

    private Map<SafeHtml, String> getSafeHtmlStringMap(VDS object) {
        VDS host = getEntityObject(object);
        Map<SafeHtml, String> imagesToText = new LinkedHashMap<>();
        ExternalStatus externalStatus = host.getExternalStatus();

        if (host.isUpdateAvailable()) {
            imagesToText.put(getImageSafeHtml(resources.updateAvailableImage()), constants.updateAvailable());
        }
        if (externalStatus != null && host.getExternalStatus() != ExternalStatus.Ok) {
            ImageResource statusImage = getStatusImage(externalStatus);
            if (statusImage != null) {
                imagesToText.put(getImageSafeHtml(statusImage),
                        constants.ExternalStatus() + externalStatus.name());
            }
        }
        if (host.isHostedEngineHost()) {
            imagesToText.put(getImageSafeHtml(resources.mgmtNetwork()), constants.hostedEngineVmTooltip());
        } else if (host.getHighlyAvailableIsActive()) {
            if(host.getHighlyAvailableScore() > 0) {
                imagesToText.put(getImageSafeHtml(resources.haActive()), constants.haActiveTooltip());
            } else {
                imagesToText.put(getImageSafeHtml(resources.haActiveZeroHaScore()),
                        constants.haActiveZeroHaScoreTooltip());
            }
        }

        if (!host.isManaged()) {
            imagesToText.put(getImageSafeHtml(resources.container()), constants.isRunninVmsInContainer());
        }
        return imagesToText;
    }

    @Override
    protected VDS getEntityObject(VDS object) {
        return object;
    }
}
