package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * A comparator that sorts according to simple status images - first null, then down, then up.
 */
public class SimpleStatusImageComparator implements Comparator<ImageResource> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final ImageResource downImage = resources.downImage();

    @Override
    public int compare(ImageResource o1, ImageResource o2) {
        if (o1 == o2) {
            return 0;
        } else if (o1 == null || o2 == null) {
            return (o1 == null) ? -1 : 1;
        } else if (o1.equals(o2)) {
            return 0;
        } else {
            return downImage.equals(o1) ? -1 : 1;
        }
    }

}
