package org.ovirt.engine.core.bll.host.provider.foreman;

import org.ovirt.engine.core.compat.Guid;

public class ContentHostIdentifier {
    private final String name;
    private final String fqdn;
    private final String id;

    private ContentHostIdentifier(String id, String fqdn, String name) {
        this.id = id;
        this.fqdn = fqdn;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getFqdn() {
        return fqdn;
    }

    public String getId() {
        return id;
    }

    public static class Builder {
        private String id;
        private String fqdn;
        private String name;

        @SuppressWarnings("unused")
        private Builder() {
            // private
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFqdn(String fqdn) {
            this.fqdn = fqdn;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withId(Guid id) {
            return withId(id.toString());
        }

        public ContentHostIdentifier build() {
            return new ContentHostIdentifier(id, fqdn, name);
        }
    }

}
