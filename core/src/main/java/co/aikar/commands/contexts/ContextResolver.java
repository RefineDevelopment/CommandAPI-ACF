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

package co.aikar.commands.contexts;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;

/**
 * This defines a context resolver, which parses {@link T} from {@link C}.
 *
 * @param <T>
 *         The type to be parsed.
 * @param <C>
 *         The type of the context which the resolver would get its data from.
 */
@FunctionalInterface
public interface ContextResolver <T, C extends CommandExecutionContext<? extends CommandIssuer>> {
    /**
     * Parses the context of type {@link C} into {@link T}, or throws an exception.
     *
     * @param c
     *         The context to parse from.
     *
     * @return The parsed instance of the wanted type.
     *
     * @throws InvalidCommandArgument
     *         In case the context contains any discrepancies, it will throw this exception.
     */
    T getContext(C c) throws InvalidCommandArgument;
}
