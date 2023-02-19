package co.aikar.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Clickable {

    private TextComponent component = null;

    public Clickable(String msg, String hoverMsg, String clickString, String suggestString) {
        TextComponent message = new TextComponent(TextComponent.fromLegacyText(msg));

        if (hoverMsg != null && !hoverMsg.equalsIgnoreCase("")) {
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverMsg)));
        }

        if (clickString != null && !clickString.equalsIgnoreCase("")) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickString));
        }

        if (suggestString != null && !suggestString.equalsIgnoreCase("")) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestString));
        }

        component = message;
    }

    public void sendToPlayer(ProxiedPlayer player) {
        player.sendMessage(component);
    }
}
