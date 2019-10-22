/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.rsdl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.DetailedLinks;
import org.ovirt.engine.api.model.GeneralMetadata;
import org.ovirt.engine.api.model.Header;
import org.ovirt.engine.api.model.Headers;
import org.ovirt.engine.api.model.HttpMethod;
import org.ovirt.engine.api.model.Parameter;
import org.ovirt.engine.api.model.ParametersSet;
import org.ovirt.engine.api.model.Request;
import org.ovirt.engine.api.model.Response;
import org.ovirt.engine.api.model.Rsdl;
import org.ovirt.engine.api.model.Schema;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Url;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.SystemResource;

public class RsdlBuilder {

    private static final String COLLECTION_PARAMETER_RSDL = "collection";
    private static final String COLLECTION_PARAMETER_YAML = "--COLLECTION";
    private static final String DEPRECATED_PARAMETER_YAML = "--DEPRECATED";
    private Rsdl rsdl;
    private Map<String, Action> parametersMetaData;
    private String rel;
    private String href;
    private Schema schema;
    private GeneralMetadata generalMetadata;
    private String description;
    private List<String> rels;
    private MetaData metadata;

    private static final String ACTION = "Action";
    private static final String DELETE = "delete";
    private static final String UPDATE = "update";
    private static final String GET = "get";
    private static final String ADD = "add";

    private static final String RESOURCES_PACKAGE = "org.ovirt.engine.api.resource";

    public RsdlBuilder(List<String> rels, MetaData metadata) {
        this.rels = rels;
        this.metadata = metadata;
        this.parametersMetaData = addParametersMetaData();
    }

    public Map<String, Action> addParametersMetaData() {
        parametersMetaData = new HashMap<>();
        for (Action action : metadata.getActions()) {
            parametersMetaData.put(action.getName(), action);
        }
        return parametersMetaData;
    }

    private Rsdl construct() throws ClassNotFoundException, IOException {
        Rsdl rsdl = new Rsdl();
        rsdl.setLinks(new DetailedLinks());
        for (DetailedLink link : getLinks()) {
            rsdl.getLinks().getLinks().add(link);
        }

        uniteDuplicateLinks(rsdl);

        Collections.sort(rsdl.getLinks().getLinks(),
                Comparator.comparing(DetailedLink::getHref).thenComparing(DetailedLink::getRel));

        return rsdl;
    }

    public Rsdl build() throws ClassNotFoundException, IOException {
        rsdl = construct();
        rsdl.setRel(getRel());
        rsdl.setHref(getHref());
        rsdl.setDescription(getDescription());
        rsdl.setSchema(getSchema());
        rsdl.setGeneral(getGeneralMetadata());
        return rsdl;
    }

    public RsdlBuilder rel(String rel) {
        this.rel = rel;
        return this;
    }

    public RsdlBuilder href(String href) {
        this.href = href;
        return this;
    }

    public RsdlBuilder schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public RsdlBuilder generalMetadata(GeneralMetadata entryPoint) {
        this.generalMetadata = entryPoint;
        return this;
    }

    public RsdlBuilder description(String description) {
        this.description = description;
        return this;
    }

    public String getHref() {
        return this.href;
    }

    public String getRel() {
        return this.rel;
    }

    public Schema getSchema() {
        return schema;
    }

    public GeneralMetadata getGeneralMetadata() {
        return generalMetadata;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
            return "RSDL Href: " + getHref() +
                               ", Description:" + getDescription() +
                               ", Links: " + (rsdl != null ? rsdl.isSetLinks() ? rsdl.getLinks().getLinks().size() : "0" : "0") + ".";
    }

