package com.github.koxsosen.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlashCommandListener implements SlashCommandCreateListener {

    private static final Logger logger = LogManager.getLogger(SlashCommandListener.class);
    private static final long REQUIRED_ROLE_ID = 1350907647217635389L;
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        String commandName = interaction.getCommandName();

        switch (commandName) {
            case "mute":
                handleMuteCommand(interaction);
                break;
            case "kick":
                handleKickCommand(interaction);
                break;
            case "ban":
                handleBanCommand(interaction);
                break;
            case "embed":
                handleEmbedCommand(interaction);
                break;
        }
    }

    private void handleMuteCommand(SlashCommandInteraction interaction) {
        User author = interaction.getUser();
        
        boolean hasPermission = interaction.getServer()
            .flatMap(server -> server.getRoleById(REQUIRED_ROLE_ID))
            .map(role -> author.getRoles(interaction.getServer().get()).contains(role))
            .orElse(false);

        if (!hasPermission) {
            interaction.createImmediateResponder()
                .setContent("❌ You don't have permission to use this command!")
                .respond();
            logger.info(author.getDiscriminatedName() + " tried to use mute command without permission");
            return;
        }

        User targetUser = interaction.getArgumentUserValueByName("user").orElse(null);
        String timeString = interaction.getArgumentStringValueByName("time").orElse("");

        if (targetUser == null) {
            interaction.createImmediateResponder()
                .setContent("❌ Please mention a user to mute!")
                .respond();
            return;
        }

        Long delaySeconds = parseTimeToSeconds(timeString.toLowerCase());
        if (delaySeconds == null) {
            interaction.createImmediateResponder()
                .setContent("❌ Invalid time format! Use: 10s, 5m, 2h, or 1d")
                .respond();
            return;
        }

        interaction.getServer().ifPresent(server -> {
            server.muteUser(targetUser).thenAccept(v -> {
                interaction.createImmediateResponder()
                    .setContent("✅ Voice muted " + targetUser.getMentionTag() + " for " + timeString + " (they must be in a voice channel)")
                    .respond();
                logger.info(author.getDiscriminatedName() + " muted " + targetUser.getDiscriminatedName() + " for " + timeString);
                
                scheduler.schedule(() -> {
                    server.unmuteUser(targetUser).thenAccept(unmuted -> {
                        interaction.getChannel().ifPresent(channel -> 
                            channel.sendMessage("🔊 " + targetUser.getMentionTag() + " has been automatically unmuted")
                        );
                        logger.info("Auto-unmuted " + targetUser.getDiscriminatedName() + " after " + timeString);
                    }).exceptionally(ex -> {
                        logger.error("Failed to auto-unmute " + targetUser.getDiscriminatedName() + ": " + ex.getMessage());
                        return null;
                    });
                }, delaySeconds, TimeUnit.SECONDS);
                
            }).exceptionally(e -> {
                interaction.createImmediateResponder()
                    .setContent("❌ Failed to mute user. Make sure the bot has proper permissions and the user is in a voice channel.")
                    .respond();
                logger.error("Failed to mute user: " + e.getMessage());
                return null;
            });
        });
    }

    private void handleKickCommand(SlashCommandInteraction interaction) {
        User author = interaction.getUser();
        
        boolean hasPermission = interaction.getServer()
            .flatMap(server -> server.getRoleById(REQUIRED_ROLE_ID))
            .map(role -> author.getRoles(interaction.getServer().get()).contains(role))
            .orElse(false);

        if (!hasPermission) {
            interaction.createImmediateResponder()
                .setContent("❌ You don't have permission to use this command!")
                .respond();
            logger.info(author.getDiscriminatedName() + " tried to use kick command without permission");
            return;
        }

        User targetUser = interaction.getArgumentUserValueByName("user").orElse(null);
        String reason = interaction.getArgumentStringValueByName("reason").orElse("No reason provided");

        if (targetUser == null) {
            interaction.createImmediateResponder()
                .setContent("❌ Please mention a user to kick!")
                .respond();
            return;
        }

        interaction.getServer().ifPresent(server -> {
            server.kickUser(targetUser, reason).thenAccept(v -> {
                interaction.createImmediateResponder()
                    .setContent("✅ Kicked " + targetUser.getDiscriminatedName() + " | Reason: " + reason)
                    .respond();
                logger.info(author.getDiscriminatedName() + " kicked " + targetUser.getDiscriminatedName() + " for: " + reason);
            }).exceptionally(e -> {
                interaction.createImmediateResponder()
                    .setContent("❌ Failed to kick user: " + e.getMessage())
                    .respond();
                return null;
            });
        });
    }

    private void handleBanCommand(SlashCommandInteraction interaction) {
        User author = interaction.getUser();
        
        boolean hasPermission = interaction.getServer()
            .flatMap(server -> server.getRoleById(REQUIRED_ROLE_ID))
            .map(role -> author.getRoles(interaction.getServer().get()).contains(role))
            .orElse(false);

        if (!hasPermission) {
            interaction.createImmediateResponder()
                .setContent("❌ You don't have permission to use this command!")
                .respond();
            logger.info(author.getDiscriminatedName() + " tried to use ban command without permission");
            return;
        }

        User targetUser = interaction.getArgumentUserValueByName("user").orElse(null);
        String reason = interaction.getArgumentStringValueByName("reason").orElse("No reason provided");

        if (targetUser == null) {
            interaction.createImmediateResponder()
                .setContent("❌ Please mention a user to ban!")
                .respond();
            return;
        }

        interaction.getServer().ifPresent(server -> {
            server.banUser(targetUser, java.time.Duration.ofDays(0), reason).thenAccept(v -> {
                interaction.createImmediateResponder()
                    .setContent("✅ Banned " + targetUser.getDiscriminatedName() + " | Reason: " + reason)
                    .respond();
                logger.info(author.getDiscriminatedName() + " banned " + targetUser.getDiscriminatedName() + " for: " + reason);
            }).exceptionally(e -> {
                interaction.createImmediateResponder()
                    .setContent("❌ Failed to ban user: " + e.getMessage())
                    .respond();
                return null;
            });
        });
    }

    private void handleEmbedCommand(SlashCommandInteraction interaction) {
        String title = interaction.getArgumentStringValueByName("title").orElse("Embed");
        String description = interaction.getArgumentStringValueByName("description").orElse("");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#005A9C"));

        interaction.createImmediateResponder()
                .addEmbed(embed)
                .respond();
        
        logger.info(interaction.getUser().getDiscriminatedName() + " used embed command");
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
