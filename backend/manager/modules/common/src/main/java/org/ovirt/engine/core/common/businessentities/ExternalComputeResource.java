package org.ovirt.engine.core.common.businessentities;

public class ExternalComputeResource implements ExternalEntityBase {
    private static final long serialVersionUID = -6951116030464852526L;
    private String name;
    private int id;
    private String url;
    private String provider;
    private String user;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return "URL: " + (url != null ? url : "[N/A]") + "\n" +
                " | Provider: " + (provider != null ? provider : "[N/A]") + "\n" +
                " | User: " + (user != null ? user : "[N/A]");
    }
}