    public class LinkBuilder {
        private DetailedLink link = new DetailedLink();
        public LinkBuilder url(String url) {
            link.setHref(url);
            return this;
        }
        public LinkBuilder description(String description) {
            link.setDescription(description);
            return this;
        }
        public LinkBuilder rel(String rel) {
            link.setRel(rel);
            return this;
        }
        public LinkBuilder requestParameter(final String requestParameter) {
            org.ovirt.engine.api.model.Body body = new org.ovirt.engine.api.model.Body();
            body.setType(requestParameter);
            Request request = new Request();
            request.setBody(body);
            link.setRequest(request);
            return this;
        }
        public LinkBuilder responseType(final String responseType) {
            Response response = new Response();
            response.setType(responseType);
            link.setResponse(response);
            return this;
        }
        public LinkBuilder httpMethod(HttpMethod httpMethod) {
            if(!link.isSetRequest()) {
                link.setRequest(new Request());
            }
            link.getRequest().setHttpMethod(httpMethod);
            return this;
        }
        public DetailedLink build() {
            if (!link.getRequest().isSetBody()) {
                link.getRequest().setBody(new org.ovirt.engine.api.model.Body());
            }
            return addParametersMetadata(link);
        }
    }

    public Collection<DetailedLink> getLinks() throws ClassNotFoundException, IOException {
        //SortedSet<Link> results = new TreeSet<Link>();
        List<DetailedLink> results = new ArrayList<>();
        for (String path : rels) {
            Class<?> resource = findResource(path);
            results.addAll(describe(resource, path, new HashMap<>()));
        }
        return results;
    }

    private Class<?> findResource(String path) throws ClassNotFoundException, IOException {
        for (Method locator : SystemResource.class.getDeclaredMethods()) {
            if (path.equals(getPath(locator))) {
                return locator.getReturnType();
            }
        }
        return null;
    }

    private String getPath(Method method) {
        Path pathAnnotation = method.getAnnotation(Path.class);
        return pathAnnotation==null ? null : pathAnnotation.value();
    }

    public List<DetailedLink> describe(Class<?> resource, String prefix, Map<String, Type> parametersMap) throws ClassNotFoundException {
        //SortedSet<Link> results = new TreeSet<Link>();
        List<DetailedLink> results = new ArrayList<>();
        if (resource!=null) {
            for (Method m : resource.getMethods()) {
                if (isConcreteReturnType(m, resource)) {
                    handleMethod(prefix, results, m, resource, parametersMap);
                }
            }
        }
        return results;
    }

    private boolean isConcreteReturnType(Method method, Class<?> resource) {
        for (Method m : resource.getMethods()) {
            if (!m.equals(method)
                    && m.getName().equals(method.getName())
                    && parameterTypesEqual(m.getParameterTypes(), method.getParameterTypes())
                    && method.getReturnType().isAssignableFrom(m.getReturnType())) {
                return false;
            }
        }
        return true;
    }

    private boolean parameterTypesEqual(Class<?>[] types1, Class<?>[] types2) {
        if (types1.length!=types2.length) {
            return false;
        } else {
            for (int i=0; i<types1.length; i++) {
                if (!(types1[i].isAssignableFrom(types2[i]) || types2[i].isAssignableFrom(types1[i]))) {
                    return false;
                }
            }
            return true;
        }
    }

