const express = require('express');
const path = require('path');
const session = require('express-session');
const crypto = require('crypto');
const OpenAI = require('openai');

const app = express();
const PORT = 5000;

// Initialize OpenAI with Replit AI Integrations
const openai = new OpenAI({
    apiKey: process.env.AI_INTEGRATIONS_OPENAI_API_KEY,
    baseURL: process.env.AI_INTEGRATIONS_OPENAI_BASE_URL
});

// Generate secure session secret if not provided
const SESSION_SECRET = process.env.SESSION_SECRET || crypto.randomBytes(32).toString('hex');

// Session configuration
app.use(session({
    secret: SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    cookie: { 
        secure: process.env.NODE_ENV === 'production',
        httpOnly: true,
        maxAge: 24 * 60 * 60 * 1000, // 24 hours
        sameSite: 'lax'
    }
}));

app.use(express.json());

// Auth middleware - protect routes
function requireAuth(req, res, next) {
    if (req.session && req.session.user) {
        return next();
    }
    res.status(401).json({ error: 'Authentication required' });
}

// Serve login page for unauthenticated users
app.use(express.static('public', {
    setHeaders: (res, path) => {
        if (path.endsWith('index.html')) {
            // Don't cache the main HTML file
            res.setHeader('Cache-Control', 'no-cache');
        }
    }
}));

// Webhooks storage - populated when users set up their servers
let WEBHOOKS = {};

// Server branding - populated when users set up their servers
let serverName = '';
let serverLogo = '';

const COLOR_MAP = {
    red: 0xED4245,
    green: 0x57F287,
    blue: 0x5865F2,
    yellow: 0xFEE75C,
    purple: 0xEB459E,
    orange: 0xF26522
};

async function sendWebhookMessage(webhookUrl, embedData, serverName, serverIcon) {
    let embedColor;
    
    if (embedData.color && embedData.color.startsWith('#')) {
        embedColor = parseInt(embedData.color.substring(1), 16);
    } else {
        embedColor = COLOR_MAP[embedData.color] || COLOR_MAP.blue;
    }
    
    const embed = {
        color: embedColor
    };
    
    // Add author if provided
    if (embedData.authorName || embedData.authorIcon) {
        embed.author = {};
        if (embedData.authorName) embed.author.name = embedData.authorName;
        if (embedData.authorIcon) embed.author.icon_url = embedData.authorIcon;
    }
    
    // Add title if provided
    if (embedData.title) {
        embed.title = embedData.title;
    }
    
    // Add description if provided
    if (embedData.description) {
        embed.description = embedData.description;
    }
    
    // Add field if provided
    if (embedData.fieldName || embedData.fieldValue) {
        embed.fields = [{
            name: embedData.fieldName || '\u200b',
            value: embedData.fieldValue || '\u200b'
        }];
    }
    
    // Add main image if provided
    if (embedData.embedImage) {
        embed.image = {
            url: embedData.embedImage
        };
    }
    
    // Add footer if provided
    if (embedData.footerText || embedData.footerIcon) {
        embed.footer = {};
        if (embedData.footerText) embed.footer.text = embedData.footerText;
        if (embedData.footerIcon) embed.footer.icon_url = embedData.footerIcon;
    }
    
    const payload = {
        username: serverName || 'Message Sender',
        avatar_url: serverIcon,
        embeds: [embed]
    };
    
    const response = await fetch(webhookUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to send webhook message');
    }
}

// Discord OAuth endpoints
const DISCORD_OAUTH_URL = 'https://discord.com/api/oauth2/authorize';
const DISCORD_TOKEN_URL = 'https://discord.com/api/oauth2/token';
const DISCORD_USER_URL = 'https://discord.com/api/users/@me';

app.get('/auth/discord', (req, res) => {
    const clientId = process.env.DISCORD_CLIENT_ID;
    
    // Use https for Replit URLs (proxy forwards as http)
    const protocol = req.get('host').includes('replit.dev') ? 'https' : req.protocol;
    const redirectUri = `${protocol}://${req.get('host')}/auth/discord/callback`;
    
    console.log('OAuth redirect URI:', redirectUri);
    
    const params = new URLSearchParams({
        client_id: clientId,
        redirect_uri: redirectUri,
        response_type: 'code',
        scope: 'identify email'
    });
    
    res.redirect(`${DISCORD_OAUTH_URL}?${params.toString()}`);
});

