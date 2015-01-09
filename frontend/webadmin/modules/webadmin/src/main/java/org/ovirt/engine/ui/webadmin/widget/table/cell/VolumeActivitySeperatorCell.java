package org.ovirt.engine.ui.webadmin.widget.table.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeActivitySeperatorCell<T> extends AbstractCell<T> {

    public interface Resources extends ClientBundle {

        @Source("org/ovirt/engine/ui/common/images/separator.gif")
        @ImageOptions(width = 1, height = 9)
        ImageResource separator();

    }

    private Resources imageResources;

    public VolumeActivitySeperatorCell() {
        imageResources = GWT.create(Resources.class);
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        SafeHtml separatorImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(imageResources.separator()).getHTML());
        sb.append(separatorImageHtml);
    }
}
