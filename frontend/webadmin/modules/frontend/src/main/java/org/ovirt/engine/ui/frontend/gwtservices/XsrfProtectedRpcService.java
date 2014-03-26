package org.ovirt.engine.ui.frontend.gwtservices;

import com.google.gwt.rpc.client.RpcService;
import com.google.gwt.user.server.rpc.XsrfProtect;

@XsrfProtect
public interface XsrfProtectedRpcService extends RpcService {
}
