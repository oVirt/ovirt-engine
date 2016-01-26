package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidatorContext;

public class ValidatorConstraint {

    private static ValidatorConstraint INSTANCE = new ValidatorConstraint();

    private ValidatorConstraint() {
    }

    public static ValidatorConstraint getInstance() {
        return INSTANCE;
    }

    public boolean isValid(ValidatorConstraintArgsFormatValue args, ConstraintValidatorContext context, String nodeName) {
        if (!args.isValidFormat()) {
            return failWith(context, args.getBadFormatErrorMessage(), nodeName);
        } else if (!args.isValidValue()) {
            return failWith(context, args.getBadValueErrorMessage(), nodeName);
        }

        return true;
    }

    private boolean failWith(ConstraintValidatorContext context, String message, String nodeName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addNode(nodeName)
                .addConstraintViolation();
        return false;
    }

    public static class ValidatorConstraintArgsFormatValue {
        private final boolean validFormat;
        private final boolean validValue;
        private final String badFormatErrorMessage;
        private final String badValueErrorMessage;

        public ValidatorConstraintArgsFormatValue(boolean validFormat,
                boolean validValue,
                String badFormatErrorMessage,
                String badValueErrorMessage) {
            this.validFormat = validFormat;
            this.validValue = validValue;
            this.badFormatErrorMessage = badFormatErrorMessage;
            this.badValueErrorMessage = badValueErrorMessage;
        }

        @SuppressWarnings("unused")
        private ValidatorConstraintArgsFormatValue() {
            this.validFormat = false;
            this.validValue = false;
            this.badFormatErrorMessage = null;
            this.badValueErrorMessage = null;
        }

        public boolean isValidFormat() {
            return validFormat;
        }

        public boolean isValidValue() {
            return validValue;
        }

        public String getBadFormatErrorMessage() {
            return badFormatErrorMessage;
        }

        public String getBadValueErrorMessage() {
            return badValueErrorMessage;
        }

    }

}
