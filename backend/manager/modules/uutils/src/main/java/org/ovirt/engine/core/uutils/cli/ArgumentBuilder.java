package org.ovirt.engine.core.uutils.cli;

import org.apache.commons.lang.StringUtils;

/**
 * Builder to create arguments for {@link ExtendedCliParser}
 */
public class ArgumentBuilder {
    private String shortName;
    private String longName;
    private String destination;
    private boolean valueRequired;

    public ArgumentBuilder() {
        valueRequired = false;
    }

    /**
     * Sets short name of argument
     *
     * @param shortName
     *            short name of argument
     * @returns builder instance
     */
    public ArgumentBuilder shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * Sets long name of argument
     *
     * @param longName
     *            long name of argument
     * @returns builder instance
     */
    public ArgumentBuilder longName(String longName) {
        this.longName = longName;
        return this;
    }

    /**
     * Sets destination of argument
     *
     * @param destination
     *            destination of argument
     * @returns builder instance
     */
    public ArgumentBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Sets indicator if value is required
     *
     * @param valueRequired
     *            indicator if value is required
     * @returns builder instance
     */
    public ArgumentBuilder valueRequired(boolean valueRequired) {
        this.valueRequired = valueRequired;
        return this;
    }

    /**
     * Builds argument. If destination is empty, it's set long name (or short name if long name is also empty). By
     * default argument value is not required
     */
    public Argument build() {
        if (StringUtils.isBlank(shortName)
                && StringUtils.isBlank(longName)) {
            throw new IllegalArgumentException("Argument must have non-empty short or long name!");
        }

        if (StringUtils.isBlank(destination)) {
            if (StringUtils.isNotBlank(longName)) {
                destination = longName;
            } else {
                destination = shortName;
            }
        }

        return new Argument(shortName, longName, destination, valueRequired);
    }
}