    private void addToGenericParamsMap (Class<?> resource, Type[] paramTypes, Type[] genericParamTypes, Map<String, Type> parametersMap) {
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

    private void handleMethod(String prefix, Collection<DetailedLink> results, Method m, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
        if (isRequiresDescription(m)) {
            Type genericReturnType = m.getGenericReturnType();
            Class<?> concreteReturnType = findConcreteType(genericReturnType, resource, parametersMap);
            if (concreteReturnType == null) {
                concreteReturnType = m.getReturnType();
            }

            Type[] genericParameterTypes = m.getGenericParameterTypes();
            Class<?>[] concreteParameterTypes = m.getParameterTypes();
            for (int i = 0; i < concreteParameterTypes.length; i++) {
                Class<?> concreteParameterType = findConcreteType(genericParameterTypes[i], resource, parametersMap);
                if (concreteParameterType != null) {
                    concreteParameterTypes[i] = concreteParameterType;
                }
            }

            if (m.isAnnotationPresent(javax.ws.rs.GET.class)) {
                handleGet(prefix, results, concreteReturnType);
            } else if (m.isAnnotationPresent(PUT.class)) {
                handlePut(prefix, results, concreteReturnType);
            } else if (m.isAnnotationPresent(javax.ws.rs.DELETE.class)) {
                handleDelete(prefix, results, m);
            } else if (m.isAnnotationPresent(Path.class)) {
                String path = m.getAnnotation(Path.class).value();
                if (isAction(m)) {
                    handleAction(prefix, results, path, m);
                } else {
                    if (isSingleEntityResource(m)) {
                        path = "{" + getSingleForm(prefix) + ":id}";
                    }
                    if (m.getGenericReturnType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)m.getGenericReturnType();
                        addToGenericParamsMap(resource, parameterizedType.getActualTypeArguments(), m.getReturnType().getTypeParameters(), parametersMap);
                    }
                    results.addAll(describe(concreteReturnType, prefix + "/" + path, new HashMap<>(parametersMap)));
                }
            } else {
                if (m.getName().equals(ADD)) {
                    handleAdd(prefix, results, concreteParameterTypes);
                }
            }
        }
    }

