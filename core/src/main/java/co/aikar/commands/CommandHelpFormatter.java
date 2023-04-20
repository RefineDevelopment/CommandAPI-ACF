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

import co.aikar.commands.config.impl.MessageConfig;

import java.util.ArrayList;
import java.util.List;

public class CommandHelpFormatter {

    private final CommandManager manager;

    public CommandHelpFormatter(CommandManager manager) {
        this.manager = manager;
    }


    public void showAllResults(CommandHelp commandHelp, List<HelpEntry> entries) {
        CommandIssuer issuer = commandHelp.getIssuer();
        printHelpHeader(commandHelp, issuer);
        for (HelpEntry e : entries) {
            printHelpCommand(commandHelp, issuer, e);
        }
        printHelpFooter(commandHelp, issuer);
    }

    public void showSearchResults(CommandHelp commandHelp, List<HelpEntry> entries) {
        CommandIssuer issuer = commandHelp.getIssuer();
        printSearchHeader(commandHelp, issuer);
        for (HelpEntry e : entries) {
            printHelpCommand(commandHelp, issuer, e);
        }
        printHelpFooter(commandHelp, issuer);
    }

    // ########
    // # help #
    // ########

    public void printHelpHeader(CommandHelp help, CommandIssuer issuer) {
        issuer.sendMessage(getReplacedHeaderFooter(MessageConfig.IMP.HELP.HEADER, help));
    }

    public void printSearchHeader(CommandHelp help, CommandIssuer issuer) {
        issuer.sendMessage(getReplacedHeaderFooter(MessageConfig.IMP.HELP.SEARCH_HEADER, help));
    }

    public void printHelpFooter(CommandHelp help, CommandIssuer issuer) {
        if (help.isOnlyPage()) {
            return;
        }

        List<ClickablePart> clickableParts = new ArrayList<>();

        String msg = MessageConfig.IMP.HELP.FOOTER;

        if (msg.contains("<previousPage>")) {
            msg = msg.replace("<previousPage>", "");
            if (help.getPage() > 1) {
                String previousPageCommand = help.getCommandPrefix() + help.getCommandName() + " " + (help.getPage() - 1);
                clickableParts.add(new ClickablePart(MessageConfig.IMP.HELP.PREVIOUS_PAGE, MessageConfig.IMP.HELP.PREVIOUS_PAGE_HOVER, previousPageCommand, ""));
            }
        }

        clickableParts.add(new ClickablePart(getReplacedHeaderFooter(msg.replace("<nextPage>", ""), help), "", "", ""));

        if (msg.contains("<nextPage>")) {
            if (help.getPage() < help.getTotalPages()) {
                String nextPageCommand = help.getCommandPrefix() + help.getCommandName() + " " + (help.getPage() + 1);
                clickableParts.add(new ClickablePart(MessageConfig.IMP.HELP.NEXT_PAGE, MessageConfig.IMP.HELP.NEXT_PAGE_HOVER, nextPageCommand, ""));
            }
        }

        issuer.sendClickablesSameLine(clickableParts);
    }


    public void printHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        String formatted = getReplacedFormat(help, entry);
        for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
            issuer.sendClickable(ACFUtil.rtrim(msg), "", "", entry.getCommandPrefix() + entry.getCommand());
        }
    }

    public String getReplacedHeaderFooter(String message, CommandHelp help) {
        return message
                .replace("<search>", help.search != null ? String.join(" ", help.search) : "")
                .replace("<command>", help.getCommandName())
                .replace("<commandPrefix>", help.getCommandPrefix())
                .replace("<page>", String.valueOf(help.getPage()))
                .replace("<totalPages>", String.valueOf(help.getTotalPages()))
                .replace("<results>", String.valueOf(help.getTotalResults()));
    }

    public String getReplacedFormat(CommandHelp help, HelpEntry entry) {
        if (entry.getDescription().isEmpty()) {
            return MessageConfig.IMP.HELP.FORMAT
                    .replace("<command>", entry.getCommand())
                    .replace("<commandPrefix>", help.getCommandPrefix())
                    .replace("<parameters>", entry.getParameterSyntax());
        } else {
            return MessageConfig.IMP.HELP.FORMAT_WITH_DESCRIPTION
                    .replace("<command>", entry.getCommand())
                    .replace("<commandPrefix>", help.getCommandPrefix())
                    .replace("<parameters>", entry.getParameterSyntax())
                    .replace("<description>", entry.getDescription());
        }
    }
}
