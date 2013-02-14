package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

//using VdcDAL.AdBroker;

public interface IImage extends BusinessEntity<Guid>{
    java.util.Date getCreationDate();

    void setCreationDate(java.util.Date creationDate);

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
