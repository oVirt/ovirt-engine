package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.HtmlParameters;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.i18n.client.LocaleInfo;

public class ReportModel extends EntityModel {

    private final HtmlParameters paramsMap = new HtmlParameters();
    private boolean differntDcError = false;
    private final String reportUrl;

    public ReportModel(String baseUrl, String ssoToken) {
        reportUrl = FrontendUrlUtils.stripParameters(baseUrl);
        paramsMap.parseUrlParams(baseUrl);
        paramsMap.setParameter("sessionID", ssoToken); //$NON-NLS-1$
        paramsMap.setParameter("active_hosts_select", //$NON-NLS-1$
               "AND+delete_date+IS+NULL"); //$NON-NLS-1$

        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        paramsMap.setParameter("userLocale", currentLocale.equals("default") ? "en_US" : currentLocale); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public Map<String, List<String>> getReportParams() {
        return paramsMap.getParameters();
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setDataCenterID(final String uuid) {
        paramsMap.setParameter("P_DataCenter_ID", uuid); //$NON-NLS-1$
    }

    public void addDataCenterID(final String uuid) {
        paramsMap.addParameter("P_DataCenter_ID", uuid); //$NON-NLS-1$
    }

    public void setPassword(String password) {
        paramsMap.setParameter("j_password", password); //$NON-NLS-1$
    }

    public void setReportEndDate(String date) {
        paramsMap.setParameter("P_End_Date", date); //$NON-NLS-1$
    }

    public void setReportStartDate(String date) {
        paramsMap.setParameter("P_Start_Date", date); //$NON-NLS-1$
    }

    public void setUser(String user) {
        paramsMap.setParameter("j_username", user); //$NON-NLS-1$
    }

    public void setReportUnit(String uri) {
        paramsMap.setParameter("reportUnit", uri); //$NON-NLS-1$
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
