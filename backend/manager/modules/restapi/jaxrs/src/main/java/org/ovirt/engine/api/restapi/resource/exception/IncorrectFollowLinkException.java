package org.ovirt.engine.api.restapi.resource.exception;

public class IncorrectFollowLinkException extends RuntimeException {

    private final String link;
    private final String entityName;

    public IncorrectFollowLinkException(String link, String entityName, Throwable cause) {
        super(cause);
        this.entityName = entityName;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getEntityName() {
        return entityName;
    }
}
