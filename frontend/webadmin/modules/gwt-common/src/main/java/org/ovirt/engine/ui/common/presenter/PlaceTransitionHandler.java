package org.ovirt.engine.ui.common.presenter;

import java.util.Map;

public interface PlaceTransitionHandler {
    void handlePlaceTransition(String nameToken, Map<String, String> parameters);
}
