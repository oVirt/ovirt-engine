package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public interface ImageType extends BusinessEntity<Guid>, Nameable {

    void setName(String value);

    String getDescription();

    void setDescription(String value);

    int getOsId();

    void setOsId(int value);

    String getIsoPath();

    void setIsoPath(String value);

    String getKernelUrl();

    void setKernelUrl(String value);

    String getKernelParams();

    void setKernelParams(String value);

    String getInitrdUrl();

    void setInitrdUrl(String value);
}
