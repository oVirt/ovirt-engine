/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.resource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.core.utils.ReapedMap;

public abstract class AbstractActionableResource<R extends BaseResource> extends AbstractUpdatableResource<R> {

    private static final long REAP_AFTER = 2 * 60 * 60 * 1000L; // 2 hours

    protected Executor executor;
    protected ReapedMap<String, ActionResource> actions;
    protected UriInfoProvider uriProvider;

    public AbstractActionableResource(String id) {
        this(id, new SimpleExecutor());
    }

    public AbstractActionableResource(String id, Executor executor) {
        super(id);
        this.executor = executor;
        actions = new ReapedMap<String, ActionResource>(REAP_AFTER);
    }

    public AbstractActionableResource(String id, Executor executor, UriInfoProvider uriProvider) {
        super(id);
        this.executor = executor;
        this.uriProvider = uriProvider;
        actions = new ReapedMap<String, ActionResource>(REAP_AFTER);
    }

    protected UriInfo getUriInfo() {
        return uriProvider.getUriInfo();
    }

    public UriInfoProvider getUriProvider() {
        return uriProvider;
    }

    protected R getModel() {
        R parent = newModel();
        parent.setId(getId());
        return parent;
    }

    /**
     * Perform an action, managing asynchrony and returning an appropriate
     * response.
     *
     * @param uriInfo  wraps the URI for the current request
     * @param action   represents the pending action
     * @param task     fulfils the action
     * @return
     */
    protected Response doAction(UriInfo uriInfo, final AbstractActionTask task) {
        Action action = task.action;
        Response.Status status = null;
        final ActionResource actionResource = new BaseActionResource<R>(uriInfo, task.action, getModel());
        if (action.isSetAsync() && action.isAsync()) {
            action.setStatus(StatusUtils.create(CreationStatus.PENDING));
            actions.put(action.getId(), actionResource);
            executor.execute(new Runnable() {
                public void run() {
                    perform(task);
                    actions.reapable(actionResource.getAction().getId());
                }
            });
            status = Status.ACCEPTED;
        } else {
            // no need for self link in action if synchronous (as no querying
            // will ever be needed)
            //
            perform(task);
            if (!action.getStatus().getState().equals(CreationStatus.FAILED.value())) {
                status = Status.OK;
            } else {
                status = Status.BAD_REQUEST;
            }
        }

        return Response.status(status).entity(action).build();
    }

    public ActionResource getActionSubresource(String action, String oid) {
        // redirect back to the target VM if action no longer cached
        // REVISIT: ultimately we should look at redirecting
        // to the event/audit log
        //
        ActionResource exists = actions.get(oid);
        return exists != null
               ? exists
               : new ActionResource() {
                    @Override
                    public Response get() {
                        R tmp = newModel();
                        tmp.setId(getId());
                        tmp = LinkHelper.addLinks(getUriInfo(), tmp);
                        Response.Status status = Response.Status.MOVED_PERMANENTLY;
                        return Response.status(status).location(URI.create(tmp.getHref())).build();
                    }
                    @Override
                    public Action getAction() {
                        return null;
                    }
                };
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    private void perform(AbstractActionTask task) {
        task.action.setStatus(StatusUtils.create(CreationStatus.IN_PROGRESS));
        if (task.action.getGracePeriod() != null) {
            try {
                Thread.sleep(task.action.getGracePeriod().getExpiry());
            } catch (Exception e) {
                // ignore
            }
        }
        task.run();
    }

    public static abstract class AbstractActionTask implements Runnable {
        protected Action action;
        protected String reason;

        public AbstractActionTask(Action action) {
            this(action, "");
        }

        public AbstractActionTask(Action action, String reason) {
            this.action = action;
            this.reason = reason;
        }

        public void run() {
            try {
                execute();
                if (!action.getStatus().getState().equals(org.ovirt.engine.api.model.CreationStatus.FAILED.value())) {
                    action.setStatus(StatusUtils.create(org.ovirt.engine.api.model.CreationStatus.COMPLETE));
                }
            } catch (Throwable t) {
                String message = t.getMessage() != null ? t.getMessage() : t.getClass().getName();
                setFault(MessageFormat.format(t.getCause() != null ? t.getCause().getMessage()
                                                                     :
                                                                     reason,
                                             message),
                         t);
            }
        }

        protected abstract void execute();

        protected void setFault(String reason, Throwable t) {
            Fault fault = new Fault();
            fault.setReason(reason);
            fault.setDetail(t.getMessage());
            action.setFault(fault);
            action.setStatus(StatusUtils.create(org.ovirt.engine.api.model.CreationStatus.FAILED));
        }

        protected static String trace(Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw, true));
            return sw.toString();
        }
    }

    protected static class DoNothingTask extends AbstractActionTask {
        public DoNothingTask(Action action) {
            super(action);
        }
        public void execute(){
        }
    }

    /**
     * Fallback executor, starts a new thread for each task.
     */
    protected static class SimpleExecutor implements Executor {
        public void execute(Runnable task) {
            new Thread(task).start();
        }
    }
}
