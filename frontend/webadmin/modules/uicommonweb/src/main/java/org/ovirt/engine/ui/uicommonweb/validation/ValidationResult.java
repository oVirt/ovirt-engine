package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public final class ValidationResult {
    private boolean privateSuccess;

    public boolean getSuccess() {
        return privateSuccess;
    }

    public void setSuccess(boolean value) {
        privateSuccess = value;
    }

    private List<String> privateReasons;

    public List<String> getReasons() {
        return privateReasons;
    }

    public void setReasons(List<String> value) {
        privateReasons = value;
    }

    public ValidationResult() {
        this(true, new ArrayList<String>(0));
    }

    public ValidationResult(boolean success, List<String> reasons) {
        setSuccess(success);
        setReasons(reasons);
    }

    public static ValidationResult ok() {
        return new ValidationResult();
    }

    public static ValidationResult fail(String... reasons) {
        return new ValidationResult(false, Arrays.asList(reasons));
    }

    @Override
    public String toString() {
        return "ValidationResult{" + //$NON-NLS-1$
                "success=" + privateSuccess + //$NON-NLS-1$
                ", reasons=" + privateReasons + //$NON-NLS-1$
                "}"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                privateSuccess,
                privateReasons);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValidationResult)) {
            return false;
        }
        ValidationResult other = (ValidationResult) obj;
        return Objects.equals(privateSuccess, other.privateSuccess)
                && Objects.equals(privateReasons, other.privateReasons);
    }
}
