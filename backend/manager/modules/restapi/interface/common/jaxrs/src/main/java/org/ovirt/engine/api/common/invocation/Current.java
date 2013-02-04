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

package org.ovirt.engine.api.common.invocation;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
public class Current implements PostProcessInterceptor {

    private static ThreadLocal<Map<Class<?>, Object>> currents = new ThreadLocal<Map<Class<?>, Object>>();

    @Override
    public void postProcess(ServerResponse response) {
        currents.set(null);
    }

    public <T> void set(T current) {
        getMap().put(current.getClass(), current);
    }

    public <T> T get(Class<T> clz) {
        return clz.cast(getMap().get(clz));
    }

    private Map<Class<?>, Object> getMap() {
        if (currents.get() == null) {
            currents.set(new HashMap<Class<?>, Object>(){{put(MetaData.class, new MetaData());}});
        }
        return currents.get();
    }
}
