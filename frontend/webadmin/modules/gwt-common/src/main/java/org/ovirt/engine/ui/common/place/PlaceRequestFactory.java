package org.ovirt.engine.ui.common.place;

import java.util.HashMap;
import java.util.Map;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Factory that implements flyweight pattern for GWTP {@link PlaceRequest} instances.
 */
public class PlaceRequestFactory {

    private static final Map<String, PlaceRequest> instances = new HashMap<>();

    public static PlaceRequest get(String nameToken) {
        if (!instances.containsKey(nameToken)) {
            instances.put(nameToken, new PlaceRequest.Builder().nameToken(nameToken).build());
        }
        return instances.get(nameToken);
    }

}
