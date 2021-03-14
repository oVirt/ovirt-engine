package org.ovirt.engine.core.sso.api.jwt;

import java.util.Date;

import org.jboss.resteasy.jwt.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class JWT extends JsonWebToken {
    @JsonProperty("acr")
    private String acr;
    @JsonProperty("auth_time")
    private Date authTime;
    @JsonProperty("sub")
    private String sub;
    @JsonProperty("preferred_username")
    private String preferredUserName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("family_name")
    private String familyName;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("nonce")
    private String nonce;

    public String getAcr() {
        return acr;
    }

    public JWT acr(String acr) {
        this.acr = acr;
        return this;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public JWT authTime(Date authTime) {
        this.authTime = authTime;
        return this;
    }

    public String getSub() {
        return sub;
    }

    public JWT sub(String sub) {
        this.sub = sub;
        return this;
    }

    public String getPreferredUserName() {
        return preferredUserName;
    }

    public JWT preferredUserName(String preferredUserName) {
        this.preferredUserName = preferredUserName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public JWT email(String email) {
        this.email = email;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public JWT familyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public JWT givenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public JWT nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String getName() {
        return name;
    }

    public JWT name(String name) {
        this.name = name;
        return this;
    }
}
