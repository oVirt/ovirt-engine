package org.ovirt.engine.core.uutils.cli;

/**
 * Represents argument specification inside {@link ExtendedCliParser}
 */
class Argument {
    private final String shortName;

    private final String longName;

    private final String destination;

    private final boolean valueRequired;

    Argument(String shortName, String longName, String destination, boolean valueRequired) {
        this.shortName = shortName;
        this.longName = longName;
        this.destination = destination;
        this.valueRequired = valueRequired;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isValueRequied() {
        return valueRequired;
    }
}
