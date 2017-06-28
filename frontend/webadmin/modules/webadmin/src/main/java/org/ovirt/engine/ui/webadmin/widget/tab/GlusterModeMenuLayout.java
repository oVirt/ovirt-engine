package org.ovirt.engine.ui.webadmin.widget.tab;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.HasCssName;

public enum GlusterModeMenuLayout implements PrimaryMenuItem {
    STORAGE(AssetProvider.getConstants().storageMainTabLabel(), null, 0, IconType.DATABASE),
    NETWORK(AssetProvider.getConstants().networkHost(), WebAdminApplicationPlaces.networkMainTabPlace, 1,
            PatternflyIconType.PF_NETWORK),
    ADMIN(AssetProvider.getConstants().administration(), null, 2, IconType.COG),
    EVENTS(AssetProvider.getConstants().eventsEventFooter(), WebAdminApplicationPlaces.eventMainTabPlace, 3,
            PatternflyIconType.PF_FLAG);


    private final String href;
    private final String title;
    private final int index;
    private final HasCssName icon;

    GlusterModeMenuLayout(String title, String href, int index, HasCssName icon) {
        this.title = title;
        this.index = index;
        this.href = href;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    public String getHref() {
        return href;
    }

    public HasCssName getIcon() {
        return icon;
    }
}
