/*
* Copyright © 2010 Red Hat, Inc.
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

package org.ovirt.engine.api.restapi.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.ovirt.engine.api.common.util.ReflectionHelper;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Body;
import org.ovirt.engine.api.model.HttpMethod;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.RSDL;
import org.ovirt.engine.api.model.Request;
import org.ovirt.engine.api.model.Response;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public class RsdlBuilder {

    private RSDL rsdl;
    private String entryPoint;
    private BackendApiResource apiResource;
    private String href;
    private String description;

    private static final String ACTION = "Action";
    private static final String DELETE = "delete";
    private static final String UPDATE = "update";
    private static final String GET = "get";
    private static final String ADD = "add";

    protected static final LogCompat LOG = LogFactoryCompat.getLog(RsdlBuilder.class);

    private static final String RESOURCES_PACKAGE = "org.ovirt.engine.api.resource";

    public RsdlBuilder(BackendApiResource apiResource) {
        this.apiResource = apiResource;
        this.entryPoint = apiResource.getUriInfo().getBaseUri().getPath();
    }

    private RSDL construct() throws ClassNotFoundException, IOException {
        RSDL rsdl = new RSDL();
        for (Link link : getLinks()) {
            rsdl.getLinks().add(link);
        }
        return rsdl;
    }

    public RSDL build() {
        try {
            rsdl = construct();
            rsdl.setHref(getHref());
            rsdl.setDescription(getDescription());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("RSDL generation failure.", e);
        }
        return rsdl;
    }

    public RsdlBuilder href(String href) {
        this.href = href;
        return this;
    }

    public RsdlBuilder description(String description) {
        this.description = description;
        return this;
    }

    public String getHref() {
        return this.href;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
            return "RSDL Href: " + getHref() +
                               ", Description:" + getDescription() +
                               ", Links: " + (rsdl != null ? rsdl.getLinks().size() : "0") + ".";
    }

    public static class LinkBuilder {
        private Link link = new Link();;
        public LinkBuilder url(String url) {
            link.setHref(url);
            return this;
        }
        public LinkBuilder rel(String rel) {
            link.setRel(rel);
            return this;
        }
        public LinkBuilder requestParameter(final String requestParameter) {
            link.setRequest(new Request());
            link.getRequest().setBody(new Body(){{setType(requestParameter);}});
            return this;
        }
        public LinkBuilder responseType(final String responseType) {
            link.setResponse(new Response(){{setType(responseType);}});
            return this;
        }
        public LinkBuilder httpMethod(HttpMethod httpMethod) {
            if(!link.isSetRequest()) {
                link.setRequest(new Request());
            }
            link.getRequest().setHttpMethod(httpMethod);
            return this;
        }
        public Link build() {
            return link;
        }
    }

    public static class ParametersBuilder {

    }

    public Collection<Link> getLinks() throws ClassNotFoundException, IOException {
        //SortedSet<Link> results = new TreeSet<Link>();
        List<Link> results = new ArrayList<Link>();
        List<Class<?>> classes = ReflectionHelper.getClasses(RESOURCES_PACKAGE);
        for (String path : apiResource.getRels()) {
            Class<?> resource = findResource(path, classes);
            results.addAll(describe(resource, entryPoint +  path, new HashMap<String, Type>()));
        }
        return results;
    }

    private static Class<?> findResource(String path, List<Class<?>> classes) throws ClassNotFoundException, IOException {
        path = "/" + path;
        for (Class<?> clazz : classes) {
            if (path.equals(getPath(clazz))) {
                return clazz;
            }
        }
        return null;
    }

    private static String getPath(Class<?> clazz) {
        Path pathAnnotation = clazz.getAnnotation(Path.class);
        return pathAnnotation==null ? null : pathAnnotation.value();
    }

    public static List<Link> describe(Class<?> resource, String prefix, Map<String, Type> parametersMap) throws ClassNotFoundException {
        //SortedSet<Link> results = new TreeSet<Link>();
        List<Link> results = new ArrayList<Link>();
        if (resource!=null) {
            for (Method m : resource.getMethods()) {
                handleMethod(prefix, results, m, resource, parametersMap);
            }
        }
        return results;
    }

    private static void addToGenericParamsMap (Class<?> resource, Type[] paramTypes, Type[] genericParamTypes, Map<String, Type> parametersMap) {
        for (int i=0; i<genericParamTypes.length; i++) {
            if (paramTypes[i].toString().length() == 1) {
                //if the parameter type is generic - don't add to map, as it might override a more meaningful value:
                //for example, without this check we could replace <"R", "Template"> with <"R", "R">, and lose information.
            } else {
                //if the length is greater than 1, we have an actual type (e.g: "CdRoms"), and we want to add it to the
                //map, even if it overrides an existing value.
                parametersMap.put(genericParamTypes[i].toString(), paramTypes[i]);
            }
        }
    }

    private static void handleMethod(String prefix, Collection<Link> results, Method m, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
        if (isRequiresDescription(m)) {
            Class<?> returnType = findReturnType(m, resource, parametersMap);
            String returnTypeStr = getReturnTypeStr(returnType);
            if (m.isAnnotationPresent(javax.ws.rs.GET.class)) {
                handleGet(prefix, results, returnTypeStr);
            } else if (m.isAnnotationPresent(PUT.class)) {
                handlePut(prefix, results, returnTypeStr);
            } else if (m.isAnnotationPresent(javax.ws.rs.DELETE.class)) {
                handleDelete(prefix, results, m);
            } else if (m.isAnnotationPresent(Path.class)) {
                String path = m.getAnnotation(Path.class).value();
                if (isAction(m)) {
                    handleAction(prefix, results, returnTypeStr, path);
                } else {
                    if (isSingleEntityResource(m)) {
                        path = "{" + getSingleForm(prefix) + ":id}";
                    }
                    if (m.getGenericReturnType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)m.getGenericReturnType();
                        addToGenericParamsMap(resource, parameterizedType.getActualTypeArguments(), m.getReturnType().getTypeParameters(), parametersMap);
                    }
                    results.addAll(describe(returnType, prefix + "/" + path, new HashMap<String, Type>(parametersMap)));
                }
            } else {
                if (m.getName().equals(ADD)) {
                    handleAdd(prefix, results, m);
                }
            }
        }
    }

    private static void handleAction(String prefix, Collection<Link> results, String returnValueStr, String path) {
        results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/" + path).rel(path).requestParameter(ACTION).responseType(returnValueStr).httpMethod(HttpMethod.POST).build());
    }

    private static void handleDelete(String prefix, Collection<Link> results, Method m) {
        if (m.getParameterTypes().length>1) {
            Class<?>[] parameterTypes = m.getParameterTypes();
            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            for (int i=0; i<parameterTypes.length; i++) {
                //ignore the id parameter (string), that's annotated with @PathParam
                if (!( parameterTypes[i].equals(String.class) && (!(parameterAnnotations[i].length==0)))) {
                    results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/{" + getSingleForm(prefix) + ":id}").rel(DELETE).requestParameter(parameterTypes[i].getSimpleName()).httpMethod(HttpMethod.DELETE).build());
                    return; //we can break, because we excpect only one parameter.
                }
            }
        } else {
            results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/{" + getSingleForm(prefix) + ":id}").rel(DELETE).httpMethod(HttpMethod.DELETE).build());
        }
    }

    private static void handlePut(String prefix, Collection<Link> results, String returnValueStr) {
        results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(UPDATE).requestParameter(returnValueStr).responseType(returnValueStr).httpMethod(HttpMethod.PUT).build());
    }

    private static void handleGet(String prefix, Collection<Link> results, String returnValueStr) {
        results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(GET).responseType(returnValueStr).httpMethod(HttpMethod.GET).build());
    }

    private static void handleAdd(String prefix, Collection<Link> results, Method m) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        assert(parameterTypes.length==1);
        String s = parameterTypes[0].getSimpleName();
        s = handleExcpetionalCases(s, prefix); //TODO: refactor to a more generic solution
        results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(ADD).requestParameter(s).responseType(s).httpMethod(HttpMethod.POST).build());
    }

    private static String handleExcpetionalCases(String s, String prefix) {
        if (s.equals("BaseDevice")) {
            if (prefix.contains("cdroms")) {
                return "CdRom";
            }
            if (prefix.contains("nics")) {
                return "NIC";
            }
            if (prefix.contains("disks")) {
                return "Disk";
            }
        }
        return s;
    }

    /**
     * get the class name, without package prefix
     * @param returnValue
     * @return
     */
    private static String getReturnTypeStr(Class<?> returnValue) {
        int lastIndexOf = returnValue.getSimpleName().lastIndexOf(".");
        String entityType = lastIndexOf==-1 ? returnValue.getSimpleName() : returnValue.getSimpleName().substring(lastIndexOf);
        return entityType;
    }

    private static Class<?> findReturnType(Method m, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
        for (Type superInterface : resource.getGenericInterfaces()) {
            if (superInterface instanceof ParameterizedType) {
                ParameterizedType p = (ParameterizedType)superInterface;
                Class<?> clazz = Class.forName(p.getRawType().toString().substring(p.getRawType().toString().lastIndexOf(' ')+1));
                Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();
                for (int i=0; i<p.getActualTypeArguments().length; i++) {
                    if (!map.containsKey(clazz.getTypeParameters()[i])) {
                        map.put(clazz.getTypeParameters()[i], p.getActualTypeArguments()[i]);
                    }
                }
                if (map.containsKey(m.getGenericReturnType())) {
                    String type = map.get(m.getGenericReturnType()).toString();
                    try {
                        Class<?> returnClass = Class.forName(type.substring(type.lastIndexOf(' ')+1));
                        return returnClass;
                    } catch (ClassNotFoundException e) {
                        break;
                    }
                }
            }
        }
        if (parametersMap.containsKey(m.getGenericReturnType().toString())) {
            try {
                Type type = parametersMap.get(m.getGenericReturnType().toString());
                Class<?> returnClass = Class.forName(type.toString().substring(type.toString().indexOf(' ') +1));
                return returnClass;
            } catch (ClassNotFoundException e) {
                return m.getReturnType();
            }
        } else {
            return m.getReturnType();
        }
    }

    private static boolean isSingleEntityResource(Method m) {
        Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        for (int i=0; i<parameterAnnotations.length; i++) {
            for (int j=0; j<parameterAnnotations[j].length; j++) {
                if (parameterAnnotations[i][j].annotationType().equals(PathParam.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAction(Method m) {
        return m.isAnnotationPresent(Actionable.class);
    }


    private static boolean isRequiresDescription(Method m) {
        boolean pathRelevant = !(m.isAnnotationPresent(Path.class) && m.getAnnotation(Path.class).value().contains(":"));
        boolean returnValueRelevant = !m.getReturnType().equals(CreationResource.class);
        return pathRelevant && returnValueRelevant;
    }

    //might need to truncate the plural 's', for example:
    //for "{api}/hosts/{host:id}/nics" return "nic"
    //but for "{api}/hosts/{host:id}/storage" return "storage" (don't truncate last character)
    private static String getSingleForm(String prefix) {
        int startIndex = prefix.lastIndexOf('/')+1;
        int endPos = prefix.endsWith("s") ?  prefix.length() -1 : prefix.length();
        return prefix.substring(startIndex, endPos);
    }
}
