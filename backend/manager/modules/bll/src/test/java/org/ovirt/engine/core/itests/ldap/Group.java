package org.ovirt.engine.core.itests.ldap;

import org.apache.commons.configuration.Configuration;

public class Group {
    private String name, dc, domain, gidNumber;

    public Group(String name,
            String dc,
            String domain,
            String gidNumber,
            String[] userMembers,
            String[] groupMembers) {
        this.name = name;
        this.dc = dc;
        this.domain = domain;
        this.gidNumber = gidNumber;
        this.userMembers = userMembers;
        this.groupMembers = groupMembers;
    }

    private String[] userMembers, groupMembers;

    public Group(Configuration configuration) {
        this(configuration.getString("name"),
                configuration.getString("dc"),
                configuration.getString("domain"),
                configuration.getString("gidNumber"),
                configuration.getString("userMembers", "").split(","),
                configuration.getString("groupMembers", "").split(","));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setGidNumber(String gidNumber) {
        this.gidNumber = gidNumber;
    }

    public String getGidNumber() {
        return gidNumber;
    }

    public String[] getUserMembers() {
        return userMembers;
    }

    public void setUserMembers(String[] userMembers) {
        this.userMembers = userMembers;
    }

    public String[] getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String[] groupMembers) {
        this.groupMembers = groupMembers;
    }

}
