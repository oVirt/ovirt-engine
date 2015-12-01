package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
/**
 * Validates a that Pair of two fields are equal
 */
public class MatchFieldsValidator implements IValidation {

    private String first;
    private String second;

    public MatchFieldsValidator(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult rs = new ValidationResult();
        rs.setSuccess(false);

        if (!Objects.equals(first, second)) {
            ArrayList<String> reasons = new ArrayList<>();
            reasons.add(ConstantsManager.getInstance().getConstants().cloudInitRootPasswordMatchMessage());
            rs.setReasons(reasons);
        } else {
            rs.setSuccess(true);
        }
        return rs;
    }
}


