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

import lombok.experimental.UtilityClass;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
@UtilityClass
final class ACFPatterns {
    public final Pattern COMMA = Pattern.compile(",");
    public final Pattern PERCENTAGE = Pattern.compile("%", Pattern.LITERAL);
    public final Pattern NEWLINE = Pattern.compile("\n");
    public final Pattern DASH = Pattern.compile("-");
    public final Pattern SPACE = Pattern.compile(" ");
    public final Pattern COLON = Pattern.compile(":");
    public final Pattern COLONEQUALS = Pattern.compile("([:=])");
    public final Pattern PIPE = Pattern.compile("\\|");
    public final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^a-zA-Z0-9]");
    public final Pattern INTEGER = Pattern.compile("^[0-9]+$");
    public final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_$]{1,16}$");
    public final Pattern NON_PRINTABLE_CHARACTERS = Pattern.compile("[^\\x20-\\x7F]");
    public final Pattern EQUALS = Pattern.compile("=");
    public final Pattern REPLACEMENT_PATTERN = Pattern.compile("%\\{.[^\\s]*}");

    final Map<String, Pattern> patternCache = ExpiringMap.builder()
            .maxSize(200)
            .expiration(1, TimeUnit.HOURS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .build();

    /**
     * Gets a pattern and compiles it.
     * If the pattern is stored already in {@link #patternCache}, it will simply fetch it from there.
     * If it is not, it will store it there for further use.
     * <p>
     * The {@link #patternCache} does not contain the constant patterns defined in this class.
     *
     * @param pattern The raw pattern in a String.
     * @return The pattern which has been cached.
     */
    public Pattern getPattern(String pattern) {
        return patternCache.computeIfAbsent(pattern, s -> Pattern.compile(pattern));
    }
}
