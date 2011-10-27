package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
public class AsyncQueryResults {
    private Guid[] queryIDs;
    private ListIVdcQueryableUpdatedDataValues[] queryData;

    public AsyncQueryResults() {
    }

    public AsyncQueryResults(KeyValuePairCompat<Guid, ListIVdcQueryableUpdatedData[]>[] results) {
        queryIDs = new Guid[results.length];
        queryData = new ListIVdcQueryableUpdatedDataValues[results.length];
        for (int i = 0; i < results.length; i++) {
            queryIDs[i] = results[i].getKey();
            queryData[i] = new ListIVdcQueryableUpdatedDataValues(results[i].getValue());
        }
    }

    @XmlElement(name = "QueryIDsGuidArray")
    public Guid[] getQueryIDs() {
        return queryIDs;
    }

    public void setQueryIDs(Guid[] queryIDs) {
        this.queryIDs = queryIDs;
    }

    @XmlElement
    public ListIVdcQueryableUpdatedDataValues[] getQueryData() {
        return queryData;
    }

    public void setQueryData(ListIVdcQueryableUpdatedDataValues[] queryData) {
        this.queryData = queryData;
    }
}
