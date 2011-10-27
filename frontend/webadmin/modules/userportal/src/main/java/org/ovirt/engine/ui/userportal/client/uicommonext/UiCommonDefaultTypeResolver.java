package org.ovirt.engine.ui.userportal.client.uicommonext;

import org.ovirt.engine.ui.uicommon.Configurator;
import org.ovirt.engine.ui.uicommon.ILogger;
import org.ovirt.engine.ui.uicommon.ITimer;
import org.ovirt.engine.ui.uicommon.ITypeResolver;
import org.ovirt.engine.ui.uicommon.models.vms.IRdp;
import org.ovirt.engine.ui.uicommon.models.vms.ISpice;

public class UiCommonDefaultTypeResolver implements ITypeResolver { 
    
    @Override
	public Object Resolve(Class type) {
		if (type == Configurator.class) {
		    return UserPortalConfigurator.getInstance();
		} else if (type == ISpice.class) {
			return new SpiceInterfaceImpl();
		} else if (type == IRdp.class) {
			return new RDPInterfaceImpl();
		} else if (type == ILogger.class) {
			return new LoggerImpl();
		}  else if (type == ITimer.class) {
			return new TimerImpl();
		}
		return null;
		//throw new RuntimeException("Cannot resolve type: " + type);
	}
}
