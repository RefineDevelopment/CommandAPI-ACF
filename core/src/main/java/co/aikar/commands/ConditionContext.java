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

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ConditionContext <I extends CommandIssuer> {

    @Getter
    private final I issuer;
    @Getter
    private final String config;
    private final Map<String, String> configs;

    ConditionContext(I issuer, String config) {
        this.issuer = issuer;
        this.config = config;
        this.configs = new HashMap<>();
        if (config != null) {
            for (String s : ACFPatterns.COMMA.split(config)) {
                String[] v = ACFPatterns.EQUALS.split(s, 2);
                this.configs.put(v[0], v.length > 1 ? v[1] : null);
            }
        }
    }


    public boolean hasConfig(String flag) {
        return configs.containsKey(flag);
    }

    public String getConfigValue(String flag, String def) {
        return configs.getOrDefault(flag, def);
    }

    public Integer getConfigValue(String flag, Integer def) {
        return ACFUtil.parseInt(this.configs.get(flag), def);
    }
}
