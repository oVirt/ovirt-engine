package org.ovirt.engine.core.bll;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReflectionUtils;

public class CommandCtorsTest {

    private static Collection<Class<CommandBase<? extends VdcActionParametersBase>>> commandClasses;

    private static Predicate<Constructor<?>> mandatoryConstructorSignature;

    @BeforeClass
    public static void initCommandsCollection() {
        // Create a stream of all VdcActionType objects.
        commandClasses = Arrays.stream(VdcActionType.values())
                // Filter out the Unknown VdcActionType.
                .filter(vdcActionType -> vdcActionType != VdcActionType.Unknown)
                // Map each vdcActionType to its appropriate command.
                .map(vdcActionType -> CommandsFactory.getCommandClass(vdcActionType.toString()))
                .collect(Collectors.toList());
    }

    @BeforeClass
    public static void createMandatoryConstructorSignaturePredicate() {
        // Signature for a constructor that receives parameters and context objects.
        mandatoryConstructorSignature =
                createConstructorSignaturePredicate(VdcActionParametersBase.class, CommandContext.class);
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
                commandClasses.stream()
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
                        ((Set<Map.Entry<Class<?>, List<Constructor<?>>>>) item).stream().forEach(
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
        // Signature for a constructor that gets a Guid object.
        Predicate<Constructor<?>> guidConstructorSignature =
                createConstructorSignaturePredicate(Guid.class);

        return mandatoryConstructorSignature.or(guidConstructorSignature);
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
    private static Predicate<Constructor<?>> createConstructorSignaturePredicate(Class<?>... constructorParametersTypes) {
        return constructor ->
                ReflectionUtils.isCompatible(constructor.getParameterTypes(), constructorParametersTypes);
    }

    @Test
    public void testCommandMandatoryConstructorsExistence() {
        List<String> commandsWithoutMandatoryConstructor =
                commandClasses.stream()
                        .filter(commandClass ->
                                Arrays.stream(commandClass.getDeclaredConstructors())
                                        .noneMatch(mandatoryConstructorSignature))
                        .map(Class::getSimpleName)
                        .sorted()
                        .collect(Collectors.toList());
        assertThat("There are commands that don't contain the mandatory constructor (constructor that receives " +
                "parameters and context objects):", commandsWithoutMandatoryConstructor, empty());
    }
}
