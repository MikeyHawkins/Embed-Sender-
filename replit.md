# Los Angeles Roleplay Bot

A feature-rich Discord bot built with Java 17 and Javacord for the Los Angeles Roleplay server.

## Overview

This Discord bot provides utility commands, fun features, and moderation tools for server management. Originally based on "Ducky" bot, it has been customized for the Los Angeles Roleplay community.

## Recent Changes (October 13, 2025)

### Bot Prefix Changed
- Changed from `!` to `/` for all commands

### New Moderation Commands
Added three role-restricted moderation commands (requires role ID: 1350907647217635389):
- **Mute Command** (`/mute @user <time>`): Voice mutes a user for a specified duration with automatic unmute
  - Supports time formats: s (seconds), m (minutes), h (hours), d (days)
  - Example: `/mute @user 10m`
- **Kick Command** (`/kick @user <reason>`): Kicks a user from the server
- **Ban Command** (`/ban @user <reason>`): Bans a user from the server

### Updated Features
- Help command now pings role ID 1350907647217635389
- Help command updated to show new moderation commands
- Embed command added with hex color #005A9C

## Bot Configuration

### Environment Variables
- `DISCORD_TOKEN`: Bot token from Discord Developer Portal
- `DISCORD_CLIENT_ID`: Application client ID

### Bot Features
- **Rotating Status**: Cycles between "Playing ERLC", "Watching bob", and "Watching members" every 10 seconds
- **Command Prefix**: `/`
- **Embed Color**: #005A9C

## Available Commands

### General Commands
- `/help` or `/halp` - Shows available commands (pings moderator role)
- `/embed <title> | <description>` - Creates a custom embed with #005A9C color
- `/g <query>` - Web search using Google
- `/cat` - Random cat image
- `/duck` or `🦆` - Random duck image
- `/dog` or `🐶` - Random dog image
- `/paste` - Self-hosted paste server
- `/site` or `/website` - Bot's website
- `/inv` or `/invite` - Invite the bot

### Moderation Commands (Role Restricted: 1350907647217635389)
- `/mute @user <time>` - Voice mute user for specified duration (e.g., 10m, 2h, 1d)
- `/kick @user <reason>` - Kick user from server
- `/ban @user <reason>` - Ban user from server

### Debug Command
- `/debug` - Debug information

## Project Architecture

### Technology Stack
- **Language**: Java 17
- **Discord Library**: Javacord 3.4.0
- **Build Tool**: Gradle 7.3.3
- **Command Framework**: SDCF4J 1.0.10
- **Logging**: Log4j 2.17.1

### Key Files
- `src/main/java/com/github/koxsosen/Main.java` - Main bot entry point with rotating status
- `src/main/java/com/github/koxsosen/Constants.java` - Bot configuration constants
- `src/main/java/com/github/koxsosen/commands/` - Command implementations
  - `MuteCommand.java` - Voice mute with scheduled unmute
  - `KickCommand.java` - Kick functionality
  - `BanCommand.java` - Ban functionality
  - `EmbedCommand.java` - Custom embed creation
  - `HelpCommand.java` - Help command with role ping
  - Other utility commands (WebSearch, Cat, Dog, Duck, etc.)

### Build Configuration
- JVM Args: `-Xms1G -Xmx1G -XX:+UseG1GC`
- Async logging via Log4j2

## User Preferences

### Moderation Setup
- Role-based permissions using role ID: 1350907647217635389
- Only users with this role can use moderation commands
- Mute command includes automatic unmute scheduling
- Clear error messages for permission issues

### Design Choices
- Prefix changed to `/` for better Discord slash command compatibility feel
- Rotating status to show bot activity and server engagement
- Embed color #005A9C matches server branding
- Voice mute implementation due to Javacord 3.4.0 limitations (text timeout requires 3.8.0+)

## Known Limitations

- Mute command only works for voice channels (Javacord 3.4.0 doesn't support Discord's text timeout API)
- User must be in a voice channel for mute command to work
- Scheduled unmutes persist only while bot is running (restarts clear scheduled tasks)
