package co.aikar.commands;


import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Clickable {

    private TextComponent component = null;

    public Clickable(String msg, String hoverMsg, String clickString, String suggestString) {
        TextComponent message = LegacyComponentSerializer.legacySection().deserialize(msg);

        if (hoverMsg != null && !hoverMsg.equalsIgnoreCase("")) {
            message.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, LegacyComponentSerializer.legacySection().deserialize(hoverMsg)));
        }

        if (clickString != null && !clickString.equalsIgnoreCase("")) {
            message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickString));
        }

        if (suggestString != null && !suggestString.equalsIgnoreCase("")) {
            message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestString));
        }

        component = message;
    }

    public void sendToPlayer(CommandSource player) {
        player.sendMessage(component);
    }
}
