package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.action.*;
import java.io.*;

//TODO: Is async_tasks required for UI or can stay empty to avoid GWT errors?
public class async_tasks {
	public async_tasks() {
	}

	public async_tasks(VdcActionType action_type, AsyncTaskResultEnum result, AsyncTaskStatusEnum status, Guid task_id,
			VdcActionParametersBase action_parameters) {
		this.action_typeField = action_type;
		this.resultField = result;
		this.statusField = status;
		this.task_idField = task_id;
		this.setaction_parameters(action_parameters);
	}

	private VdcActionType action_typeField = VdcActionType.forValue(0);

	public VdcActionType getaction_type() {
		return this.action_typeField;
	}

	public void setaction_type(VdcActionType value) {
		this.action_typeField = value;
	}

	private AsyncTaskResultEnum resultField = AsyncTaskResultEnum.forValue(0);

	public AsyncTaskResultEnum getresult() {
		return this.resultField;
	}

	public void setresult(AsyncTaskResultEnum value) {
		this.resultField = value;
	}

	private AsyncTaskStatusEnum statusField = AsyncTaskStatusEnum.forValue(0);

	public AsyncTaskStatusEnum getstatus() {
		return this.statusField;
	}

	public void setstatus(AsyncTaskStatusEnum value) {
		this.statusField = value;
	}

	private Guid task_idField = new Guid();

	public Guid gettask_id() {
		return this.task_idField;
	}

	public void settask_id(Guid value) {
		this.task_idField = value;
	}

	private VdcActionParametersBase action_parametersField;

	public VdcActionParametersBase getaction_parameters() {
		return this.action_parametersField;
	}

	public void setaction_parameters(VdcActionParametersBase value) {
		this.action_parametersField = value;
	}

	// used to be toSerializedForm
	public byte[] getSerializedForm() {
		return null;
	}

	// used to be fromSerializedForm
	public void setSerializedForm(Object stream) {

	}
}
