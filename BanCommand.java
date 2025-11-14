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

public class BanCommand implements CommandExecutor {

    private static final Logger logger = LogManager.getLogger(BanCommand.class);
    private static final long REQUIRED_ROLE_ID = 1350907647217635389L;

    @Command(aliases = {Constants.PREFIX + "ban"}, async = true, description = "Ban a user from the server", usage = "/ban @user <reason>")
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
            logger.info(author.getDiscriminatedName() + " tried to use ban command without permission");
            return;
        }

        if (message.getMentionedUsers().isEmpty()) {
            channel.sendMessage("❌ Please mention a user to ban!");
            return;
        }

        User targetUser = message.getMentionedUsers().get(0);
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";

        message.getServer().ifPresent(server -> {
            server.banUser(targetUser, java.time.Duration.ofDays(0), reason).thenAccept(v -> {
                channel.sendMessage("✅ Banned " + targetUser.getDiscriminatedName() + " | Reason: " + reason);
                logger.info(author.getDiscriminatedName() + " banned " + targetUser.getDiscriminatedName() + " for: " + reason);
            }).exceptionally(e -> {
                channel.sendMessage("❌ Failed to ban user: " + e.getMessage());
                return null;
            });
        });
    }
}
