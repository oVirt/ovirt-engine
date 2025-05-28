package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;
import java.util.stream.Collectors;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;

/**
 * SanTarget Model filter by applying LUN filter.
 */
public class SanTargetFilter extends LunFilterBase implements ModelFilter<SanTargetModel> {

    public SanTargetFilter(boolean isHideUsedLuns) {
        super(isHideUsedLuns);
    }

    public Collection<SanTargetModel> filter(Collection<SanTargetModel> items) {
        if (items == null || !needFilter()) {
            return items;
        }

        return items.stream().filter(sanTargetModel -> {
            if (getIsHideUsedLuns()) {
                // Show targets that are not logged in or that contain unused LUNs
                return !sanTargetModel.getIsLoggedIn() ||
                        sanTargetModel.getLuns().stream().anyMatch(lunModel -> !lunModel.getIsUsed());
            }
            return true;
        }).collect(Collectors.toList());
    }
}
