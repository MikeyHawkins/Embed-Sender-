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
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.Color;

public class EmbedCommand implements CommandExecutor {

    private static final Logger logger = LogManager.getLogger(EmbedCommand.class);

    @Command(aliases = {Constants.PREFIX + "embed"}, async = true, description = "Create a custom embed message", usage = "/embed <title> | <description>")
    public void onCommand(TextChannel channel, Message message, String[] args) {

        if (args.length == 0) {
            channel.sendMessage("Usage: `/embed <title> | <description>`\nExample: `/embed Welcome | This is an example embed`");
            return;
        }

        String fullMessage = String.join(" ", args);
        String[] parts = fullMessage.split("\\|", 2);

        String title = parts.length > 0 ? parts[0].trim() : "Embed";
        String description = parts.length > 1 ? parts[1].trim() : "";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#005A9C"));

        channel.sendMessage(embed);
        logger.info(message.getAuthor() + " used embed command");
    }
}
