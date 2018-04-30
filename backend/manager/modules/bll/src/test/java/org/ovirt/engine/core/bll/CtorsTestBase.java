package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.ReflectionUtils;

public abstract class CtorsTestBase {
    protected abstract Collection<Class<?>> getClassesToTest();

    protected static <E extends Enum<E>> Collection<Class<?>> commandsFromEnum
            (Class<E> enumClass, Function<String, Class<?>> f) {

        // Create a stream of the enum objects.
        return EnumSet.allOf(enumClass)
                .stream()
                .map(Enum::name)
                // Filter out the Unknown value.
                .filter(e -> !e.equals("Unknown"))
                // Map each ActionType to its appropriate class.
                .map(f)
                .collect(Collectors.toList());
    }

    /**
     * {@link CommandsFactory} can call three different types of command's constructors
     * (see {@link #getConstructorRequiredByCommandsFactoryPredicate()} method for more details).
     * This test verifies that each of these constructors is accessible from {@link CommandsFactory}.
     * Since we create command objects using reflection, this test can be considered as a compilation
     * check for the accessibility of {@link CommandsFactory} to the commands' constructors.
     */
    @Test
    public void testCommandsConstructorsContract() {
        Package commandsFactoryPackage = CommandsFactory.class.getPackage();
        Predicate<Constructor<?>> constructorRequiredByCommandsFactory =
                getConstructorRequiredByCommandsFactoryPredicate();
        Predicate<Constructor<?>> constructorInaccessibleFromPackagePredicate =
                createConstructorInaccessibleFromPackagePredicate(commandsFactoryPackage);

        Map<Class<?>, List<Constructor<?>>> commandsWithInaccessibleConstructor =
                getClassesToTest().stream()
                        .map(command -> Arrays.stream(command.getDeclaredConstructors()))
                        // Filter out all constructors that are not required by CommandsFactory.
                        .map(constructorStream -> constructorStream.filter(constructorRequiredByCommandsFactory))
                        // Filter only the constructors that are not accessible from CommandsFactory.
                        .map(constructorStream -> constructorStream.filter(constructorInaccessibleFromPackagePredicate))
                        // Flat Stream<Stream<Constructor<?>>> to Stream<Constructor<?>>.
                        .flatMap(Function.identity())
                        .collect(Collectors.groupingBy(Constructor::getDeclaringClass));

        assertThat("There are commands with at least one inaccessible constructor from CommandsFactory:",
                commandsWithInaccessibleConstructor.entrySet(),
                new BaseMatcher<Set<Map.Entry<Class<?>, List<Constructor<?>>>>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public boolean matches(Object o) {
                        return ((Set<Map.Entry<Class<?>, List<Constructor<?>>>>) o).isEmpty();
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("All constructors should be accessible");
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void describeMismatch(Object item, Description description) {
                        description.appendText("Found inaccessible constructors:" + System.lineSeparator());
                        String startStr = String.format(":%n\t");
                        String separatorStr = String.format("%n\t");
                        String endStr = String.format("%n%n");
                        ((Set<Map.Entry<Class<?>, List<Constructor<?>>>>) item).forEach(
                                commandsWithInaccessibleConstructors -> description.appendValueList(
                                        commandsWithInaccessibleConstructors.getKey().getSimpleName() + startStr,
                                        separatorStr,
                                        endStr,
                                        commandsWithInaccessibleConstructors.getValue())
                        );

                    }
                });
    }

    /**
     * Returns a predicate that gets a constructor and returns true iff
     * it's one of the constructors that are required by {@link CommandsFactory}.
     */
    private Predicate<Constructor<?>> getConstructorRequiredByCommandsFactoryPredicate() {
        return getMandatoryCtorPredicates()
                .map(MandatoryCtorPredicate::getConstructorPredicate)
                .reduce(Predicate::or)
                .orElse(t -> true);
    }

    /**
     * A constructor is inaccessible from sourcePackage iff one of the next is true:
     * - It's also located in sourcePackage and its modifier is private.
     * - It's located outside of sourcePackage and its modifier is not public.
     */
    private Predicate<Constructor<?>> createConstructorInaccessibleFromPackagePredicate(Package sourcePackage) {
        return constructor ->
                Modifier.isPrivate(constructor.getModifiers()) ||
                        (!sourcePackage.equals(constructor.getDeclaringClass().getPackage()) &&
                                !Modifier.isPublic(constructor.getModifiers()));
    }

    /**
     * Gets an array of classes and returns a predicate that given a constructor, returns
     * true iff its signature is compatible with the signature composed from the class array.
     */
    protected static Predicate<Constructor<?>> createConstructorSignaturePredicate(Class<?>... constructorParametersTypes) {
        return constructor ->
                ReflectionUtils.isCompatible(constructor.getParameterTypes(), constructorParametersTypes);
    }

    @Test
    public void testCommandMandatoryConstructorsExistence() {
        List<String> commandsWithoutMandatoryConstructor =
                getClassesToTest().stream()
                        .filter(getMandatoryCtorPredicate())
                        .map(Class::getSimpleName)
                        .sorted()
                        .collect(Collectors.toList());
        assertThat(getMandatoryCtorMessage(), commandsWithoutMandatoryConstructor, empty());
    }

    protected abstract Stream<MandatoryCtorPredicate> getMandatoryCtorPredicates();

    private Predicate<Class<?>> getMandatoryCtorPredicate() {
        return getMandatoryCtorPredicates()
                .map(Predicate.class::cast)
                .map(Predicate::negate)
                .reduce(Predicate::or)
                .orElse(t -> true);
    }

    private String getMandatoryCtorMessage() {
        return "There are commands that don't contain at least one of the mandatory constructors:"
                + System.lineSeparator()
                + getMandatoryCtorPredicates().map(MandatoryCtorPredicate::getMessage)
                    .collect(Collectors.joining(System.lineSeparator()));
    }

    protected static class MandatoryCtorPredicate implements Predicate<Class<?>> {
        private Class<?>[] ctorArgsTypes;

        public MandatoryCtorPredicate(Class<?>... ctorArgsTypes) {
            this.ctorArgsTypes = ctorArgsTypes;
        }

        @Override
        public boolean test(Class<?> commandClass) {
            return Arrays.stream(commandClass.getDeclaredConstructors()).anyMatch(getConstructorPredicate());
        }

        public Predicate<Constructor<?>> getConstructorPredicate() {
            return c -> ReflectionUtils.isCompatible(c.getParameterTypes(), ctorArgsTypes);
        }

        public String getMessage() {
            return getArgumentMessage() + (additionalInfo() == null ? "" : ", and " + additionalInfo());
        }

        private String getArgumentMessage() {
            return "Constructor with " +
                    Arrays.stream(ctorArgsTypes).map(Class::getSimpleName).collect(Collectors.joining(", ", "[", "]")) +
                    " parameters";
        }

        protected String additionalInfo() {
            return null;
        }
    }
}
