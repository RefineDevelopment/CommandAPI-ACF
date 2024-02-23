/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import co.aikar.commands.annotation.Dependency;
import co.aikar.util.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


@SuppressWarnings("WeakerAccess")
@NoArgsConstructor
public abstract class CommandManager<IT, I extends CommandIssuer, CEC extends CommandExecutionContext<I>, CC extends ConditionContext<I>> {
    protected CommandManager(CommandManager<IT, I, CEC, CC> copyFrom) {
        this.replacements = copyFrom.replacements;
        this.conditions = copyFrom.conditions;
        this.defaultExceptionHandler = copyFrom.defaultExceptionHandler;
        this.helpFormatter = copyFrom.helpFormatter;
        this.defaultHelpPerPage = copyFrom.defaultHelpPerPage;
        this.logUnhandledExceptions = copyFrom.logUnhandledExceptions;
        this.annotations = copyFrom.annotations;
        this.dependencies = copyFrom.dependencies;
    }

    /**
     * This is a stack incase a command calls a command
     */
    static ThreadLocal<Stack<CommandOperationContext>> commandOperationContext = ThreadLocal.withInitial(() -> new Stack<CommandOperationContext>() {
        @Override
        public synchronized CommandOperationContext peek() {
            return super.isEmpty() ? null : super.peek();
        }
    });

    protected CommandReplacements replacements = new CommandReplacements(this);
    protected CommandConditions<I, CEC, CC> conditions = new CommandConditions<>(this);
    protected Map<String, RootCommand> rootCommands = new HashMap<>();
    /**
     * -- GETTER --
     *  Gets the current default exception handler, might be null.
     *
     * @return the default exception handler
     */
    @Getter protected ExceptionHandler defaultExceptionHandler = null;
    protected Table<Class<?>, String, Object> dependencies = new Table<>();
    @Setter @Getter protected CommandHelpFormatter helpFormatter = new CommandHelpFormatter();
    @Setter @Getter protected int defaultHelpPerPage = 10;

    boolean logUnhandledExceptions = true;

    @Getter private Annotations<CommandManager<IT, I, CEC, CC>> annotations = new Annotations<>(this);
    private final CommandRouter router = new CommandRouter();

    public static CommandOperationContext getCurrentCommandOperationContext() {
        return commandOperationContext.get().peek();
    }

    public static CommandIssuer getCurrentCommandIssuer() {
        CommandOperationContext context = commandOperationContext.get().peek();
        return context != null ? context.getCommandIssuer() : null;
    }

    public static CommandManager getCurrentCommandManager() {
        CommandOperationContext context = commandOperationContext.get().peek();
        return context != null ? context.getCommandManager() : null;
    }

    public CommandConditions<I, CEC, CC> getCommandConditions() {
        return conditions;
    }

    /**
     * Gets the command contexts manager
     *
     * @return Command Contexts
     */
    public abstract CommandContexts<?> getCommandContexts();

    /**
     * Gets the command completions manager
     *
     * @return Command Completions
     */
    public abstract CommandCompletions<?> getCommandCompletions();

    public CommandHelp generateCommandHelp(@NotNull String command) {
        CommandOperationContext context = getCurrentCommandOperationContext();
        if (context == null) {
            throw new IllegalStateException("This method can only be called as part of a command execution.");
        }
        return generateCommandHelp(context.getCommandIssuer(), command);
    }

    public CommandHelp generateCommandHelp(CommandIssuer issuer, @NotNull String command) {
        return generateCommandHelp(issuer, obtainRootCommand(command));
    }

    public CommandHelp generateCommandHelp() {
        CommandOperationContext context = getCurrentCommandOperationContext();
        if (context == null) {
            throw new IllegalStateException("This method can only be called as part of a command execution.");
        }
        String commandLabel = context.getCommandLabel();
        return generateCommandHelp(context.getCommandIssuer(), this.obtainRootCommand(commandLabel));
    }

    public CommandHelp generateCommandHelp(CommandIssuer issuer, RootCommand rootCommand) {
        return new CommandHelp(this, rootCommand, issuer);
    }

    CommandRouter getRouter() {
        return router;
    }

    /**
     * Registers a command with ACF
     *
     * @param command The command to register
     * @return boolean
     */
    public abstract void registerCommand(BaseCommand command);

    public abstract boolean hasRegisteredCommands();

    public abstract boolean isCommandIssuer(Class<?> type);

    public abstract I getCommandIssuer(Object issuer);

    public abstract RootCommand createRootCommand(String cmd);

    public ConditionContext createConditionContext(CommandIssuer issuer, String config) {
        //noinspection unchecked
        return new ConditionContext(issuer, config);
    }

