package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Decorates a cell with an image mask given to the constructor. The actual cell is shown under the mask provided. The
 * decision if the mask should be shown is done in the implementation of the ShowMask interface.
 *
 * @param <C>
 *            the type that this Cell represents
 */
public class ImageMaskCell<T> extends CompositeCell<T> {

    private final HasCell<T, ?> hasCell;
    private SafeHtml html;
    private final ShowMask<T> showMask;

    @SuppressWarnings("unchecked")
    public ImageMaskCell(HasCell<T, ?> hasCell, ImageResource imageResource, ShowMask<T> showMask) {
        super(new ArrayList<HasCell<T, ?>>(Arrays.asList(hasCell)));
        this.hasCell = hasCell;
        this.showMask = showMask;
        html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(imageResource).getHTML());
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        if (showMask.showMask(value)) {
            sb.appendHtmlConstant("<div style=\"position: absolute\" >");
            sb.append(html);
            sb.appendHtmlConstant("</div>");
        }

        render(context, value, sb, hasCell);
    }

    public static interface ShowMask<T> {
        boolean showMask(T value);
    }
}
