package org.ovirt.engine.core.bll.scheduling.external;

public class ExternalSchedulerDiscoveryUnit{
    private String name;
    private String description;
    private String regex;

    public ExternalSchedulerDiscoveryUnit(String name, String description, String regex) {
        this.name = name;
        this.description = description;
        this.regex = regex;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    String getRegex() {
        return regex;
    }

}


