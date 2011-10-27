package org.ovirt.engine.core.itests.ldap;

import java.util.List;

import org.apache.commons.configuration.Configuration;

public class Person {
    private String name, username, dc, domain, ou, givenName, surName, displayName, description, company, country,
            homeDirectory, gidNumber, uidNumber;

    public Person(String name,
            String username,
            String dc,
            String domain,
            String ou,
            String givenName,
            String surName,
            String displayName,
            String description,
            String company,
            String country,
            String homeDirectory,
            String gidNumber,
            String uidNumber,
            List<String> groups) {
        this.name = name;
        this.username = username;
        this.dc = dc;
        this.domain = domain;
        this.ou = ou;
        this.givenName = givenName;
        this.surName = surName;
        this.displayName = displayName;
        this.description = description;
        this.company = company;
        this.country = country;
        this.homeDirectory = homeDirectory;
        this.gidNumber = gidNumber;
        this.uidNumber = uidNumber;
        this.groups = groups;
    }

    private List<String> groups;

    public Person(Configuration configuration) {
        this(configuration.getString("name"),
                configuration.getString("username"),
                configuration.getString("dc"),
                configuration.getString("domain"),
                configuration.getString("ou"),
                configuration.getString("givenName"),
                configuration.getString("surname"),
                configuration.getString("displayName"),
                configuration.getString("description"),
                configuration.getString("company"),
                configuration.getString("country"),
                configuration.getString("homeDirectory"),
                configuration.getString("gidNumber"),
                configuration.getString("uidNumber"),
                null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public void setGidNumber(String gidNumber) {
        this.gidNumber = gidNumber;
    }

    public String getGidNumber() {
        return gidNumber;
    }

    public void setUidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getUidNumber() {
        return uidNumber;
    }

}
