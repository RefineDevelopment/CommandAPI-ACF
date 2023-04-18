package co.aikar.commands;

import co.aikar.commands.config.impl.MessageConfig;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ACFVelocityUtil {

    public static TextComponent color(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(message);
    }

    public static Player findPlayerSmart(ProxyServer server, CommandIssuer issuer, String search) {
        CommandSource requester = issuer.getIssuer();
        String name = ACFUtil.replace(search, ":confirm", "");

        List<Player> matches = new ArrayList<>(matchPlayer(server, name));

        if (matches.size() > 1) {
            String allMatches = matches.stream().map(Player::getUsername).collect(Collectors.joining(", "));
            issuer.sendError(MessageConfig.IMP.ERROR.PLAYER.MULTIPLE_PLAYERS_MATCH
                    .replace("<search>", name)
                    .replace("<all>", allMatches));
            return null;
        }

        if (matches.isEmpty()) {
            if (!isValidName(name)) {
                issuer.sendError(MessageConfig.IMP.ERROR.PLAYER.INVALID_USERNAME.replace("<name>", name));
                return null;
            }

            issuer.sendError(MessageConfig.IMP.ERROR.PLAYER.NO_ONLINE_PLAYER_FOUND.replace("<search>", name));
            return null;
        }
        return matches.get(0);
    }

    /*
     * Original code written by md_5
     *
     * Modified to work with Velocity by Crypnotic
     */
    private static Collection<Player> matchPlayer(ProxyServer server, final String partialName) {
        // A better error message might be nice. This just mimics the previous output
        if (partialName == null) {
            throw new NullPointerException("partialName");
        }

        Optional<Player> exactMatch = server.getPlayer(partialName);
        if (exactMatch.isPresent()) {
            return Collections.singleton(exactMatch.get());
        }

        return server.getAllPlayers().stream()
                .filter(player -> player.getUsername().regionMatches(true, 0, partialName, 0, partialName.length()))
                .collect(Collectors.toList());
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    public static <T> T validate(T object, String message, Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
        return object;
    }
}
