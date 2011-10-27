package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Column for displaying {@link SafeHtml} instances.
 * 
 * @param <T>
 *            Table row data type.
 */
public abstract class PopupTextColumn<T> extends SafeHtmlColumn<T> {
    int maxLength = 50;

    public PopupTextColumn(int maxLength) {
        this.maxLength = maxLength;
    }

    public PopupTextColumn() {

    }

    @Override
    public final SafeHtml getValue(T object) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String value = getRawValue(object);
        String shownValue = value;
        String title = "";
        // int shownValueLength = getRawValue(object).length() < maxLength ? getRawValue(object).length() : maxLength;

        if (value == null)
            return b.toSafeHtml();

        if (value.length() > maxLength) {
            shownValue = shownValue.substring(0, maxLength - 4) + " ...";
            title = value;
        }

        b.appendHtmlConstant("<div title='" + title + "'>"
                + shownValue);

        return b.toSafeHtml();
    }

    public abstract String getRawValue(T object);
}
