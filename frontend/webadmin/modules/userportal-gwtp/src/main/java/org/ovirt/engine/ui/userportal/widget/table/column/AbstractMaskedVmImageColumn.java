package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class AbstractMaskedVmImageColumn<T> extends AbstractImageResourceColumn<T> {

    private static final ApplicationResourcesWithLookup resourcesWithLookup = AssetProvider.getResourcesWithLookup();

    public interface ShowMask<T> {
        boolean showMask(T value);
    }

    private final OsTypeExtractor<T> extractor;
    private final ShowMask<T> showMask;
    private final ImageResource mask;

    public AbstractMaskedVmImageColumn(OsTypeExtractor<T> extractor, ShowMask<T> showMask, ImageResource mask) {
        super();
        this.extractor = extractor;
        this.showMask = showMask;
        this.mask = mask;
    }

    @Override
    public ImageResource getValue(T item) {
        String osTypeName = AsyncDataProvider.getInstance().getOsUniqueOsNames().get(extractor.extractOsType(item));
        ResourcePrototype resource = resourcesWithLookup.getResource(osTypeName + "SmallImage"); //$NON-NLS-1$

        if (!(resource instanceof ImageResource)) {
            resource = resourcesWithLookup.otherSmallImage();
        }

        return (ImageResource) resource;
    }

    @Override
    public void render(Context context, T object, SafeHtmlBuilder sb) {
        if (showMask.showMask(object)) {
            renderMask(mask, sb);
        }
        super.render(context, object, sb);
    }

    public static void renderMask(ImageResource mask, SafeHtmlBuilder sb) {
        // TODO why hardcode to 19px left here?
        sb.appendHtmlConstant("<div style=\"position: absolute; left: 19px\" >"); //$NON-NLS-1$
        sb.append(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(mask).getHTML()));
        sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
    }
}
