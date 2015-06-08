package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StorageDomainAdditionalStatusColumn extends EntityAdditionalStatusColumn<StorageDomain> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();


    @Override
    public SafeHtml getEntityValue(StorageDomain object) {
        List<SafeHtml> imagesHtml = new ArrayList(getSafeHtmlStringMap(object).keySet());
        if (! imagesHtml.isEmpty()) {
            return MultiImageColumnHelper.getValue(imagesHtml);
        }
        return null;
    }

    @Override
    public SafeHtml getEntityTooltip(StorageDomain object) {
        Map<SafeHtml, String> imagesToText = getSafeHtmlStringMap(object);
        if (!imagesToText.isEmpty()) {
            return MultiImageColumnHelper.getTooltip(imagesToText);
        }
        return null;
    }

    private Map<SafeHtml, String> getSafeHtmlStringMap(StorageDomain object) {
        StorageDomain storageDomain = getEntityObject(object);
        Map<SafeHtml, String> imagesToText = new LinkedHashMap<>();

        if (storageDomain.getExternalStatus() != ExternalStatus.Ok) {
            ImageResource statusImage = getStatusImage(storageDomain.getExternalStatus());
            imagesToText.put(getImageSafeHtml(statusImage),
                    constants.ExternalStatus() + storageDomain.getExternalStatus().name());
        }
        return imagesToText;
    }

    @Override
    protected StorageDomain getEntityObject(StorageDomain object) {
        return object;
    }
}
