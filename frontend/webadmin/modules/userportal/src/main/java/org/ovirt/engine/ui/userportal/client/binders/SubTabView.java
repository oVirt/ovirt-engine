package org.ovirt.engine.ui.userportal.client.binders;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.SetAdGroupRoleParameters;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.ListModel;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommon.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommon.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommon.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.EntityModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelDualGridBinder;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelWithActionsBinder;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ModelToViewerBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.PermissionListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.PoolDiskListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.PoolGeneralModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.PoolInterfaceListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.TemplateDiskListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.TemplateEventListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.TemplateGeneralModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.TemplateInterfaceListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmAppListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmDiskListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmEventListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmGeneralModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmInterfaceListModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmMonitorModelBinder;
import org.ovirt.engine.ui.userportal.client.binders.specific.VmSnapshotListModelBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.MonitorBarsViewer;
import org.ovirt.engine.ui.userportal.client.components.SubTabDetailViewer;
import org.ovirt.engine.ui.userportal.client.components.SubTabDualGrid;
import org.ovirt.engine.ui.userportal.client.components.SubTabGrid;
import org.ovirt.engine.ui.userportal.client.components.SubTabGridWithToolbar;
import org.ovirt.engine.ui.userportal.client.components.ToolBar;
import org.ovirt.engine.ui.userportal.client.components.ToolBarButton;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;

public class SubTabView {
	
	private Canvas layout; 
	private ModelToViewerBinder binder;
	private GridController gridController;

	public SubTabView(final EntityModel model, GridController gridController){
		this.gridController = gridController;
		
		binder = getBinderType(model);
		
		layout = getCanvasType(model);

		setEventListeners(model);
	}	
	
	public Canvas getLayout() {
		return layout;
	}

	private Canvas getCanvasType(EntityModel model) {
		Canvas layout = null;

		switch (binder.getRendererType()) {
		case Grid:
			layout = new SubTabGrid();
			break;
		case DetailViewer:
			EntityModelBinder emb = (EntityModelBinder)binder;
			layout = new SubTabDetailViewer(emb.getFields(), emb.getNumOfRowsInColumn());
			break;
		case GridWithToolbar:
			layout = new SubTabGridWithToolbar((ListModel)model);
			setToolbarActions((SubTabGridWithToolbar)layout);
			break;
		case MonitorBarsViewer:
			layout = new MonitorBarsViewer();
			break;
		case DualGrid:
			layout = new SubTabDualGrid((ListModel)model);
			setToolbarActions((SubTabDualGrid)layout);
			break;			
		default:
			throw new RuntimeException("No corresponding layout was found for the model " + model.getClass().getName());
		}
		
		return layout;
	}
	
	private ModelToViewerBinder getBinderType(Model model) {
		ModelToViewerBinder binder = bindersMap.get(model.getClass());
		if (binder == null) {
			throw new RuntimeException("A corresponding binder was not found for the model " + model.getClass().getName());
		}
		binder.setModel(model);
		return binder;
	}
	
	private void setEventListeners(final EntityModel model) {
		if (model instanceof ListModel) {
			((SubTabGrid)layout).setFields(((ListModelBinder)binder).getFields());
			
			if (binder.getRendererType().equals(RendererType.DualGrid))
				((SubTabDualGrid)layout).setAdditionalFields(((ListModelDualGridBinder)binder).getAdditionalFields());
			
			((ListModel)model).getItemsChangedEvent().addListener(new IEventListener() {
				Boolean isObservable = null;
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					if (isObservable == null) {
						if (((ListModel)model).getItems() instanceof ObservableCollection) {
							isObservable = true;
						}
						else 
							isObservable = false;
					}
					
					// Safety measure for cases where the same binder is used for different instances of the same model, we should make sure that the binder uses the correct one
					binder.setModel(model);
					
					if (isObservable) {
						ObservableCollection l = (ObservableCollection)((ListModel)model).getItems();
						if (l == null)
							return;
						
						l.getCollectionChangedEvent().addListener(new IEventListener() {
							@Override
							public void eventRaised(Event ev, Object sender, EventArgs args) {
								((SubTabGrid)layout).setData(binder.calcRecords());
							}
						});
					}
					
					if (!isObservable) {
						((SubTabGrid)layout).setData(binder.calcRecords());
					}
				}
			});

			if (binder instanceof ListModelDualGridBinder) {
				model.getPropertyChangedEvent().addListener(new IEventListener() {
					@Override
					public void eventRaised(Event ev, Object sender, EventArgs args) {
						if (((PropertyChangedEventArgs)args).PropertyName.equals(((ListModelDualGridBinder)binder).getPropertyName())) {
							((SubTabDualGrid)layout).setAdditionalData(((ListModelDualGridBinder)binder).calcAdditionalRecords());
						}
					}
				});
			}
			return;
		}

