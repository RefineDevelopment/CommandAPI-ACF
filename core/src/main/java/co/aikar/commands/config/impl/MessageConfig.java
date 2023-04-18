/*
 * Copyright (c) 2016-2023 Daniel Ennis (Aikar) - MIT License
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

package co.aikar.commands.config.impl;

import co.aikar.commands.config.api.ConfigUtil;

import java.io.File;

/**
 * @author Creaxx
 * Created At: 4/18/2023
 */
public class MessageConfig extends ConfigUtil {

    @Ignore
    public static MessageConfig IMP;

    @Create public ERROR ERROR;
    @Create public FORMATS FORMATS;
    @Create public HELP HELP;

    public void reload(String pluginName) {
        File file = new File("plugins/" + pluginName + "/acf-messages.yml");
        load(file);
        save(file);
    }

    public void createConfig(String pluginName) {
        IMP = this;
        reload(pluginName);
    }

    public static class ERROR {
        public String MUST_HOLD_ITEM = "&cYou must be holding an item in your main hand.";
        public String NO_PERMISSION = "&cI'm sorry, but you do not have permission to perform this command.";
        public String GENERIC_ERROR_LOGGED = "&cAn error occurred. This problem has been logged. Sorry for the inconvenience.";
        public String UNKNOWN_COMMAND = "&cUnknown Command, please type &e/help";
        public String INVALID_SYNTAX = "&fUsage: &c<command> &f<syntax>";
        public String ERROR_PERFORMING_COMMAND = "&cI'm sorry, but there was an error performing this command.";
        public String PLEASE_SPECIFY_ONE_OF = "&cPlease specify one of (<valid>).";
        public String MUST_BE_A_NUMBER = "&c<num> &cmust be a number.";
        public String LENGTH_TOO_LARGE = "&cMust be at least &f<min> &ccharacters long.";
        public String LENGTH_TOO_SMALL = "&cMust be at most &f<max> &ccharacters long.";
        public String PLEASE_SPECIFY_AT_MOST = "&cPlease specify a value at most &f<max>&c.";
        public String PLEASE_SPECIFY_AT_LEAST = "&cPlease specify a value at least &f<min>&c.";
 
        @Create public PLAYER PLAYER;
        @Create public LOCATION LOCATION;
    }
    
    public static class PLAYER {
        public String USERNAME_TOO_SHORT = "&cUsername too short, must be at least three characters.";
        public String INVALID_USERNAME = "&c<name> &cis not a valid username.";
        public String MULTIPLE_PLAYERS_MATCH = "&cMultiple players matched &f<search> &c(<all>), please be more specific.";
        public String NO_ONLINE_PLAYER_FOUND = "&cNo player matching &f<search> &cis connected to this server.";
        public String NO_OFFLINE_PLAYER_FOUND = "&cNo player matching &f<search> &ccould be found.";
        public String NO_PLAYER_FOUND = "&cNo player matching &f<search> &c&ccould be found.";
        public String PLAYER_NOT_FOUND = "&cCould not find a player by the name: &f<search>";

        public String PLAYER_ONLY = "&cConsole may not execute this command.";
    }
    
    public static class LOCATION {
        public String INVALID_WORLD = "&cThat world does not exists.";
        public String SPECIFY_WORLD = "&cPlease specify world. Example: world:x,y,z.";
        public String SPECIFY_COORDS = "&cPlease specify the coordinates x, y and z. Example: world:x,y,z.";
        public String CONSOLE_RELATIVE = "&cConsole may not use relative coordinates for location.";
    }

    public static class FORMATS {
        public String ERROR_MESSAGE = "&4&lError &7» &c<message>";
        public String INFO_MESSAGE = "&c<message>";

    }

    public static class HELP {
        public String NO_COMMANDS_MATCHED_SEARCH = "&cNo command matched &f<search>&c.";
        public String NO_RESULTS = "&cNo more results.";

        public String HEADER = "&c=== &fShowing help for &c<commandPrefix><command>&f. &c===";
        public String SEARCH_HEADER = "&c=== &fSearch results for &c<commandPrefix><command> &f<search> &c===";
        public String FORMAT = "&f<command> &c<parameters>";
        public String FORMAT_WITH_DESCRIPTION = "&f<command> &c<parameters> &7&o(<description>)";
        public String FOOTER = "&fShowing page &f<page> &fof &f<totalPages> &7(<results> results).";
    }
}
