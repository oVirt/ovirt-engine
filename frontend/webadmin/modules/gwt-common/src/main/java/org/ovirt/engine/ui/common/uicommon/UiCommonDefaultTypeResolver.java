package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITimer;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdp;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVnc;

import com.google.inject.Inject;

public class UiCommonDefaultTypeResolver implements ITypeResolver {

    private final Configurator configurator;
    private final ILogger logger;

    @Inject
    public UiCommonDefaultTypeResolver(Configurator configurator, ILogger logger) {
        this.configurator = configurator;
        this.logger = logger;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object Resolve(Class type) {
        if (type == Configurator.class) {
            return configurator;
        } else if (type == ILogger.class) {
            return logger;
        } else if (type == ITimer.class) {
            return new TimerImpl();
        } else if (type == ISpice.class) {
            return new SpiceInterfaceImpl();
        } else if (type == IVnc.class) {
            return new IVncImpl();
        } else if (type == IRdp.class) {
            return new IRdpImpl();
        }

        throw new RuntimeException("UiCommon Resolver cannot resolve type: " + type); //$NON-NLS-1$
    }

}