		if (binder.getRendererType().equals(RendererType.DetailViewer)) {
			IEventListener detailViewerDataSetEvent = new IEventListener() {
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					if (model.getEntity() != null)
						((SubTabDetailViewer)layout).setData(binder.calcRecords());
				}
			};
			
			// Unfortunately the update complete event is not a member of the superclass of pool/vm general model, so we have to cast the model accordingly to add a listener
			if (model instanceof VmGeneralModel)
				((VmGeneralModel)model).getUpdateCompleteEvent().addListener(detailViewerDataSetEvent);
			else if (model instanceof PoolGeneralModel)
				((PoolGeneralModel)model).getUpdateCompleteEvent().addListener(detailViewerDataSetEvent);
			else {
				model.getEntityChangedEvent().addListener(new IEventListener() {
					@Override
					public void eventRaised(Event ev, Object sender, EventArgs args) {
						if (model.getEntity() != null)
							((SubTabDetailViewer)layout).setData(binder.calcRecords());
					}
				});
			}

			return;
		};

		if (binder.getRendererType().equals(RendererType.MonitorBarsViewer)) {
			model.getPropertyChangedEvent().addListener(new IEventListener() {
				@Override 
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					String propertyName = ((PropertyChangedEventArgs)args).PropertyName;

					if (propertyName.equals("Entity")) {
						if (model.getEntity() != null)
							((VmMonitorModelBinder)binder).startRefreshTimer();
						else
							((VmMonitorModelBinder)binder).cancelRefreshTimer();
					}
					
					if (propertyName.equals("CpuUsage") || propertyName.equals("MemoryUsage") || propertyName.equals("NetworkUsage")) {
						((MonitorBarsViewer)layout).setData(binder.calcRecords());
					}
				}
			});
			return;
		};
	}
	
	private void setToolbarActions(SubTabGridWithToolbar layout) {
		ToolBar toolBar = ((SubTabGridWithToolbar)layout).getToolbar();
		ToolbarAction[] actions = ((ListModelWithActionsBinder)binder).getCommands(gridController);
		for (int i=0; i<actions.length; i++) {
			final UICommand command = actions[i].getUiCommand();
			final ToolBarButton button = new ToolBarButton(command.getName());
			button.setDisabled(!command.getIsExecutionAllowed());
			command.getPropertyChangedEvent().addListener(new IEventListener() {
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					PropertyChangedEventArgs pargs = (PropertyChangedEventArgs)args;
					if (pargs.PropertyName.equals("IsExecutionAllowed"))
						button.setDisabled(!command.getIsExecutionAllowed());
				}
			});
			
			if (actions[i].getOnClickAction() != null)
				button.addClickHandler(actions[i].getOnClickAction());
			toolBar.addButton(button);	
		}
	}
	
	@SuppressWarnings("serial")
	static private Map<Class<? extends EntityModel>, ModelToViewerBinder> bindersMap = new HashMap<Class<? extends EntityModel>, ModelToViewerBinder>() {{
		put(TemplateInterfaceListModel.class, new TemplateInterfaceListModelBinder());
		put(TemplateDiskListModel.class, new TemplateDiskListModelBinder());
		put(TemplateEventListModel.class, new TemplateEventListModelBinder());
		put(PermissionListModel.class, new PermissionListModelBinder());
		put(TemplateGeneralModel.class, new TemplateGeneralModelBinder());
		put(VmGeneralModel.class, new VmGeneralModelBinder());
		put(PoolGeneralModel.class, new PoolGeneralModelBinder());
		put(PoolDiskListModel.class, new PoolDiskListModelBinder());
		put(PoolInterfaceListModel.class, new PoolInterfaceListModelBinder());
		put(VmDiskListModel.class, new VmDiskListModelBinder());
		put(VmInterfaceListModel.class, new VmInterfaceListModelBinder());
		put(VmEventListModel.class, new VmEventListModelBinder());
		put(VmAppListModel.class, new VmAppListModelBinder());
		put(VmMonitorModel.class, new VmMonitorModelBinder());
		put(VmSnapshotListModel.class, new VmSnapshotListModelBinder());
	}};
}
