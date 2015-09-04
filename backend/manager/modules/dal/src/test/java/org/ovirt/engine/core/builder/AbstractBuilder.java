package org.ovirt.engine.core.builder;

/**
 * Base for builders to create domain objects. Builders extending this builder allow building and peristing objects. The
 * builders are only meant for unit- and integration testing purposes.
 * <p>
 * Use the following methods to create an entity with the builder:
 * <p>
 * * {@link #persist()} to persist the built object in a real database. This is very useful when running integration
 * tests with an available database.
 * <p>
 * * {@link #build()} to just build the objects and return them. This is very useful when writing unit tests. No
 * database needs to be present and no DAOs are involved.
 * <p>
 * The builder will always return the same instance when the builder is not explicitly reset. This allows persisting
 * many instances by just changing the {@link org.ovirt.engine.core.compat.Guid} of an object.
 * <p>
 * Use the following methods to reset the builder:
 * <p>
 * * {@link #reset()} reset the whole builder to its original state.
 * <p>
 * * {@link #reset(T object)} resets the whole builder to its original state and sets the supplied argument as the
 * template to use.
 * <p>
 * <b>Examples</b>
 * <p>
 * Add a running VM to a host, persist everything to the database and load all VMs which are running on the
 * host:
 * <pre>{@code
 * VDS host = vdsBuilder.cluster(persistedCluster).persist();
 * vmBuilder.host(host).up().persist();
 * List<VM> vms = vmDao.getAllRunningForVds(host.getId());
 * }</pre>
 * Add 10 hosts with 1 GB of RAM to a cluster, persist the hosts to the database and load them again:
 * <pre>{@code
 * VdsBuilder builder = new VdsBuilder().cluster(persistedCluster).physicalMemory(1000);
 * for (int x =0; x < 10; x++){
 *     builder.id(Guid.newGuid()).persist();
 * }
 * List<Vds> persistedHosts = vdsDao.getAllForCluster(persistedCluster.getId());
 * }</pre>
 */
public abstract class AbstractBuilder<T, B extends AbstractBuilder> {

    public AbstractBuilder() {
        reset();
    }

    protected T object;

    /**
     * Reset the builder into its original state.
     *
     * @return the scrubbed builder
     */
    public abstract B reset();

    /**
     * Reset the builder into its original state and set the provided object as a template.
     *
     * @param object the template
     * @return the builder
     */
    public abstract B reset(T object);

    /**
     * Hook to check if all default values are set when building an object with {@link #build()}. Validation and setting
     * default values if they are missing can be done here.
     */
    protected void preBuild() {
    }

    /**
     * Hook to change the way on how to finally build the desired object. The default implementation just returns
     * the {@link AbstractBuilder#object}. An alternative implementation might do a shallow or deep copy on every
     * invocation.
     * @return the created object
     */
    protected T doBuild() {
        return object;
    }

    /**
     * Hook to check if all default values are set when building an object with {@link #persist()}. Validation and
     * setting default values if they are missing can be done here.
     */
    protected abstract void prePersist();

    /**
     * Persist the object in the database and return the persisted object.
     * <p>
     * Subsequent calls to persist without resetting an object or setting another id can lead to constraint violations
     * in the database.
     *
     * @return the persisted object
     */
    protected abstract T doPersist();

    public final T build() {
        preBuild();
        return doBuild();
    }

    public final T persist() {
        prePersist();
        return doPersist();
    }

}
