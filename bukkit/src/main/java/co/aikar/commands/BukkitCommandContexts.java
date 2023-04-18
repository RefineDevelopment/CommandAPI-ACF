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

import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import co.aikar.commands.config.impl.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static co.aikar.commands.ACFBukkitUtil.isValidName;

@SuppressWarnings("WeakerAccess")
public class BukkitCommandContexts extends CommandContexts<BukkitCommandExecutionContext> {

    public BukkitCommandContexts(BukkitCommandManager manager) {
        super(manager);

        registerContext(OnlinePlayer.class, c -> getOnlinePlayer(c.getIssuer(), c.popFirstArg(), false));
        registerContext(co.aikar.commands.contexts.OnlinePlayer.class, c -> {
            OnlinePlayer onlinePlayer = getOnlinePlayer(c.getIssuer(), c.popFirstArg(), false);
            return new co.aikar.commands.contexts.OnlinePlayer(onlinePlayer.getPlayer());
        });
        registerContext(OnlinePlayer[].class, (c) -> {
            BukkitCommandIssuer issuer = c.getIssuer();
            final String search = c.popFirstArg();
            boolean allowMissing = c.hasFlag("allowmissing");
            Set<OnlinePlayer> players = new HashSet<>();
            Pattern split = ACFPatterns.COMMA;
            String splitter = c.getFlagValue("splitter", (String) null);
            if (splitter != null) {
                split = Pattern.compile(Pattern.quote(splitter));
            }
            for (String lookup : split.split(search)) {
                OnlinePlayer player = getOnlinePlayer(issuer, lookup, allowMissing);
                if (player != null) {
                    players.add(player);
                }
            }
            if (players.isEmpty() && !c.hasFlag("allowempty")) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.NO_ONLINE_PLAYER_FOUND.replace("<search>", search), false);
            }
            return players.toArray(new OnlinePlayer[players.size()]);
        });
        registerIssuerAwareContext(World.class, (c) -> {
            String firstArg = c.getFirstArg();
            World world = firstArg != null ? Bukkit.getWorld(firstArg) : null;
            if (world != null) {
                c.popFirstArg();
            }
            if (world == null && c.getSender() instanceof Player) {
                world = ((Entity) c.getSender()).getWorld();
            }
            if (world == null) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.INVALID_WORLD);
            }
            return world;
        });
        registerIssuerAwareContext(CommandSender.class, BukkitCommandExecutionContext::getSender);
        registerIssuerAwareContext(Player.class, (c) -> {
            boolean isOptional = c.isOptional();
            CommandSender sender = c.getSender();
            boolean isPlayerSender = sender instanceof Player;
            if (!c.hasFlag("other")) {
                Player player = isPlayerSender ? (Player) sender : null;
                if (player == null && !isOptional) {
                    throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.PLAYER_ONLY, false);
                }
                PlayerInventory inventory = player != null ? player.getInventory() : null;
                if (inventory != null && c.hasFlag("itemheld") && !ACFBukkitUtil.isValidItem(inventory.getItem(inventory.getHeldItemSlot()))) {
                    throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.MUST_HOLD_ITEM, false);
                }
                return player;
            } else {
                String arg = c.popFirstArg();
                if (arg == null && isOptional) {
                    if (c.hasFlag("defaultself")) {
                        if (isPlayerSender) {
                            return (Player) sender;
                        } else {
                            throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.PLAYER_ONLY, false);
                        }
                    } else {
                        return null;
                    }
                } else if (arg == null) {
                    throw new InvalidCommandArgument();
                }

                OnlinePlayer onlinePlayer = getOnlinePlayer(c.getIssuer(), arg, false);
                return onlinePlayer.getPlayer();
            }
        });
        registerContext(OfflinePlayer.class, c -> {
            String name = c.popFirstArg();
            OfflinePlayer offlinePlayer;
            if (c.hasFlag("uuid")) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(name);
                } catch (IllegalArgumentException e) {
                    throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.NO_OFFLINE_PLAYER_FOUND.replace("<search>", name));
                }
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } else {
                offlinePlayer = Bukkit.getOfflinePlayer(name);
            }
            if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                if (!c.hasFlag("uuid") && !isValidName(name)) {
                    throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.INVALID_USERNAME.replace("<name>", name));
                }
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLAYER.NO_OFFLINE_PLAYER_FOUND.replace("<search>", name));
            }
            return offlinePlayer;
        });
        registerContext(ChatColor.class, c -> {
            String first = c.popFirstArg();
            Stream<ChatColor> colors = Stream.of(ChatColor.values());
            if (c.hasFlag("colorsonly")) {
                colors = colors.filter(color -> color.ordinal() <= 0xF);
            }
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                colors = colors.filter(color -> finalFilter.equals(ACFUtil.simplifyString(color.name())));
            }

            ChatColor match = ACFUtil.simpleMatch(ChatColor.class, first);
            if (match == null) {
                String valid = colors.map(color -> "<c2>" + ACFUtil.simplifyString(color.name()) + "</c2>").collect(Collectors.joining("<c1>,</c1> "));

                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.PLEASE_SPECIFY_ONE_OF.replace("<valid>", valid));
            }
            return match;
        });
        registerContext(Location.class, c -> {
            String input = c.popFirstArg();
            CommandSender sender = c.getSender();
            String[] split = ACFPatterns.COLON.split(input, 2);
            if (split.length == 0) {
                throw new InvalidCommandArgument(true);
            }
            if (split.length < 2 && !(sender instanceof Player) && !(sender instanceof BlockCommandSender)) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.SPECIFY_WORLD);
            }
            final String world;
            final String rest;
            Location sourceLoc = null;
            if (split.length == 2) {
                world = split[0];
                rest = split[1];
            } else if (sender instanceof Player) {
                sourceLoc = ((Player) sender).getLocation();
                world = sourceLoc.getWorld().getName();
                rest = split[0];
            } else if (sender instanceof BlockCommandSender) {
                sourceLoc = ((BlockCommandSender) sender).getBlock().getLocation();
                world = sourceLoc.getWorld().getName();
                rest = split[0];
            } else {
                throw new InvalidCommandArgument(true);
            }

            boolean rel = rest.startsWith("~");
            split = ACFPatterns.COMMA.split(rel ? rest.substring(1) : rest);
            if (split.length < 3) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.SPECIFY_COORDS);
            }

            Double x = ACFUtil.parseDouble(split[0], rel ? 0.0D : null);
            Double y = ACFUtil.parseDouble(split[1], rel ? 0.0D : null);
            Double z = ACFUtil.parseDouble(split[2], rel ? 0.0D : null);

            if (sourceLoc != null && rel) {
                x += sourceLoc.getX();
                y += sourceLoc.getY();
                z += sourceLoc.getZ();
            } else if (rel) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.CONSOLE_RELATIVE);
            }

            if (x == null || y == null || z == null) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.SPECIFY_COORDS);
            }

            World worldObj = Bukkit.getWorld(world);
            if (worldObj == null) {
                throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.INVALID_WORLD);
            }

            if (split.length >= 5) {
                Float yaw = ACFUtil.parseFloat(split[3]);
                Float pitch = ACFUtil.parseFloat(split[4]);

                if (pitch == null || yaw == null) {
                    throw new InvalidCommandArgument(MessageConfig.IMP.ERROR.LOCATION.SPECIFY_COORDS);
                }
                return new Location(worldObj, x, y, z, yaw, pitch);
            } else {
                return new Location(worldObj, x, y, z);
            }
        });

        if (manager.mcMinorVersion >= 12) {
            BukkitCommandContexts_1_12.register(this);
        }
    }

    @Contract("_,_,false -> !null")
    OnlinePlayer getOnlinePlayer(BukkitCommandIssuer issuer, String lookup, boolean allowMissing) throws InvalidCommandArgument {
        Player player = ACFBukkitUtil.findPlayerSmart(issuer, lookup);
        //noinspection Duplicates
        if (player == null) {
            if (allowMissing) {
                return null;
            }
            throw new InvalidCommandArgument(false);
        }
        return new OnlinePlayer(player);
    }
}
