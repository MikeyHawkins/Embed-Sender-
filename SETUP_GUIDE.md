# Setup Guide

Complete step-by-step guide to set up your Discord Message Sender.

## Part 1: Create Discord Application

1. **Go to Discord Developer Portal**
   - Visit: https://discord.com/developers/applications
   - Log in with your Discord account

2. **Create New Application**
   - Click "New Application" (top right)
   - Enter a name (e.g., "Message Sender")
   - Click "Create"

3. **Save Your Client ID**
   - You'll see your **Client ID** on the General Information page
   - Copy this - you'll need it later

4. **Get Your Client Secret**
   - Still on General Information page
   - Click "Reset Secret" under Client Secret
   - **Copy the secret immediately** - you can't see it again!
   - Keep this secret safe

## Part 2: Create Bot User

1. **Navigate to Bot Section**
   - Click "Bot" in the left sidebar
   - Click "Add Bot" → "Yes, do it!"

2. **Configure Bot**
   - Give your bot a username
   - Optional: Upload a profile picture

3. **Get Bot Token**
   - Under "TOKEN", click "Reset Token"
   - **Copy the token immediately** - you can't see it again!
   - Keep this token secret and secure

4. **Enable Bot Permissions**
   - Scroll to "Bot Permissions"
   - Enable: **Manage Webhooks**
   - Note the permissions integer: `536870912`

## Part 3: Configure OAuth2

1. **Navigate to OAuth2**
   - Click "OAuth2" → "General" in the left sidebar

2. **Add Redirect URLs**
   - Click "Add Redirect"
   - For Replit deployment, add:
     ```
     https://your-repl-name.your-username.repl.co/callback
     ```
   - For local testing, also add:
     ```
     http://localhost:5000/callback
     ```
   - Click "Save Changes"

## Part 4: Set Up Replit Secrets

1. **Open Secrets Tab**
   - In your Repl, click the lock icon (🔒) in the left sidebar
   - Or click "Secrets" in Tools menu

2. **Add Three Secrets**
   
   **Secret 1: DISCORD_CLIENT_ID**
   - Key: `DISCORD_CLIENT_ID`
   - Value: Paste your Client ID from Part 1, Step 3

   **Secret 2: DISCORD_CLIENT_SECRET**
   - Key: `DISCORD_CLIENT_SECRET`
   - Value: Paste your Client Secret from Part 1, Step 4

   **Secret 3: DISCORD_TOKEN**
   - Key: `DISCORD_TOKEN`
   - Value: Paste your Bot Token from Part 2, Step 3

3. **Verify Secrets**
   - Make sure all three secrets are added
   - Keys must match exactly (case-sensitive)

## Part 5: Invite Bot to Your Server

1. **Generate Invite URL**
   - Use this template, replacing YOUR_CLIENT_ID with your actual Client ID:
     ```
     https://discord.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&permissions=536870912&scope=bot
     ```
   
   Example:
   ```
   https://discord.com/oauth2/authorize?client_id=1234567890&permissions=536870912&scope=bot
   ```

2. **Invite the Bot**
   - Paste the URL in your browser
   - Select your Discord server
   - Click "Authorize"
   - Complete the CAPTCHA

3. **Verify Bot is in Server**
   - Check your Discord server's member list
   - Your bot should appear online (or offline, that's okay)

## Part 6: Run the Application

1. **Start the Server**
   - Click the "Run" button in Replit
   - Wait for "Server running on port 5000"
   - The webview should open automatically

2. **Login with Discord**
   - Click "Login with Discord"
   - Authorize the application
   - You'll be redirected back to the app

3. **Set Up Your Server**
   - Enter your Discord Server ID
     - To get Server ID: Right-click your server icon → "Copy Server ID"
     - (Enable Developer Mode in Discord settings if you don't see this)
   - Click "Setup Webhooks"
   - Wait for webhooks to be created

4. **Start Sending Messages!**
   - Select a channel
   - Fill in your embed details
   - Click "Send Message"
   - Check Discord to see your message!

## Part 7: Optional - AI Improve Feature

The AI Improve feature uses Replit AI Integrations, which is automatically available:

- ✅ No setup needed
- ✅ No API keys required
- ✅ Charges billed to your Replit credits
- ✨ Just click "AI Improve" and it works!

## Troubleshooting

### "Failed to fetch server info"
**Problem**: Can't connect to Discord server

**Solutions**:
- Make sure bot is invited to the server
- Verify DISCORD_TOKEN is correct
- Check Server ID is correct (right-click server → Copy Server ID)
- Ensure bot has "Manage Webhooks" permission

### "Login redirect not working"
**Problem**: OAuth2 redirect fails

**Solutions**:
- Check DISCORD_CLIENT_ID and DISCORD_CLIENT_SECRET are set
- Verify redirect URL in Discord Developer Portal matches your Repl URL
- Make sure you added `/callback` at the end of redirect URL
- Try clearing cookies and logging in again

### "Session expired" or keeps logging out
**Problem**: Session isn't persisting

**Solutions**:
- Replit automatically generates a session secret
- Clear your browser cookies
- Try a different browser
- Check if browser blocks third-party cookies

### Bot appears offline
**Problem**: Bot shows as offline in Discord

**Solution**: This is normal! The bot doesn't need to be online to use webhooks. Webhooks work independently.

### Webhooks not working after bot removed
**Problem**: Messages stopped sending

**Solution**: The bot MUST stay in the server for webhooks to work. Re-invite the bot using the invite URL from Part 5.

## Getting Your Server ID

### Enable Developer Mode
1. Open Discord Settings (gear icon ⚙️)
2. Go to "Advanced" (under App Settings)
3. Enable "Developer Mode"
4. Click "Save"

### Copy Server ID
1. Right-click your server icon (in the left sidebar)
2. Click "Copy Server ID"
3. Paste this ID in the setup form

## Security Best Practices

✅ **DO**:
- Keep your bot token secret
- Keep your client secret safe
- Use Replit Secrets for sensitive data
- Enable 2FA on your Discord account

❌ **DON'T**:
- Share your bot token publicly
- Commit secrets to GitHub
- Give your bot unnecessary permissions
- Share your OAuth2 client secret

## Next Steps

Once everything is working:

1. **Customize Your Messages**
   - Experiment with different colors
   - Try the AI Improve feature
   - Upload custom images

2. **Add More Servers**
   - Click "Manage Servers"
   - Add multiple Discord servers
   - Switch between them easily

3. **Share Your App**
   - Deploy to Replit (click Deploy button)
   - Share the URL with others
   - Update OAuth2 redirect URL for production

4. **Extend the Template**
   - Add more embed fields
   - Customize color presets
   - Modify the AI prompt
   - Build new features!

## Need Help?

- Check the main README.md for feature documentation
- Review Discord API docs: https://discord.com/developers/docs
- Check Replit documentation: https://docs.replit.com

---

Happy messaging! 🚀
