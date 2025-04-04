package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;
import java.util.stream.Collectors;

import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

/**
 * LUN model filter.
 */
public class LunFilter extends LunFilterBase implements ModelFilter<LunModel> {

    public LunFilter(boolean isHideUsedLuns) {
        super(isHideUsedLuns);
    }

    public Collection<LunModel> filter(Collection<LunModel> items) {
        if (items == null || !needFilter()) {
            return items;
        }

        return items.stream().filter(lunModel -> {
            if (getIsHideUsedLuns()) {
                return !lunModel.getIsUsed();
            }
            return true;
        }).collect(Collectors.toList());
    }
}
