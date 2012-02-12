package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class TaskFirstRowModelProvider extends TaskModelProvider {

    @Inject
    public TaskFirstRowModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected void updateDataProvider(List<Job> items) {
        List<Job> firstRowData = items.isEmpty() ? items : Arrays.asList(items.get(0));
        super.updateDataProvider(firstRowData);
    }

}
