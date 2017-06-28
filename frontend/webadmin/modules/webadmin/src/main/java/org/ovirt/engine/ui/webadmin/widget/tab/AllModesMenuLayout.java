package org.ovirt.engine.ui.webadmin.widget.tab;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AllModesMenuLayout implements PrimaryMenuItem {
    COMPUTE(AssetProvider.getConstants().computeLabel(), null, 0, PatternflyIconType.PF_CPU),
    NETWORK(AssetProvider.getConstants().networkHost(), null, 1, PatternflyIconType.PF_NETWORK),
    STORAGE(AssetProvider.getConstants().storageMainTabLabel(), null, 2, IconType.DATABASE),
    ADMIN(AssetProvider.getConstants().administration(), null, 3, IconType.COG),
    EVENTS(AssetProvider.getConstants().eventsEventFooter(), WebAdminApplicationPlaces.eventMainTabPlace, 4,
            PatternflyIconType.PF_FLAG);


    private final String href;
    private final String title;
    private final int index;
    private final HasCssName icon;

    AllModesMenuLayout(String title, String href, int index, HasCssName icon) {
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