app.get('/auth/discord/callback', async (req, res) => {
    const { code } = req.query;
    
    if (!code) {
        return res.redirect('/?error=no_code');
    }
    
    try {
        const clientId = process.env.DISCORD_CLIENT_ID;
        const clientSecret = process.env.DISCORD_CLIENT_SECRET;
        
        // Use https for Replit URLs (proxy forwards as http)
        const protocol = req.get('host').includes('replit.dev') ? 'https' : req.protocol;
        const redirectUri = `${protocol}://${req.get('host')}/auth/discord/callback`;
        
        // Exchange code for access token
        const tokenResponse = await fetch(DISCORD_TOKEN_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                client_id: clientId,
                client_secret: clientSecret,
                grant_type: 'authorization_code',
                code: code,
                redirect_uri: redirectUri
            })
        });
        
        const tokenData = await tokenResponse.json();
        
        if (!tokenResponse.ok) {
            throw new Error('Failed to get access token');
        }
        
        // Get user info
        const userResponse = await fetch(DISCORD_USER_URL, {
            headers: {
                'Authorization': `Bearer ${tokenData.access_token}`
            }
        });
        
        const userData = await userResponse.json();
        
        // Store user in session
        req.session.user = {
            id: userData.id,
            username: userData.username,
            discriminator: userData.discriminator,
            avatar: userData.avatar ? `https://cdn.discordapp.com/avatars/${userData.id}/${userData.avatar}.png` : 'https://cdn.discordapp.com/embed/avatars/0.png',
            email: userData.email
        };
        
        await req.session.save();
        res.redirect('/');
        
    } catch (error) {
        console.error('OAuth error:', error);
        res.redirect('/?error=auth_failed');
    }
});

app.get('/auth/user', (req, res) => {
    if (req.session && req.session.user) {
        res.json({ user: req.session.user });
    } else {
        res.json({ user: null });
    }
});

app.post('/auth/logout', (req, res) => {
    req.session.destroy();
    res.json({ success: true });
});

// Protected routes
app.post('/send-message', requireAuth, async (req, res) => {
    try {
        const { 
            channel, 
            authorName,
            authorIcon,
            title,
            description,
            fieldName,
            fieldValue,
            embedImage,
            footerText,
            footerIcon,
            color, 
            serverName: customName, 
            serverLogo: customLogo 
        } = req.body;
        
        if (!channel) {
            return res.status(400).json({ error: 'Channel is required' });
        }
        
        // At least one content field must be provided
        if (!authorName && !title && !description && !fieldName && !fieldValue && !embedImage && !footerText) {
            return res.status(400).json({ error: 'At least one embed field is required' });
        }
        
        const webhookUrl = WEBHOOKS[channel];
        if (!webhookUrl) {
            return res.status(400).json({ error: 'Invalid channel selected' });
        }
        
        console.log('Sending message via webhook...');
        
        const embedData = {
            authorName,
            authorIcon,
            title,
            description,
            fieldName,
            fieldValue,
            embedImage,
            footerText,
            footerIcon,
            color
        };
        
        await sendWebhookMessage(
            webhookUrl,
            embedData,
            customName || serverName,
            customLogo || serverLogo
        );
        
        res.json({ 
            success: true, 
            message: 'Message sent successfully'
        });
        
    } catch (error) {
        console.error('Error sending message:', error);
        res.status(500).json({ error: 'Failed to send message. Please try again.' });
    }
});

app.post('/ai-improve', requireAuth, async (req, res) => {
    try {
        const { authorName, title, description, fieldName, fieldValue, footerText } = req.body;
        
        // Build the prompt for AI
        const fieldsToImprove = [];
        if (authorName) fieldsToImprove.push(`Author Name: "${authorName}"`);
        if (title) fieldsToImprove.push(`Title: "${title}"`);
        if (description) fieldsToImprove.push(`Description: "${description}"`);
        if (fieldName) fieldsToImprove.push(`Field Name: "${fieldName}"`);
        if (fieldValue) fieldsToImprove.push(`Field Value: "${fieldValue}"`);
        if (footerText) fieldsToImprove.push(`Footer Text: "${footerText}"`);
        
        if (fieldsToImprove.length === 0) {
            return res.status(400).json({ error: 'No text provided to improve' });
        }
        
        const prompt = `You are a Discord message editor. Improve the following embed fields by fixing grammar, making them more professional and neat, while keeping the original meaning and tone. Return ONLY a JSON object with the improved text for each field that was provided.

${fieldsToImprove.join('\n')}

Return the response as a JSON object with these keys (only include keys that were provided in the input): authorName, title, description, fieldName, fieldValue, footerText

Keep the improvements concise and professional. Fix any spelling or grammar errors. Make the text clearer and more polished.`;

        console.log('Requesting AI improvement...');
        
        const completion = await openai.chat.completions.create({
            model: 'gpt-4o-mini',
            messages: [
                {
                    role: 'system',
                    content: 'You are a helpful assistant that improves Discord embed text. You respond ONLY with valid JSON objects containing the improved text fields.'
                },
                {
                    role: 'user',
                    content: prompt
                }
            ],
            temperature: 0.7,
            max_tokens: 500
        });
        
        const aiResponse = completion.choices[0].message.content.trim();
        console.log('AI Response:', aiResponse);
        
        // Parse the JSON response
        let improvedText;
        try {
            // Remove markdown code blocks if present
            const jsonText = aiResponse.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim();
            improvedText = JSON.parse(jsonText);
        } catch (parseError) {
            console.error('Failed to parse AI response:', parseError);
            return res.status(500).json({ error: 'Failed to parse AI response' });
        }
        
        res.json(improvedText);
        
    } catch (error) {
        console.error('Error improving text:', error);
        res.status(500).json({ error: 'Failed to improve text. Please try again.' });
    }
});

