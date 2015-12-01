package org.ovirt.engine.ui.uicommonweb.builders;


/**
 * A builder which takes a list of builderes and invokes all of them one by one
 *
 * @param <S>
 *            source
 * @param <D>
 *            destination
 */
public class CompositeBuilder<S, D> implements Builder<S, D> {

    private BuilderList<S, D> builders;

    public CompositeBuilder(Builder<S, D>... builders) {
        this.builders = new BuilderList<>(builders);
    }

    @Override
    public void build(S source, D destination, BuilderList<S, D> rest) {
        BuilderList<S, D> joinedWithRest = builders.append(new LastBuilder(rest));
        joinedWithRest.head().build(source, destination, joinedWithRest.tail());
    }

    /**
     * Hook for descendant builders to implement additional build steps after the parent builders execute
     */
    protected void postBuild(S source, D destination) {
    }

    /**
     * It gets called when the last of the child builders finished it's job. Than this builder calls the next builder
     * after this one
     */
    class LastBuilder implements Builder<S, D> {

        private BuilderList<S, D> parentsRest;

        public LastBuilder(BuilderList<S, D> parentsRest) {
            this.parentsRest = parentsRest;
        }

        @Override
        public void build(S source, D destination, BuilderList<S, D> rest) {
            // finish this builder and delegate
            postBuild(source, destination);
            // ignoring the "rest" because this class is always the last in the chain
            parentsRest.head().build(source, destination, parentsRest.tail());
        }

    }

}
