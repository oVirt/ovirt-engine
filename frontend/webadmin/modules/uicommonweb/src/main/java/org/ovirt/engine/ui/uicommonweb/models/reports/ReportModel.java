package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.http.client.UrlBuilder;

public class ReportModel extends EntityModel {

    private final UrlBuilder builder;
    private final Map<String, List<String>> reportParams = new HashMap<String, List<String>>();
    private boolean differntDcError = false;

    public ReportModel(String host, int port, String context) {
        builder = new UrlBuilder();
        builder.setHost(host);
        builder.setPort(port);
        builder.setPath(context + "/flow.html");
        reportParams.put("_flowId", new LinkedList<String>(Collections.singletonList("viewReportFlow")));
        reportParams.put("active_hosts_select",
                new LinkedList<String>(Collections.singletonList("AND+delete_date+IS+NULL")));
    }

    public Map<String, List<String>> getReportParams() {
        return Collections.unmodifiableMap(reportParams);
    }

    public String getReportUrl() {
        return builder.buildString();
    }

    public void setDataCenterID(final String uuid) {
        reportParams.put("P_DataCenter_ID", new LinkedList<String>(Collections.singletonList(uuid)));
    }

    public void addDataCenterID(final String uuid) {
        List<String> ids = reportParams.get("P_DataCenter_ID");

        if (ids == null) {
            setDataCenterID(uuid);
        } else {
            ids.add(uuid);
        }
    }

    public void setPassword(String password) {
        reportParams.put("j_password", new LinkedList<String>(Collections.singletonList(password)));
    }

    public void setReportEndDate(Date date) {
        reportParams.put("P_End_Date", new LinkedList<String>(Collections.singletonList(dateStr(date))));
    }

    public void setReportStartDate(Date date) {
        reportParams.put("P_Start_Date", new LinkedList<String>(Collections.singletonList(dateStr(date))));
    }

    public void setUser(String user) {
        reportParams.put("j_username", new LinkedList<String>(Collections.singletonList(user)));
    }

    public void setViewAsDashboard(boolean viewAsDashboard) {
        builder.setParameter("viewAsDashboardFrame", String.valueOf(viewAsDashboard));
    }

    public void setReportUnit(String uri) {
        reportParams.put("reportUnit", new LinkedList<String>(Collections.singletonList(uri)));
    }

    public void setResourceId(String idParamName, String id) {
        reportParams.put(idParamName, new LinkedList<String>(Collections.singletonList(id)));
    }

    public void addResourceId(String idParamName, String id) {
        List<String> ids = reportParams.get(idParamName);

        if (ids == null) {
            setResourceId(idParamName, id);
        } else {
            ids.add(id);
        }
    }

    private String dateStr(Date date) {
        return date.getYear() + "-" + date.getMonth() + "-" + date.getDate();
    }

    public void setDifferntDcError(boolean differntDcError) {
        this.differntDcError = differntDcError;
    }

    public boolean isDifferntDcError() {
        return differntDcError;
    }

}
