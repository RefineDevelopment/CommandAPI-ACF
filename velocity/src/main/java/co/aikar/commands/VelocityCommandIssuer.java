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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class VelocityCommandIssuer implements CommandIssuer {
    private final VelocityCommandManager manager;
    private final CommandSource issuer;

    public Player getPlayer() {
        return isPlayer() ? (Player) issuer : null;
    }

    @Override
    public boolean isPlayer() {
        return issuer instanceof Player;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if (isPlayer()) {
            return ((Player) issuer).getUniqueId();
        }

        // TODO: Find a better solution for this
        //generate a unique id based of the name (like for the console command sender)
        return UUID.randomUUID();
    }

    @Override
    public void sendMessageInternal(String message) {
        issuer.sendMessage(ACFVelocityUtil.color(message));
    }

    @Override
    public boolean hasPermission(String name) {
        return issuer.hasPermission(name);
    }

    @Override
    public void sendClickable(String message, String hover, String command, String suggest) {
        new Clickable(message, hover, command, suggest).sendToPlayer(issuer);
    }

    @Override
    public void sendClickablesSameLine(List<ClickablePart> clickableParts) {
        Clickable clickable = new Clickable();

        for (ClickablePart clickablePart : clickableParts) {
            clickable.add(clickablePart.getMessage(), clickablePart.getHover(), clickablePart.getCommand(), clickablePart.getSuggest());
        }

        clickable.sendToPlayer(issuer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityCommandIssuer that = (VelocityCommandIssuer) o;
        return Objects.equals(issuer, that.issuer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer);
    }
}
