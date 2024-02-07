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

import org.bukkit.plugin.Plugin;

@SuppressWarnings("WeakerAccess")
public class PaperCommandManager extends BukkitCommandManager {

    private boolean brigadierAvailable;

    // If we get anything Paper specific
    public PaperCommandManager(Plugin plugin) {
        super(plugin);
        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            plugin.getServer().getPluginManager().registerEvents(new PaperAsyncTabCompleteHandler(this), plugin);
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent");
            brigadierAvailable = true;
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
    }

    public PaperCommandManager(Plugin plugin, PaperCommandManager copyFrom) {
        super(plugin, copyFrom);
        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            plugin.getServer().getPluginManager().registerEvents(new PaperAsyncTabCompleteHandler(this), plugin);
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent");
            brigadierAvailable = true;
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
    }

    public void enableBrigadier() {
        if (brigadierAvailable) {
            new PaperBrigadierManager(plugin, this);
        }
    }

    @Override
    public synchronized CommandContexts<BukkitCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new PaperCommandContexts(this);
        }
        return this.contexts;
    }

    @Override
    public synchronized CommandCompletions<BukkitCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new PaperCommandCompletions(this);
        }
        return this.completions;
    }
}
