package org.ovirt.engine.core.utils.violation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.utils.ReplacementUtils;

public final class Violation implements ViolationRenderer {
    static final String LIST_SUFFIX = "_LIST";

    private final String violationName;
    private final Set<String> violatingEntities;

    public Violation(String violationName, String violatingEntities) {
        Objects.requireNonNull(violationName, "violationName cannot be null");
        Objects.requireNonNull(violatingEntities, "violatingEntities cannot be null");

        this.violationName = violationName;
        this.violatingEntities = new LinkedHashSet<>();
        this.violatingEntities.add(violatingEntities);
    }

    @Override
    public List<String> render() {
        final List<String> violationMessages = new ArrayList<>();
        violationMessages.add(violationName);
        violationMessages.addAll(ReplacementUtils.replaceWith(violationName + LIST_SUFFIX, violatingEntities));
        return violationMessages;
    }

    public void add(String violatingEntity) {
        violatingEntities.add(violatingEntity);
    }
}