    public abstract CommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs);

    public abstract CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args);

    public abstract void log(final LogLevel level, final String message, final Throwable throwable);

    public void log(final LogLevel level, final String message) {
        log(level, message, null);
    }

    /**
     * Lets you add custom string replacements that can be applied to annotation values,
     * to reduce duplication/repetition of common values such as permission nodes and command prefixes.
     * <p>
     * Any replacement registered starts with a %
     * <p>
     * So for ex @CommandPermission("%staff")
     *
     * @return Replacements Manager
     */
    public CommandReplacements getCommandReplacements() {
        return replacements;
    }

    public boolean hasPermission(CommandIssuer issuer, Set<String> permissions) {
        for (String permission : permissions) {
            if (!hasPermission(issuer, permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(CommandIssuer issuer, String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        for (String perm : ACFPatterns.COMMA.split(permission)) {
            if (!perm.isEmpty() && !issuer.hasPermission(perm)) {
                return false;
            }
        }
        return true;
    }

    public synchronized RootCommand getRootCommand(@NotNull String cmd) {
        return rootCommands.get(ACFPatterns.SPACE.split(cmd.toLowerCase(), 2)[0]);
    }

    public synchronized RootCommand obtainRootCommand(@NotNull String cmd) {
        return rootCommands.computeIfAbsent(ACFPatterns.SPACE.split(cmd.toLowerCase(), 2)[0], this::createRootCommand);
    }

    public abstract Collection<RootCommand> getRegisteredRootCommands();

    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new RegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    /**
     * Sets the default {@link ExceptionHandler} that is called when an exception occurs while executing a command, if the command doesn't have its own exception handler registered, and lets you control if ACF should also log the exception still.
     * <p>
     * If you disable logging, you need to log it yourself in your handler.
     *
     * @param exceptionHandler the handler that should handle uncaught exceptions. May not be null if logExceptions is false
     * @param logExceptions    Whether or not to log exceptions.
     */
    public void setDefaultExceptionHandler(ExceptionHandler exceptionHandler, boolean logExceptions) {
        if (exceptionHandler == null && !logExceptions) {
            throw new IllegalArgumentException("You may not disable the default exception handler and have logging of unhandled exceptions disabled");
        }
        this.logUnhandledExceptions = logExceptions;
        this.defaultExceptionHandler = exceptionHandler;
    }

    public boolean isLoggingUnhandledExceptions() {
        return this.logUnhandledExceptions;
    }

    /**
     * Sets the default {@link ExceptionHandler} that is called when an exception occurs while executing a command, if the command doesn't have its own exception handler registered.
     *
     * @param exceptionHandler the handler that should handle uncaught exceptions.  May not be null if logExceptions is false
     */
    public void setDefaultExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler == null && !this.logUnhandledExceptions) {
            throw new IllegalArgumentException("You may not disable the default exception handler and have logging of unhandled exceptions disabled");
        }
        defaultExceptionHandler = exceptionHandler;
    }

    protected boolean handleUncaughtException(BaseCommand scope, RegisteredCommand registeredCommand, CommandIssuer sender, List<String> args, Throwable t) {
        if (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        boolean result = false;
        if (scope.getExceptionHandler() != null) {
            result = scope.getExceptionHandler().execute(scope, registeredCommand, sender, args, t);
        } else if (defaultExceptionHandler != null) {
            result = defaultExceptionHandler.execute(scope, registeredCommand, sender, args, t);
        }
        return result;
    }

    public void sendMessage(IT issuerArg, String s) {
        sendMessage(getCommandIssuer(issuerArg), s);
    }

    public void sendMessage(CommandIssuer issuer, String s) {
        String message = formatMessage(issuer, s);

        for (String msg : ACFPatterns.NEWLINE.split(message)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public String formatMessage(CommandIssuer issuer, String message) {
        message = getCommandReplacements().replace(message);
        return message;
    }

    CommandOperationContext<I> createCommandOperationContext(BaseCommand command, CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync) {
        //noinspection unchecked
        return new CommandOperationContext<>(this, (I) issuer, command, commandLabel, args, isAsync);
    }

    /**
     * Registers an instance of a class to be registered as an injectable dependency.<br>
     * The command manager will attempt to inject all fields in a command class that are annotated with
     * {@link co.aikar.commands.annotation.Dependency} with the provided instance.
     *
     * @param clazz    the class the injector should look for when injecting
     * @param instance the instance of the class that should be injected
     * @throws IllegalStateException when there is already an instance for the provided class registered
     */
    public <T> void registerDependency(Class<? extends T> clazz, T instance) {
        registerDependency(clazz, clazz.getName(), instance);
    }

    /**
     * Registers an instance of a class to be registered as an injectable dependency.<br>
     * The command manager will attempt to inject all fields in a command class that are annotated with
     * {@link co.aikar.commands.annotation.Dependency} with the provided instance.
     *
     * @param clazz    the class the injector should look for when injecting
     * @param key      the key which needs to be present if that
     * @param instance the instance of the class that should be injected
     * @throws IllegalStateException when there is already an instance for the provided class registered
     */
    public <T> void registerDependency(Class<? extends T> clazz, String key, T instance) {
        if (dependencies.containsKey(clazz, key)) {
            throw new IllegalStateException("There is already an instance of " + clazz.getName() + " with the key " + key + " registered!");
        }

        dependencies.put(clazz, key, instance);
    }

    /**
     * Attempts to inject instances of classes registered with {@link CommandManager#registerDependency(Class, Object)}
     * into all fields of the class and its superclasses that are marked with {@link Dependency}.
     *
     * @param baseCommand the instance which fields should be filled
     */
    void injectDependencies(BaseCommand baseCommand) {
        Class<?> clazz = baseCommand.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (annotations.hasAnnotation(field, Dependency.class)) {
                    String dependency = annotations.getAnnotationValue(field, Dependency.class);
                    String key = (key = dependency).isEmpty() ? field.getType().getName() : key;
                    Object object = dependencies.row(field.getType()).get(key);
                    if (object == null) {
                        throw new UnresolvedDependencyException("Could not find a registered instance of " + field.getType().getName() + " with key " + key + " for field " + field.getName() + " in class " + baseCommand.getClass().getName());
                    }

                    try {
                        boolean accessible = field.isAccessible();
                        if (!accessible) {
                            field.setAccessible(true);
                        }
                        field.set(baseCommand, object);
                        field.setAccessible(accessible);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace(); //TODO should we print our own exception here to make a more descriptive error?
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(BaseCommand.class));
    }
    public String getCommandPrefix(CommandIssuer issuer) {
        return "";
    }
}