app.get('/client-id', (req, res) => {
    res.json({ clientId: process.env.DISCORD_CLIENT_ID || '' });
});

app.get('/channels', requireAuth, (req, res) => {
    const channels = Object.keys(WEBHOOKS).map(key => ({
        value: key,
        label: key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    }));
    res.json({ channels, serverName, serverLogo });
});

app.post('/setup-webhooks', requireAuth, async (req, res) => {
    try {
        const { serverId } = req.body;
        const DISCORD_BOT_TOKEN = process.env.DISCORD_TOKEN;
        const fs = require('fs');
        
        if (!DISCORD_BOT_TOKEN) {
            return res.status(500).json({ error: 'Discord bot token not configured' });
        }
        
        if (!serverId) {
            return res.status(400).json({ error: 'Server ID is required' });
        }

        console.log('Fetching server info for:', serverId);
        
        // Fetch server/guild information
        const guildResponse = await fetch(`https://discord.com/api/v10/guilds/${serverId}`, {
            headers: {
                'Authorization': `Bot ${DISCORD_BOT_TOKEN}`
            }
        });

        if (!guildResponse.ok) {
            throw new Error('Failed to fetch server info. Make sure the bot is in the server.');
        }

        const guild = await guildResponse.json();
        const fetchedServerName = guild.name;
        const fetchedServerLogo = guild.icon 
            ? `https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}.png`
            : 'https://cdn.discordapp.com/embed/avatars/0.png';

        console.log('Fetching channels for server:', serverId);
        const channelsResponse = await fetch(`https://discord.com/api/v10/guilds/${serverId}/channels`, {
            headers: {
                'Authorization': `Bot ${DISCORD_BOT_TOKEN}`
            }
        });

        if (!channelsResponse.ok) {
            throw new Error('Failed to fetch channels. Make sure the bot is in the server.');
        }

        const channels = await channelsResponse.json();
        const textChannels = channels.filter(ch => ch.type === 0);

        const webhooks = {};
        let count = 0;

        for (const channel of textChannels) {
            try {
                // First, check for existing webhooks
                const existingWebhooksResponse = await fetch(`https://discord.com/api/v10/channels/${channel.id}/webhooks`, {
                    headers: {
                        'Authorization': `Bot ${DISCORD_BOT_TOKEN}`
                    }
                });

                let webhook = null;
                
                if (existingWebhooksResponse.ok) {
                    const existingWebhooks = await existingWebhooksResponse.json();
                    // Find a webhook created by our bot - check by webhook name matching our server name
                    webhook = existingWebhooks.find(w => w.name === fetchedServerName);
                }

                // Create new webhook if none exists
                if (!webhook) {
                    console.log(`Creating webhook for #${channel.name}`);
                    const webhookResponse = await fetch(`https://discord.com/api/v10/channels/${channel.id}/webhooks`, {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bot ${DISCORD_BOT_TOKEN}`,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            name: fetchedServerName
                        })
                    });

                    if (webhookResponse.ok) {
                        webhook = await webhookResponse.json();
                    } else {
                        const errorData = await webhookResponse.json().catch(() => ({}));
                        console.error(`Failed to create webhook for ${channel.name}:`, errorData);
                        continue;
                    }
                } else {
                    console.log(`Using existing webhook for #${channel.name}`);
                }

                if (webhook) {
                    const channelKey = channel.name.toLowerCase().replace(/[^a-z0-9]+/g, '_');
                    webhooks[channelKey] = `https://discord.com/api/webhooks/${webhook.id}/${webhook.token}`;
                    count++;
                }
            } catch (err) {
                console.error(`Error processing webhook for ${channel.name}:`, err);
            }
        }

        // Update in-memory webhooks and server info immediately
        Object.keys(WEBHOOKS).forEach(key => delete WEBHOOKS[key]);
        Object.assign(WEBHOOKS, webhooks);
        serverName = fetchedServerName;
        serverLogo = fetchedServerLogo;
        console.log('Updated server config in memory');
        
        // Update server.js file for persistence
        try {
            const serverJsPath = path.join(__dirname, 'server.js');
            let fileContent = fs.readFileSync(serverJsPath, 'utf8');
            
            // Build the new WEBHOOKS object
            const webhooksCode = `const WEBHOOKS = {\n${Object.entries(webhooks).map(([key, url]) => `    ${key}: '${url}'`).join(',\n')}\n};`;
            
            // Build the server info
            const serverInfoCode = `let serverName = '${fetchedServerName}';\nlet serverLogo = '${fetchedServerLogo}';`;
            
            // Replace WEBHOOKS section
            fileContent = fileContent.replace(
                /\/\/ Configure your webhooks here.*?\nconst WEBHOOKS = \{[^}]*\};/s,
                `// Configure your webhooks here (generate using the setup page)\n${webhooksCode}`
            );
            
            // Replace server info section
            fileContent = fileContent.replace(
                /\/\/ Configure your server branding here\nlet serverName = .*?;\nlet serverLogo = .*?;/s,
                `// Configure your server branding here\n${serverInfoCode}`
            );
            
            fs.writeFileSync(serverJsPath, fileContent, 'utf8');
            console.log('Successfully updated server.js file for persistence');
        } catch (fileError) {
            console.error('Error updating server.js:', fileError);
        }

        const code = `const WEBHOOKS = {\n${Object.entries(webhooks).map(([key, url]) => `    ${key}: '${url}'`).join(',\n')}\n};`;

        res.json({
            success: true,
            count,
            code,
            channels: Object.keys(webhooks),
            serverName: fetchedServerName,
            serverLogo: fetchedServerLogo,
            autoUpdated: true
        });

    } catch (error) {
        console.error('Error setting up webhooks:', error);
        res.status(500).json({ error: 'Failed to setup webhooks. Please check the server ID and ensure the bot is in the server.' });
    }
});

app.post('/setup-manual-webhook', requireAuth, async (req, res) => {
    try {
        const { inviteLink, webhookUrl } = req.body;
        
        if (!inviteLink || !webhookUrl) {
            return res.status(400).json({ error: 'Invite link and webhook URL are required' });
        }

        // Validate webhook URL format
        if (!webhookUrl.startsWith('https://discord.com/api/webhooks/')) {
            return res.status(400).json({ error: 'Invalid webhook URL format' });
        }

        // Extract invite code from the link
        let inviteCode = inviteLink.trim();
        inviteCode = inviteCode.replace('https://discord.gg/', '');
        inviteCode = inviteCode.replace('http://discord.gg/', '');
        inviteCode = inviteCode.replace('discord.gg/', '');
        inviteCode = inviteCode.replace('https://discord.com/invite/', '');
        inviteCode = inviteCode.replace('http://discord.com/invite/', '');
        inviteCode = inviteCode.split('?')[0]; // Remove query params if any

        console.log('Fetching server info from invite:', inviteCode);

        // Fetch server info from the invite (public API, no auth needed)
        const inviteResponse = await fetch(`https://discord.com/api/v10/invites/${inviteCode}?with_counts=false`);
        
        if (!inviteResponse.ok) {
            return res.status(400).json({ error: 'Invalid invite link or invite has expired' });
        }

        const inviteData = await inviteResponse.json();
        const fetchedServerName = inviteData.guild.name;
        const fetchedServerLogo = inviteData.guild.icon 
            ? `https://cdn.discordapp.com/icons/${inviteData.guild.id}/${inviteData.guild.icon}.png`
            : 'https://cdn.discordapp.com/embed/avatars/0.png';

        console.log('Fetching webhook info...');

        // Fetch webhook info to get channel name
        const webhookResponse = await fetch(webhookUrl);
        
        if (!webhookResponse.ok) {
            return res.status(400).json({ error: 'Invalid webhook URL. The webhook may have been deleted.' });
        }

        const webhookData = await webhookResponse.json();
        const webhookName = webhookData.name || 'webhook';
        const channelId = webhookData.channel_id;

        // Use webhook name as the key (this is what Discord shows as the webhook identity)
        // For manual setup, this is fine since there's typically only one webhook
        const channelKey = webhookName.toLowerCase().replace(/[^a-z0-9]+/g, '_');
        WEBHOOKS[channelKey] = webhookUrl;
        serverName = fetchedServerName;
        serverLogo = fetchedServerLogo;

        console.log(`Manual webhook added: ${webhookName} (Channel ID: ${channelId}) in ${fetchedServerName}`);

        res.json({
            success: true,
            serverName: fetchedServerName,
            serverLogo: fetchedServerLogo,
            channelName: webhookName
        });

    } catch (error) {
        console.error('Error setting up manual webhook:', error);
        res.status(500).json({ error: 'Failed to save webhook. Please try again.' });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`Open http://localhost:${PORT} to use the message sender`);
    console.log(`Setup tool available at: http://localhost:${PORT}/setup.html`);
});
