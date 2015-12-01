package org.ovirt.engine.core.utils.violation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils.LinkedHashSetCreator;

final class ViolatingEntityDetails implements ViolationRenderer {
    private final Map<String, Set<String>> details = new LinkedHashMap<>();

    ViolatingEntityDetails(String detailName, String detailValue) {
        addDetail(detailName, detailValue);
    }

    void addDetail(String detailName, String detailValue) {
        MultiValueMapUtils.addToMap(detailName,
                detailValue,
                details,
                new LinkedHashSetCreator<>());
    }

    @Override
    public List<String> render() {
        final List<String> result = new ArrayList<>(details.size());
        for (Entry<String, Set<String>> detailEntry : details.entrySet()) {
            final String detailName = detailEntry.getKey();
            final Set<String> detailValues = detailEntry.getValue();
            result.addAll(ReplacementUtils.replaceWith(detailName, detailValues));
        }
        return result;
    }
}
