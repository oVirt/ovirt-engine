package org.ovirt.engine.ui.uicompat;

public class TaskContext {

//	private Dispatcher dispatcher;
	private Object privateState;
	public Object getState()
	{
		return privateState;
	}
	private void setState(Object value)
	{
		privateState = value;
	}

	public TaskContext(Dispatcher dispatcher, Object state)
	{
//		this.dispatcher = dispatcher;
		setState(state);
	}

	public void InvokeUIThread(ITaskTarget target, Object state)
	{
		setState(state);
		target.run(this);
	}
}
