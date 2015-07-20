package org.ovirt.engine.core.utils.violation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.ovirt.engine.core.utils.ReplacementUtils;

public final class DetailedViolation implements ViolationRenderer {

    private final String violationName;
    private final Map<String, ViolatingEntityDetails> violations;

    public DetailedViolation(String violationName) {
        Objects.requireNonNull(violationName, "violationName cannot be null");

        this.violationName = violationName;
        this.violations = new LinkedHashMap<>();
    }

    public void add(String violatingEntity, String detailName, String detailValue) {
        ViolatingEntityDetails violatingEntityDetails = violations.get(violatingEntity);
        if (violatingEntityDetails == null) {
            violatingEntityDetails = new ViolatingEntityDetails(detailName, detailValue);
            violations.put(violatingEntity, violatingEntityDetails);
        } else {
            violatingEntityDetails.addDetail(detailName, detailValue);
        }
    }

    @Override
    public List<String> render() {
        final List<String> violationMessages = new ArrayList<>();
        for (Entry<String, ViolatingEntityDetails> violationEntry : violations.entrySet()) {
            final String violatingEntity = violationEntry.getKey();
            final ViolatingEntityDetails violationEntityDetails = violationEntry.getValue();
            violationMessages.add(violationName);
            violationMessages.add(ReplacementUtils.createSetVariableString(violationName + Violation.LIST_SUFFIX,
                    violatingEntity));
            violationMessages.addAll(violationEntityDetails.render());
        }

        return violationMessages;
    }
}
