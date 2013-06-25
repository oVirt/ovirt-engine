package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TaskFirstRowModelProvider extends TaskModelProvider {

    private static Job emptyJob = new Job();
    private static List<Job> emptyJobList;

    @Inject
    public TaskFirstRowModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            ApplicationConstants constants) {
        super(eventBus, defaultConfirmPopupProvider);
        emptyJob.setDescription(constants.emptyJobMessage());
        emptyJobList = Arrays.asList(emptyJob);
    }

    @Override
    protected void updateDataProvider(List<Job> items) {
        List<Job> firstRowData = items.isEmpty() ? emptyJobList : Arrays.asList(items.get(0));
        super.updateDataProvider(firstRowData);
    }

}
