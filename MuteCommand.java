/*
Ducky - A web search utility with other features.
Copyright (C) 2021 KoxSosen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.github.koxsosen.commands;

import com.github.koxsosen.Constants;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand implements CommandExecutor {

    private static final Logger logger = LogManager.getLogger(MuteCommand.class);
    private static final long REQUIRED_ROLE_ID = 1350907647217635389L;
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Command(aliases = {Constants.PREFIX + "mute"}, async = true, description = "Mute a user for a specified time", usage = "/mute @user <time>")
    public void onCommand(TextChannel channel, Message message, String[] args) {

        if (!message.getUserAuthor().isPresent()) {
            return;
        }

        User author = message.getUserAuthor().get();
        
        boolean hasPermission = message.getServer()
            .flatMap(server -> server.getRoleById(REQUIRED_ROLE_ID))
            .map(role -> author.getRoles(message.getServer().get()).contains(role))
            .orElse(false);

        if (!hasPermission) {
            channel.sendMessage("❌ You don't have permission to use this command!");
            logger.info(author.getDiscriminatedName() + " tried to use mute command without permission");
            return;
        }

        if (args.length < 2) {
            channel.sendMessage("Usage: `/mute @user <time>`\nExample: `/mute @user 10m` (s=seconds, m=minutes, h=hours, d=days)");
            return;
        }

        if (message.getMentionedUsers().isEmpty()) {
            channel.sendMessage("❌ Please mention a user to mute!");
            return;
        }

        User targetUser = message.getMentionedUsers().get(0);
        String timeString = args[1].toLowerCase();

        Long delaySeconds = parseTimeToSeconds(timeString);
        if (delaySeconds == null) {
            channel.sendMessage("❌ Invalid time format! Use: 10s, 5m, 2h, or 1d");
            return;
        }

        message.getServer().ifPresent(server -> {
            server.muteUser(targetUser).thenAccept(v -> {
                channel.sendMessage("✅ Voice muted " + targetUser.getMentionTag() + " for " + timeString + " (they must be in a voice channel)");
                logger.info(author.getDiscriminatedName() + " muted " + targetUser.getDiscriminatedName() + " for " + timeString);
                
                scheduler.schedule(() -> {
                    server.unmuteUser(targetUser).thenAccept(unmuted -> {
                        channel.sendMessage("🔊 " + targetUser.getMentionTag() + " has been automatically unmuted");
                        logger.info("Auto-unmuted " + targetUser.getDiscriminatedName() + " after " + timeString);
                    }).exceptionally(ex -> {
                        logger.error("Failed to auto-unmute " + targetUser.getDiscriminatedName() + ": " + ex.getMessage());
                        return null;
                    });
                }, delaySeconds, TimeUnit.SECONDS);
                
            }).exceptionally(e -> {
                channel.sendMessage("❌ Failed to mute user. Make sure the bot has proper permissions and the user is in a voice channel.");
                logger.error("Failed to mute user: " + e.getMessage());
                return null;
            });
        });
    }

    private Long parseTimeToSeconds(String timeString) {
        Matcher matcher = TIME_PATTERN.matcher(timeString);
        if (!matcher.matches()) {
            return null;
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        switch (unit) {
            case "s":
                return value;
            case "m":
                return value * 60;
            case "h":
                return value * 3600;
            case "d":
                return value * 86400;
            default:
                return null;
        }
    }
}

