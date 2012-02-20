package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.HtmlParameters;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class ReportModel extends EntityModel {

    private final HtmlParameters paramsMap = new HtmlParameters();
    private boolean differntDcError = false;
    private final String reportUrl;

    public ReportModel(String baseUrl) {
        reportUrl = baseUrl + "/flow.html" + "?viewAsDashboardFrame=false";
        paramsMap.setParameter("_flowId", "viewReportFlow");
        paramsMap.setParameter("active_hosts_select",
               "AND+delete_date+IS+NULL");
    }

    public Map<String, List<String>> getReportParams() {
        return paramsMap.getParameters();
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setDataCenterID(final String uuid) {
        paramsMap.setParameter("P_DataCenter_ID", uuid);
    }

    public void addDataCenterID(final String uuid) {
        paramsMap.addParameter("P_DataCenter_ID", uuid);
    }

    public void setPassword(String password) {
        paramsMap.setParameter("j_password", password);
    }

    public void setReportEndDate(String date) {
        paramsMap.setParameter("P_End_Date", date);
    }

    public void setReportStartDate(String date) {
        paramsMap.setParameter("P_Start_Date", date);
    }

    public void setUser(String user) {
        paramsMap.setParameter("j_username", user);
    }

    public void setReportUnit(String uri) {
        paramsMap.setParameter("reportUnit", uri);
    }

    public void setResourceId(String idParamName, String id) {
        paramsMap.setParameter(idParamName, id);
    }

    public void addResourceId(String idParamName, String id) {
        paramsMap.addParameter(idParamName, id);
    }

    public void setDifferntDcError(boolean differntDcError) {
        this.differntDcError = differntDcError;
    }

    public boolean isDifferntDcError() {
        return differntDcError;
    }

}
