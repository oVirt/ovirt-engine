package org.ovirt.engine.core.sso.utils.jwt;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.resteasy.jwt.JsonWebToken;

public class OpenIdJWT extends JsonWebToken {
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

    public OpenIdJWT acr(String acr) {
        this.acr = acr;
        return this;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public OpenIdJWT authTime(Date authTime) {
        this.authTime = authTime;
        return this;
    }

    public String getSub() {
        return sub;
    }

    public OpenIdJWT sub(String sub) {
        this.sub = sub;
        return this;
    }

    public String getPreferredUserName() {
        return preferredUserName;
    }

    public OpenIdJWT preferredUserName(String preferredUserName) {
        this.preferredUserName = preferredUserName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public OpenIdJWT email(String email) {
        this.email = email;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public OpenIdJWT familyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public OpenIdJWT givenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public OpenIdJWT nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpenIdJWT name(String name) {
        this.name = name;
        return this;
    }
}
