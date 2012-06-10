package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;

//using VdcDAL.AdBroker;

public interface IImage extends BusinessEntity<Guid>{
    java.util.Date getcreation_date();

    void setcreation_date(java.util.Date value);

    // DateTime lastmodified_date
    // {
    // get;
    // set;
    // }
    long getsize();

    void setsize(long value);

    String getdescription();

    void setdescription(String value);

    Guid getit_guid();

    void setit_guid(Guid value);

    int getread_rate_kb_per_sec();

    void setread_rate_kb_per_sec(int value);

    int getwrite_rate_kb_per_sec();

    void setwrite_rate_kb_per_sec(int value);
}