    private void handleAction(String prefix, Collection<DetailedLink> results, String path, Method m) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        assert parameterTypes.length == 1;
        String returnValueStr = parameterTypes[0].getSimpleName();
        DetailedLink link = new RsdlBuilder.LinkBuilder().url(prefix + "/" + path).rel(path).requestParameter(ACTION).responseType(returnValueStr).httpMethod(HttpMethod.POST).build();
        addCommonActionParameters(link);
        addAsyncMatrixParameter(link);
        results.add(link);
    }

    private void handleDelete(String prefix, Collection<DetailedLink> results, Method m) {
        DetailedLink link = new RsdlBuilder.LinkBuilder().url(prefix)
            .rel(DELETE)
            .httpMethod(HttpMethod.DELETE)
            .build();
        Class<?>[] parameterTypes = m.getParameterTypes();
        if (parameterTypes.length > 0) {
            link.getRequest().getBody().setType(parameterTypes[0].getSimpleName());
        }
        addCommonActionParameters(link);
        addAsyncMatrixParameter(link);
        results.add(link);
    }

    private void handlePut(String prefix, Collection<DetailedLink> results, Class<?> returnType) {
        String returnTypeStr = getReturnTypeStr(returnType);
        DetailedLink link = new RsdlBuilder.LinkBuilder().url(prefix).rel(UPDATE).requestParameter(returnTypeStr).responseType(returnTypeStr).httpMethod(HttpMethod.PUT).build();
        addAsyncMatrixParameter(link);
        results.add(link);
    }

    private void handleGet(String prefix, Collection<DetailedLink> results, Class<?> returnType) {
        String returnTypeStr = getReturnTypeStr(returnType);
        DetailedLink link = new RsdlBuilder.LinkBuilder().url(prefix).rel(GET).responseType(returnTypeStr).httpMethod(HttpMethod.GET).build();
        if (BaseResources.class.isAssignableFrom(returnType) && returnType != Statistics.class) {
            addMaxMatrixParameter(link);
        }
        results.add(link);
    }

    /**
     * Adds to a link the parameters that are common to all actions, like {@code async} and {@code grace_period.expiry}.
     * These parameters will be added to all the signatures that have a body of type {@code Action}.
     *
     * @param link the link where the parameters will be added
     */
    private void addCommonActionParameters(DetailedLink link) {
        Request request = link.getRequest();
        if (request != null) {
            org.ovirt.engine.api.model.Body body = request.getBody();
            if (body != null) {
                String type = body.getType();
                if (ACTION.equals(type)) {
                    List<ParametersSet> parametersSets = body.getParametersSets();
                    for (ParametersSet parametersSet : parametersSets) {
                        List<Parameter> parameters = parametersSet.getParameters();
                        parameters.add(newGracePeriodParameter());
                        parameters.add(newAsyncActionParameter());
                    }
                }
            }
        }
    }

    /**
     * Creates the definition of the {@code grace_period.expiry} action parameter.
     */
    private Parameter newAsyncActionParameter() {
        Parameter parameter = new Parameter();
        parameter.setName("action.grace_period.expiry");
        parameter.setRequired(false);
        parameter.setType("xs:long");
        return parameter;
    }

    /**
     * Creates the definition of the {@code grace_period} action parameter.
     */
    private Parameter newGracePeriodParameter() {
        Parameter parameter = new Parameter();
        parameter.setName("action.async");
        parameter.setRequired(false);
        parameter.setType("xs:boolean");
        return parameter;
    }

    /**
     * Adds to a link the {@code async} matrix parameter.
     *
     * @param link the link where the parameters will be added
     */
    private void addAsyncMatrixParameter(DetailedLink link) {
        Parameter parameter = new Parameter();
        parameter.setName("async");
        parameter.setRequired(false);
        parameter.setType("xs:boolean");
        parameter.setValue("true|false");
        parameter.setContext("matrix");
        addUrlParameter(link, parameter);
    }

    /**
     * Adds to a link the {@code max} matrix parameter.
     *
     * @param link the link where the parameters will be added
     */
    private void addMaxMatrixParameter(DetailedLink link) {
        Parameter parameter = new Parameter();
        parameter.setName("max");
        parameter.setRequired(false);
        parameter.setType("xs:int");
        parameter.setValue("max results");
        parameter.setContext("matrix");
        addUrlParameter(link, parameter);
    }

    /**
     * Adds to a link the a URL parameter, creating all the intermediate objects if they don't exist.
     *
     * @param link the link where the parameters will be added
     * @param parameter the parameter to add
     */
    private void addUrlParameter(DetailedLink link, Parameter parameter) {
        Request request = link.getRequest();
        if (request == null) {
            request = new Request();
            link.setRequest(request);
        }
        Url url = request.getUrl();
        if (url == null) {
            url = new Url();
            request.setUrl(url);
        }
        List<ParametersSet> parametersSets = url.getParametersSets();
        ParametersSet parametersSet;
        if (parametersSets.isEmpty()) {
            parametersSet = new ParametersSet();
            parametersSets.add(parametersSet);
        } else {
            parametersSet = parametersSets.get(0);
        }
        parametersSet.getParameters().add(parameter);
    }

    private DetailedLink addParametersMetadata(DetailedLink link) {
        String link_name = link.getHref() + "|rel=" + link.getRel();
        if (this.parametersMetaData.containsKey(link_name)) {
            Action action = this.parametersMetaData.get(link_name);
            if (action.getDescription() != null) {
                link.setDescription(action.getDescription());
            }
            if (action.getRequest() != null) {
                addUrlParams(link, action);
                addHeaderParams(link, action);
                addBodyParams(link, action);
            }
        }
        return link;
    }

    private void addBodyParams(DetailedLink link, Action action) {
        if (action.getRequest().getBody() != null) {
            link.getRequest().getBody().setRequired(action.getRequest().getBody().isRequired());
            if (action.getRequest().getBody().getSignatures() != null) {
                for (Signature signature : action.getRequest().getBody().getSignatures()) {
                    ParametersSet ps = new ParametersSet();
                    if (signature.getDeprecated() != null) {
                        ps.setDeprecated(signature.getDeprecated());
                    }
                    if (signature.getDescription() != null) {
                        ps.setDescription(signature.getDescription());
                    }
                    addBodyParams(ps, signature.getMandatoryArguments().entrySet(), true);
                    addBodyParams(ps, signature.getOptionalArguments().entrySet(), false);
                    link.getRequest().getBody().getParametersSets().add(ps);
                }
            }
        }
    }

    private void addBodyParams(ParametersSet ps, Set<Entry<Object, Object>> entrySet, boolean required) {
        for (Entry<Object, Object> paramData : entrySet) {
            Parameter param = createBodyParam(paramData, required);
            ps.getParameters().add(param);
        }
    }

    private Parameter createBodyParam(Entry<Object, Object> mandatoryKeyValuePair, boolean required) {
        Parameter param = new Parameter();
        param.setRequired(required);
        String paramName = getParamName(mandatoryKeyValuePair);
        param.setName(paramName);
        if (mandatoryKeyValuePair.getKey().toString().contains(COLLECTION_PARAMETER_YAML)) {
            handleCollection(mandatoryKeyValuePair, required, param);
        } else {
            param.setType(mandatoryKeyValuePair.getValue().toString());
        }
        if (mandatoryKeyValuePair.getKey().toString().contains(DEPRECATED_PARAMETER_YAML)) {
            param.setDeprecated(true);
        }
        return param;
    }

    private void handleCollection(Entry<Object, Object> mandatoryKeyValuePair, boolean required, Parameter param) {
        param.setType(COLLECTION_PARAMETER_RSDL);
        @SuppressWarnings("unchecked")
        Map<Object, Object> listParams = (Map<Object, Object>)mandatoryKeyValuePair.getValue();
        param.setParametersSet(new ParametersSet());
        for (Entry<Object, Object> listParamData : listParams.entrySet()) {
            Parameter listParam = createBodyParam(listParamData, required);
            param.getParametersSet().getParameters().add(listParam);
        }
    }

    private String getParamName(Entry<Object, Object> mandatoryKeyValuePair) {
        String paramName = mandatoryKeyValuePair.getKey().toString();
        if (paramName.contains("--")) {
            paramName = paramName.substring(0, paramName.indexOf("--"));
        }
        return paramName;
    }

    private void addHeaderParams(DetailedLink link, Action action) {
        // Add the parameters that are specified in the metadata:
        if (action.getRequest().getHeaders() != null && !action.getRequest().getHeaders().isEmpty()) {
            link.getRequest().setHeaders(new Headers());
            for (Object key :  action.getRequest().getHeaders().keySet()) {
                Header header = new Header();
                header.setName(key.toString());
                Object value = action.getRequest().getHeaders().get(key);
                if (value != null) {
                    ParamData paramData = (ParamData) value;
                    header.setValue(paramData.getValue());
                    header.setRequired(paramData.getRequired() == null ? Boolean.FALSE : paramData.getRequired());
                    header.setDeprecated(paramData.getDeprecated());
                }

                link.getRequest().getHeaders().getHeaders().add(header);
            }
        }

        // All the operations that potentially modify the state of the system accept the "Correlation-Id" header, so
        // instead of adding it explicitly in the metadata file it is better to add it implicitly:
        if (!GET.equals(link.getRel())) {
            addCorrelationIdHeader(link);
        }

        // All the operations that potentially send a body (everything except GET) should also specify
        // the "Content-Type" header, so instead of explicitly adding it in the metadata file it is better to add it
        // implicity:
        if (!GET.equals(link.getRel())) {
            addContentTypeHeader(link);
        }

        // All the operations that create a new entity (those whose rel is "add") support the "Expect" header with the
        // "201-created" value, so instead of explicitly adding it in the metadata file it is better to add it
        // implicitly:
        if (ADD.equals(link.getRel())) {
            addExpectHeader(link, "201-created");
        }

        // All the operations that update entities (those whose rel is "update") support the "Expect" header with the
        // "202-accepted" value, so instead of explicitly adding it in the metadata file it is better to add it
        // implicitly:
        if (UPDATE.equals(link.getRel())) {
            addExpectHeader(link, "202-accepted");
        }
    }

    /**
     * Adds the description of the {@code Correlation-Id} header to a link.
     *
     * @param link the link where the description of the header will be added
     */
    private void addCorrelationIdHeader(DetailedLink link) {
        Headers headers = link.getRequest().getHeaders();
        if (headers == null) {
            headers = new Headers();
            link.getRequest().setHeaders(headers);
        }
        Header header = new Header();
        header.setName("Correlation-Id");
        header.setValue("any string");
        header.setRequired(false);
        headers.getHeaders().add(header);
    }

    /**
     * Adds the description of the {@code Content-Type} header to a link.
     *
     * @param link the link where the description of the header will be added
     */
    private void addContentTypeHeader(DetailedLink link) {
        Headers headers = link.getRequest().getHeaders();
        if (headers == null) {
            headers = new Headers();
            link.getRequest().setHeaders(headers);
        }
        Header header = new Header();
        header.setName("Content-Type");
        header.setValue("application/xml|json");
        header.setRequired(true);
        headers.getHeaders().add(header);
    }

    /**
     * Adds the description of the {@code Expect} header to a link.
     *
     * @param link the link where the description of the header will be added
     * @param value the value of the header
     */
    private void addExpectHeader(DetailedLink link, String value) {
        Headers headers = link.getRequest().getHeaders();
        if (headers == null) {
            headers = new Headers();
            link.getRequest().setHeaders(headers);
        }
        Header header = new Header();
        header.setName("Expect");
        header.setValue(value);
        header.setRequired(false);
        headers.getHeaders().add(header);
    }

    private void addUrlParams(DetailedLink link, Action action) {
        if (action.getRequest().getUrlparams() != null && !action.getRequest().getUrlparams().isEmpty()) {
            link.getRequest().setUrl(new Url());
            ParametersSet ps = new ParametersSet();
        for (Object key :  action.getRequest().getUrlparams().keySet()) {
                Parameter param = new Parameter();
                param.setName(key.toString());
                Object value = action.getRequest().getUrlparams().get(key);
                if (value != null) {
                    ParamData urlParamData = (ParamData)value;
                    param.setType(urlParamData.getType());
                    param.setContext(urlParamData.getContext());
                    param.setValue(urlParamData.getValue());
                    param.setRequired(urlParamData.getRequired()==null ? Boolean.FALSE : urlParamData.getRequired());
                    param.setDeprecated(urlParamData.getDeprecated());
                }
                ps.getParameters().add(param);
            }
            link.getRequest().getUrl().getParametersSets().add(ps);
        }
    }

    private void handleAdd(String prefix, Collection<DetailedLink> results, Class<?>[] parameterTypes) {
        assert parameterTypes.length == 1;
        String s = parameterTypes[0].getSimpleName();
        s = handleExcpetionalCases(s, prefix); //TODO: refactor to a more generic solution

        results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(ADD).requestParameter(s).responseType(s).httpMethod(HttpMethod.POST).build());
    }

    private String handleExcpetionalCases(String s, String prefix) {
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
            if (prefix.contains("watchdogs")) {
                return "WatchDog";
            }
        }
        return s;
    }

    /**
     * get the class name, without package prefix
     */
    private String getReturnTypeStr(Class<?> returnValue) {
        int lastIndexOf = returnValue.getSimpleName().lastIndexOf(".");
        String entityType = lastIndexOf==-1 ? returnValue.getSimpleName() : returnValue.getSimpleName().substring(lastIndexOf);
        return entityType;
    }

    private Class<?> findConcreteType(Type generic, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
        for (Type superInterface : resource.getGenericInterfaces()) {
            if (superInterface instanceof ParameterizedType) {
                ParameterizedType p = (ParameterizedType)superInterface;
                Class<?> clazz = Class.forName(p.getRawType().toString().substring(p.getRawType().toString().lastIndexOf(' ')+1));
                Map<String, Type> map = new HashMap<>();
                for (int i=0; i<p.getActualTypeArguments().length; i++) {
                    if (!map.containsKey(clazz.getTypeParameters()[i].toString())) {
                        map.put(clazz.getTypeParameters()[i].toString(), p.getActualTypeArguments()[i]);
                    }
                }
                if (map.containsKey(generic.toString())) {
                    String type = map.get(generic.toString()).toString();
                    try {
                        Class<?> returnClass = Class.forName(type.substring(type.lastIndexOf(' ')+1));
                        return returnClass;
                    } catch (ClassNotFoundException e) {
                        break;
                    }
                }
            }
        }
        if (parametersMap.containsKey(generic.toString())) {
            try {
                Type type = parametersMap.get(generic.toString());
                Class<?> returnClass = Class.forName(type.toString().substring(type.toString().indexOf(' ') +1));
                return returnClass;
            } catch (ClassNotFoundException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isSingleEntityResource(Method m) {
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

    private boolean isAction(Method m) {
        return m.isAnnotationPresent(Actionable.class);
    }


    private boolean isRequiresDescription(Method m) {
        boolean pathRelevant = !(m.isAnnotationPresent(Path.class) && m.getAnnotation(Path.class).value().contains(":"));
        boolean returnValueRelevant = !m.getReturnType().equals(CreationResource.class);
        return pathRelevant && returnValueRelevant;
    }

    //might need to truncate the plural 's', for example:
    //for "{api}/hosts/{host:id}/nics" return "nic"
    //but for "{api}/hosts/{host:id}/storage" return "storage" (don't truncate last character)
    private String getSingleForm(String prefix) {
        int startIndex = prefix.lastIndexOf('/')+1;
        prefix = prefix.substring(startIndex);
        if (prefix.endsWith("ies")) {
            return prefix.replaceAll("ies$", "y");
        }
        if (prefix.endsWith("s")) {
            return prefix.replaceAll("s$", "");
        }
        return prefix;
    }

    /**
     * There is a special kind of url: a url that may receive a body (with parameters in it),
     * or may not. For example, when deleting a datacenter, the user may pass nothing in the body,
     * or may pass <action><force>true</force></action>.
     *
     * RSDL builder will encounter both signatures during construction, and when it encounters the
     * first is has no knowledge of the second yet, so it must create both linke.
     *
     * This method will be called at the end of construction, and search for such duplicate links.
     * It will unite these pairs into a single link with required=false in the <body>.
     */
    private void uniteDuplicateLinks(Rsdl rsdl) {
        Map<String, DetailedLink> linksMap = new HashMap<>();
        Collection<DetailedLink> linksToDelete = new LinkedList<>();
        for (DetailedLink link : rsdl.getLinks().getLinks()) {
            String linkId = link.getHref() + link.getRel();
            if (linksMap.containsKey(linkId)) {
              //duplicate found, determine which of the two should be deleted
                DetailedLink linkToDelete = decideWhichToDelete(linksMap.get(linkId), link);
                if (linkToDelete!=null) {
                    linksToDelete.add(linkToDelete);
                }
            } else {
                linksMap.put(linkId, link);
            }
        }
        for (DetailedLink link : linksToDelete) {
            rsdl.getLinks().getLinks().remove(link);
        }
    }

    private DetailedLink decideWhichToDelete(DetailedLink link1, DetailedLink link2) {
        String link1ParamType = link1.getRequest().getBody().getType();
        String link2ParamType = link2.getRequest().getBody().getType();
        //Verify for both links that body is not mandatory
        if (    (
                (link1.getRequest().getBody().isRequired() != null)
                &&
                (link2.getRequest().getBody().isRequired() != null)
                )

                &&

                (
                (Boolean.FALSE.equals(link1.getRequest().getBody().isRequired()))
                &&
                (Boolean.FALSE.equals(link2.getRequest().getBody().isRequired()))
                )
           ) {
            if (link1ParamType!=null && link2ParamType==null) {
                return link2;
            }
            if (link1ParamType==null && link2ParamType!=null) {
                return link1;
            }
        }
        return null;
    }
}
