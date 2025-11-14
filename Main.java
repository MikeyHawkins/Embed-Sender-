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

package com.github.koxsosen;

import de.btobastian.sdcf4j.CommandHandler;
import com.github.koxsosen.listeners.DuckyMSG;
import com.github.koxsosen.listeners.SlashCommandListener;
import com.github.koxsosen.debug.DebugCommand;
import com.github.koxsosen.commands.WebSearch;
import com.github.koxsosen.commands.CatCommand;
import com.github.koxsosen.commands.DogCommand;
import com.github.koxsosen.commands.DuckCommand;
import com.github.koxsosen.commands.HelpCommand;
import com.github.koxsosen.commands.InviteCommand;
import com.github.koxsosen.commands.PasteCommand;
import com.github.koxsosen.commands.WebsiteCommand;
import com.github.koxsosen.commands.EmbedCommand;
import com.github.koxsosen.commands.MuteCommand;
import com.github.koxsosen.commands.KickCommand;
import com.github.koxsosen.commands.BanCommand;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.DiscordApi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.entity.permission.PermissionType;

import java.util.Arrays;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        FallbackLoggerConfiguration.setDebug(false);

        DiscordApi api = new DiscordApiBuilder()
                .setToken(Constants.TOKEN)
                .setWaitForServersOnStartup(false)
                .setAllNonPrivilegedIntentsExcept(
                        Intent.GUILD_EMOJIS,
                        Intent.GUILD_BANS,
                        Intent.GUILD_INVITES,
                        Intent.DIRECT_MESSAGES,
                        Intent.GUILD_INTEGRATIONS,
                        Intent.GUILD_WEBHOOKS,
                        Intent.DIRECT_MESSAGE_REACTIONS,
                        Intent.DIRECT_MESSAGE_TYPING,
                        Intent.GUILD_MESSAGE_TYPING,
                        Intent.GUILD_VOICE_STATES) // Disable unneeded Intents.
                .login().join();
                // If the bot disconnects always reconnect with a 2*sec delay. ( 1st: 2s, 2nd:4s )
                api.setReconnectDelay(attempt -> attempt * 2);
                // Only cache 10 messages per channel & remove ones older than 15 min.
                api.setMessageCacheSize(10, 30*30);
        
        // Rotating status setup
        String[][] statuses = {
            {"PLAYING", "ERLC"},
            {"WATCHING", "Members"},
            {"WATCHING", "Bob"},
            {"PLAYING", "Made by Mikey"}
        };
        
        AtomicInteger statusIndex = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            int index = statusIndex.getAndIncrement() % statuses.length;
            String type = statuses[index][0];
            String status = statuses[index][1];
            api.updateActivity(ActivityType.valueOf(type), status);
            logger.info("Updated status to: " + type + " " + status);
        }, 0, 10, TimeUnit.SECONDS);

        // Register commands
        CommandHandler handler = new JavacordHandler(api);
        handler.registerCommand(new WebsiteCommand());
        handler.registerCommand(new InviteCommand());
        handler.registerCommand(new PasteCommand());
        handler.registerCommand(new HelpCommand());
        handler.registerCommand(new WebSearch());
        handler.registerCommand(new CatCommand());
        handler.registerCommand(new DuckCommand());
        handler.registerCommand(new DogCommand());
        handler.registerCommand(new DebugCommand());
        handler.registerCommand(new EmbedCommand());
        handler.registerCommand(new MuteCommand());
        handler.registerCommand(new KickCommand());
        handler.registerCommand(new BanCommand());

        api.addMessageCreateListener(new DuckyMSG(handler));
        
        // Add slash command listener
        api.addSlashCommandCreateListener(new SlashCommandListener());

        // Register Slash Commands
        logger.info("Registering slash commands...");
        
        SlashCommand.with("mute", "Mute a user in voice for a specified time",
            Arrays.asList(
                SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The user to mute", true),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "time", "Duration (e.g., 10m, 2h, 1d)", true)
            ))
            .createGlobal(api)
            .join();

        SlashCommand.with("kick", "Kick a user from the server",
            Arrays.asList(
                SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The user to kick", true),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "reason", "Reason for kick", false)
            ))
            .createGlobal(api)
            .join();

        SlashCommand.with("ban", "Ban a user from the server",
            Arrays.asList(
                SlashCommandOption.create(SlashCommandOptionType.USER, "user", "The user to ban", true),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "reason", "Reason for ban", false)
            ))
            .createGlobal(api)
            .join();

        SlashCommand.with("embed", "Create a custom embed message",
            Arrays.asList(
                SlashCommandOption.create(SlashCommandOptionType.STRING, "title", "Embed title", true),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "description", "Embed description", false)
            ))
            .createGlobal(api)
            .join();

        logger.info("Slash commands registered!");
        logger.info("The bots prefix is " + Constants.PREFIX());
        logger.info("Bot is using rotating status (ERLC, Members, Bob, Made by Mikey) every 10 seconds");
        logger.info("Logged in as " + api.getYourself() + ".");
        }

}