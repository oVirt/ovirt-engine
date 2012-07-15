package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.ConfiguredRangeValidator;

/**
 * The annotated element must be a number whose value exists in range of specified minimum to the maximum value obtained
 * from the provided configuration value.
 * <p/>
 * The minimum value could be provided either as constant via the <code>min()</code> or by specifying a configuration
 * value to be obtained from via <code>minConfigValue()</code> In case of both provided, <code>minConfigValue()</code>
 * will precede the value provided by <code>min()</code>.
 * <p/>
 * <code>null</code> elements are considered valid
 */
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ConfiguredRangeValidator.class)
public @interface ConfiguredRange {
    String message() default "CONFIGURED_RANGE_INVALID";

    /**
     * @return value the element must be higher or equal to
     */
    int min() default 0;

    /**
     * @return the configuration value to evaluate the value for the element to be higher or equal to
     */
    ConfigValues minConfigValue() default ConfigValues.Invalid;

    /**
     * @return the configuration value to evaluate the value for the element to be lower or equal to
     */
    ConfigValues maxConfigValue();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
