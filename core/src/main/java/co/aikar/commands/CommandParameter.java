/*
 * Copyright (c) 2016-2018 Daniel Ennis (Aikar) - MIT License
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

import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flag;
import co.aikar.commands.annotation.Name;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.contexts.OptionalContextResolver;
import lombok.Getter;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandParameter<CEC extends CommandExecutionContext<CEC, ? extends CommandIssuer>> {
    @Getter
    private final Parameter parameter;
    @Getter
    private final Class<?> type;
    @Getter
    private final String name;
    @Getter
    private final CommandManager manager;
    @Getter
    private final int paramIndex;
    boolean consumesRest;
    @Getter
    private ContextResolver<?, CEC> resolver;
    @Getter
    private boolean optional;
    private final Set<String> permissions = new HashSet<>();
    private final String permission;
    @Getter
    private String description;
    @Getter
    private String defaultValue;
    private String syntax;
    @Getter
    private String conditions;
    private boolean requiresInput;
    @Getter
    private boolean commandIssuer;
    @Getter
    private String[] values;
    private boolean canConsumeInput;
    @Getter
    private boolean optionalResolver;
    private final boolean isLast;
    private final boolean isOptionalInput;
    @Getter
    private CommandParameter<CEC> nextParam;
    private boolean isFlag;
    @Getter
    private String flag;
    @Getter
    private String[] flagAliases;
    public CommandParameter(RegisteredCommand<CEC> command, Parameter param, int paramIndex, boolean isLast) {
        this.parameter = param;
        this.isLast = isLast;
        this.type = param.getType();
        this.manager = command.manager;
        this.paramIndex = paramIndex;
        Annotations annotations = manager.getAnnotations();

        String annotationName = annotations.getAnnotationValue(param, Name.class, Annotations.REPLACEMENTS);
        this.name = annotationName != null ? annotationName : param.getName();
        this.defaultValue = annotations.getAnnotationValue(param, Default.class, Annotations.REPLACEMENTS | (type != String.class ? Annotations.NO_EMPTY : 0));
        this.description = annotations.getAnnotationValue(param, Description.class, Annotations.REPLACEMENTS | Annotations.DEFAULT_EMPTY);
        this.conditions = annotations.getAnnotationValue(param, Conditions.class, Annotations.REPLACEMENTS | Annotations.NO_EMPTY);

        if (annotations.hasAnnotation(param, Flag.class)) {
            if (type != boolean.class && type != Boolean.class) {
                ACFUtil.sneaky(new InvalidCommandArgument("Flag parameters must be boolean"));
            }
            this.isFlag = true;
            this.flag = annotations.getAnnotationValue(param, Flag.class, Annotations.REPLACEMENTS | Annotations.NO_EMPTY);
            this.flagAliases = new String[0];

            if (flag.contains(ACFPatterns.PIPE.pattern())) {
                flagAliases = ACFPatterns.PIPE.split(flag);
                flag = flagAliases[0];
            }
        }

        //noinspection unchecked
        this.resolver = manager.getCommandContexts().getResolver(type);
        if (this.resolver == null) {
            ACFUtil.sneaky(new InvalidCommandContextException(
                    "Parameter " + type.getSimpleName() + " of " + command + " has no applicable context resolver"
            ));
        }

        this.optional = annotations.hasAnnotation(param, Optional.class) || this.defaultValue != null || (isLast && type == String[].class);
        this.permission = annotations.getAnnotationValue(param, CommandPermission.class, Annotations.REPLACEMENTS | Annotations.NO_EMPTY);
        this.optionalResolver = isOptionalResolver(resolver);
        this.requiresInput = !this.optional && !this.optionalResolver && !this.isFlag;
        //noinspection unchecked
        this.commandIssuer = paramIndex == 0 && manager.isCommandIssuer(type);
        this.canConsumeInput = !this.commandIssuer && !(resolver instanceof IssuerOnlyContextResolver) || this.isFlag;
        this.consumesRest = isLast && ((type == String.class && !annotations.hasAnnotation(param, Single.class)) || (type == String[].class));

        this.values = annotations.getAnnotationValues(param, Values.class, Annotations.REPLACEMENTS | Annotations.NO_EMPTY);

        this.syntax = null;
        this.isOptionalInput = !requiresInput && canConsumeInput;

        if (!commandIssuer) {
            this.syntax = annotations.getAnnotationValue(param, Syntax.class);
        }

        this.computePermissions();
    }

    public boolean isFlag() {
        return isFlag;
    }

    private void computePermissions() {
        this.permissions.clear();
        if (this.permission != null && !this.permission.isEmpty()) {
            this.permissions.addAll(Arrays.asList(ACFPatterns.COMMA.split(this.permission)));
        }
    }

    private boolean isOptionalResolver(ContextResolver<?, CEC> resolver) {
        return resolver instanceof IssuerAwareContextResolver
                || resolver instanceof IssuerOnlyContextResolver
                || resolver instanceof OptionalContextResolver;
    }


    public void setResolver(ContextResolver<?, CEC> resolver) {
        this.resolver = resolver;
    }

    public boolean isOptionalInput() {
        return isOptionalInput;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setCommandIssuer(boolean commandIssuer) {
        this.commandIssuer = commandIssuer;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean canConsumeInput() {
        return canConsumeInput;
    }

    public void setCanConsumeInput(boolean canConsumeInput) {
        this.canConsumeInput = canConsumeInput;
    }

    public void setOptionalResolver(boolean optionalResolver) {
        this.optionalResolver = optionalResolver;
    }

    public boolean requiresInput() {
        return requiresInput;
    }

    public void setRequiresInput(boolean requiresInput) {
        this.requiresInput = requiresInput;
    }

    public String getSyntax() {
        if (syntax == null) {
            if (isFlag) {
                return "(-" + flag + ")";
            }

            if (isOptionalInput) {
                return "[" + name + "]";
            } else if (requiresInput) {
                return "<" + name + ">";
            }
        }
        return syntax;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Set<String> getRequiredPermissions() {
        return permissions;
    }

    public void setNextParam(CommandParameter<CEC> nextParam) {
        this.nextParam = nextParam;
    }

    public boolean canExecuteWithoutInput() {
        return (!canConsumeInput || isOptionalInput()) && (nextParam == null || nextParam.canExecuteWithoutInput());
    }

    public boolean isLast() {
        return isLast;
    }
}
