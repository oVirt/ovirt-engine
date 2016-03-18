package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GuestContainer implements Serializable {
    private static final long serialVersionUID = -3300100010514970647L;
    private String id;
    private List<String> names;
    private String image;
    private String command;
    private String status;

    public GuestContainer() {
    }

    public GuestContainer(String id, List<String> names, String image, String command, String status) {
        this.id = id;
        this.names = names;
        this.image = image;
        this.command = command;
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GuestContainer)) {
            return false;
        }
        GuestContainer other = (GuestContainer) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(names, other.names)
                && Objects.equals(image, other.image)
                && Objects.equals(command, other.command)
                && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                names,
                image,
                command,
                status
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
