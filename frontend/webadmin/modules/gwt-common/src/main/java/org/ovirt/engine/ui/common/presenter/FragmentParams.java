package org.ovirt.engine.ui.common.presenter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public enum FragmentParams {
    SEARCH("search"), //$NON-NLS-1$
    NAME("name"); //$NON-NLS-1$

    private final String paramName;

    private FragmentParams(String paramName) {
        this.paramName = paramName;
    }

    public String getName() {
        return paramName;
    }

    public static Set<FragmentParams> getParams(PlaceRequest currentPlace) {
        Set<String> parameterNames = currentPlace.getParameterNames();
        Set<FragmentParams> result = new HashSet<>();
        for (int i = 0; i < FragmentParams.values().length; i++) {
            if (parameterNames.contains(FragmentParams.values()[i].getName())) {
                result.add(FragmentParams.values()[i]);
            }
        }
        return result;
    }

    public static <T> T findItemByName(String name, SearchableListModel<?, T> model) {
        Collection<T> items = model.getItems();
        T namedItem = null;
        if (items != null) {
            namedItem = items.stream().filter(item ->
                item instanceof Nameable && name.equals(((Nameable)item).getName())).findFirst().orElse(null);
        }
        return namedItem;
    }
}
