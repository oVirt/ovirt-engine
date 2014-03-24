package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Column for rendering {@link ImageResource} instances using {@link StyledImageResourceCell}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class ImageResourceColumn<T> extends SortableColumn<T, ImageResource> {

    private static final CommonApplicationResources RESOURCES = GWT.create(CommonApplicationResources.class);
    protected static final CommonApplicationConstants CONSTANTS = GWT.create(CommonApplicationConstants.class);
    private static final CommonApplicationTemplates TEMPLATES = GWT.create(CommonApplicationTemplates.class);

    public ImageResourceColumn() {
        super(new StyledImageResourceCell());
    }

    @Override
    public StyledImageResourceCell getCell() {
        return (StyledImageResourceCell) super.getCell();
    }

    public void setTitle(String title) {
        getCell().setTitle(title);
    }

    public void setEnumTitle(Enum<?> enumObj) {
        setTitle(EnumTranslator.getInstance().get(enumObj));
    }

    public String getDefaultTitle() {
        return CONSTANTS.empty();
    }

    public ImageResource getDefaultImage() {
        return null;
    }

    public String getHeaderHtml() {
        if (getDefaultImage() == null) {
            return CONSTANTS.empty();
        }

        return TEMPLATES.imageWithTitle(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(getDefaultImage()).getHTML()), getDefaultTitle()).asString();
    }

    protected CommonApplicationResources getCommonResources() {
        return RESOURCES;
    }

    protected CommonApplicationConstants getCommonConstants() {
        return CONSTANTS;
    }

}
