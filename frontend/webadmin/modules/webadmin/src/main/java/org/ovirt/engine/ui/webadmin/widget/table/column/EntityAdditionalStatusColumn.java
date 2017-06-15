package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class EntityAdditionalStatusColumn<S> extends AbstractSafeHtmlColumn<S> {

    protected static final ApplicationResources resources = AssetProvider.getResources();

    protected ImageResource getStatusImage(ExternalStatus externalStatus) {
        switch (externalStatus) {
        case Info:
            return resources.ExternalInfoStatusImage();
        case Warning:
            return resources.ExternalWarningStatusImage();
        case Error:
            return resources.ExternalErrorStatusImage();
        case Failure:
            return resources.ExternalFailureStatusImage();
        default:
            return null;
        }
    }

    protected SafeHtml getImageSafeHtml(ImageResource imageResource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.
                create(imageResource).getHTML());
    }

    protected SafeHtml getImageSafeHtml(IconType iconType) {
        return AssetProvider.getTemplates().iconTypeAlertTemplate(iconType.getCssName());
    }

    protected abstract SafeHtml getEntityValue(S s);

    protected abstract SafeHtml getEntityTooltip(S object);

    protected abstract S getEntityObject(S object);

    @Override
    public SafeHtml getValue(S object) {
        if (getEntityObject(object) == null) {
            return null;
        }
        return getEntityValue(object);
    }

    @Override
    public SafeHtml getTooltip(S object) {
        if (getEntityObject(object) == null) {
            return null;
        }
        return getEntityTooltip(object);
    }
}
