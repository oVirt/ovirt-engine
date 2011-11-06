package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
public class AsyncQueryResults {
    private ArrayList<Guid> queryIDs;
    private ArrayList<ListIVdcQueryableUpdatedDataValues> queryData;

    public AsyncQueryResults() {
    }

    public AsyncQueryResults(KeyValuePairCompat<Guid, ArrayList<ListIVdcQueryableUpdatedData>>[] results) {
        queryIDs = new ArrayList<Guid>(results.length);
        queryData = new ArrayList<ListIVdcQueryableUpdatedDataValues>(results.length);
        for (int i = 0; i < results.length; i++) {
            queryIDs.add(i, results[i].getKey());
            queryData.add(i, new ListIVdcQueryableUpdatedDataValues(results[i].getValue()));
        }
    }

    @XmlElement(name = "QueryIDsGuidArray")
    public ArrayList<Guid> getQueryIDs() {
        return queryIDs;
    }

    public void setQueryIDs(ArrayList<Guid> queryIDs) {
        this.queryIDs = queryIDs;
    }

    @XmlElement
    public ArrayList<ListIVdcQueryableUpdatedDataValues> getQueryData() {
        return queryData;
    }

    public void setQueryData(ArrayList<ListIVdcQueryableUpdatedDataValues> queryData) {
        this.queryData = queryData;
    }
}
