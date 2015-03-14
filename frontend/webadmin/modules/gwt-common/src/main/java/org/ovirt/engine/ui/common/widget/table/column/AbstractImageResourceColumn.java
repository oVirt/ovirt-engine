package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.ImageResourceCell;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Column for rendering {@link ImageResource} instances using {@link ImageResourceCell}. Supports tooltips.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractImageResourceColumn<T> extends AbstractColumn<T, ImageResource> {

    private final static CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private final static CommonApplicationResources resources = AssetProvider.getResources();
    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    public AbstractImageResourceColumn() {
        super(new ImageResourceCell());
    }

    public AbstractImageResourceColumn(ImageResourceCell cell) {
        super(cell);
    }

    @Override
    public ImageResourceCell getCell() {
        return (ImageResourceCell) super.getCell();
    }

    public ImageResource getDefaultImage() {
        return null;
    }

    public String getHeaderHtml() {
        if (getDefaultImage() == null) {
            return constants.empty();
        }

        // TODO tt (in follow-up header patch) users of AbstractImageResourceColumn will have to set the Header Tooltip
        return templates.headerImage(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(getDefaultImage()).getHTML())).asString();
    }

}
