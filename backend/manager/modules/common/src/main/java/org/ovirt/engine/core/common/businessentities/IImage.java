package org.ovirt.engine.core.common.businessentities;

import java.util.Date;

import org.ovirt.engine.core.compat.Guid;

public interface IImage extends BusinessEntity<Guid>{
    Date getCreationDate();

    void setCreationDate(Date creationDate);

    long getSize();

    void setSize(long value);

    String getDescription();

    void setDescription(String description);

    Guid getImageTemplateId();

    void setImageTemplateId(Guid value);

    int getReadRateKbPerSec();

    void setReadRateKbPerSec(int readRate);

    int getWriteRateKbPerSec();

    void setWriteRateKbPerSec(int writeRate);
}
