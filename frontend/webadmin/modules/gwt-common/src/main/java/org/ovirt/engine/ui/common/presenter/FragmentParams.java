package org.ovirt.engine.ui.common.presenter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public enum FragmentParams {
    SEARCH("search"), //$NON-NLS-1$
    NAME("name"), //$NON-NLS-1$
    DATACENTER("dataCenter"), //$NON-NLS-1$
    NETWORK("network"), //$NON-NLS-1$
    ID("id"); //$NON-NLS-1$

    private final String paramName;

    private FragmentParams(String paramName) {
        this.paramName = paramName;
    }

    public String getName() {
        return paramName;
    }

    public static Set<FragmentParams> getParams(PlaceRequest currentPlace) {
        return getParams(currentPlace.getParameterNames());
    }

    public static Set<FragmentParams> getParams(Set<String> parameterNames) {
        Set<FragmentParams> result = new HashSet<>();
        for (int i = 0; i < FragmentParams.values().length; i++) {
            if (parameterNames.contains(FragmentParams.values()[i].getName())) {
                result.add(FragmentParams.values()[i]);
            }
        }
        return result;
    }

    public static <T> List<T> findItemByName(String name, SearchableListModel<?, T> model) {
        Collection<T> items = model.getItems();
        List<T> namedItems = null;
        if (items != null) {
            namedItems = items.stream().filter(item ->
                item instanceof Nameable && name.equals(((Nameable)item).getName())).collect(Collectors.toList());
        }
        return namedItems;
    }
}
