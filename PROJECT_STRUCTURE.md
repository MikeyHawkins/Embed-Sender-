# Project Structure

This document outlines the essential files for the Discord Message Sender Template.

## Essential Files

### Core Application Files

```
discord-message-sender-template/
├── server.js                 # Main Express server and API endpoints
├── public/
│   └── index.html           # Complete frontend (HTML, CSS, JS)
├── package.json             # Node.js dependencies
├── package-lock.json        # Locked dependency versions
└── .env.example             # Example environment variables
```

### Documentation Files

```
├── README.md                # Main documentation and overview
├── SETUP_GUIDE.md          # Step-by-step setup instructions
├── PROJECT_STRUCTURE.md    # This file - project organization
└── LICENSE                 # MIT License
```

### Configuration Files

```
├── .gitignore              # Git ignore rules
├── .env.example            # Environment variable template
└── .replit                 # Replit configuration (auto-generated)
```

## File Descriptions

### server.js (Backend)
The main application server that handles:
- Express.js setup and middleware
- Discord OAuth2 authentication flow
- Session management with secure cookies
- Webhook creation and management via Discord API
- Message sending endpoint with embed support
- AI text improvement using OpenAI API
- Server info fetching and caching

**Key Features**:
- Auto-generates session secrets
- Reuses existing webhooks to prevent duplicates
- Checks for webhooks by server name
- Uses Replit AI Integrations for OpenAI access

### public/index.html (Frontend)
Complete single-page application with:
- Discord OAuth2 login interface
- Server setup and management UI
- Full embed editor with all Discord fields
- Image upload system with base64 conversion
- Live message preview matching Discord's design
- Multi-server management with localStorage
- AI Improve button integration

**Key Features**:
- No build process needed - pure HTML/CSS/JS
- Responsive design
- Real-time preview updates
- Clickable image upload boxes
- Color picker with presets + custom hex

### package.json
Node.js project configuration with minimal dependencies:
- `express` - Web server framework
- `express-session` - Session management
- `node-fetch` - HTTP requests to Discord API
- `openai` - AI text improvement

### Documentation

**README.md**
- Features overview
- Prerequisites and setup
- Usage instructions
- Technical details
- Troubleshooting guide

**SETUP_GUIDE.md**
- Step-by-step Discord app creation
- Bot setup and permissions
- OAuth2 configuration
- Replit secrets setup
- Complete troubleshooting

**PROJECT_STRUCTURE.md** (this file)
- Project organization
- File descriptions
- Customization guide

## Ignored Files

The following files/directories should be ignored (see .gitignore):
- `node_modules/` - Installed npm packages
- `.env` - Environment secrets (never commit!)
- `logs/` - Application logs
- `build/`, `src/` - Java/Gradle files (not part of template)
- `attached_assets/` - Temporary asset storage

## Required Environment Variables

Set these in Replit Secrets (not in .env file):

```bash
DISCORD_CLIENT_ID       # Discord Application Client ID
DISCORD_CLIENT_SECRET   # Discord Application Client Secret  
DISCORD_TOKEN          # Discord Bot Token
```

Auto-provided by Replit:
```bash
AI_INTEGRATIONS_OPENAI_API_KEY    # Replit AI Integrations
AI_INTEGRATIONS_OPENAI_BASE_URL   # Replit AI Integrations
```

## How to Customize

### Add New Color Presets
Edit `public/index.html`, find the color options section:
```html
<div class="color-option" data-color="red"></div>
```

### Change AI Model or Prompt
Edit `server.js`, find the `/ai-improve` endpoint:
```javascript
model: 'gpt-4o-mini',  // Change model
// Edit the prompt text
```

### Add More Embed Fields
Discord supports up to 25 fields. To add more:
1. Add form inputs in `public/index.html`
2. Update the embed preview function
3. Include in the `/send-message` payload

### Modify Preview Styling
Edit the CSS in `public/index.html`:
```css
.discord-embed {
    /* Customize preview appearance */
}
```

### Add Rate Limiting
Add rate limiting middleware in `server.js`:
```javascript
const rateLimit = require('express-rate-limit');
```

## Running Locally

```bash
# Install dependencies
npm install

# Set environment variables in .env file
cp .env.example .env
# Edit .env with your Discord credentials

# Run the server
npm start

# Visit http://localhost:5000
```

## Deploying to Replit

1. Fork this Repl
2. Set Discord secrets in Secrets tab
3. Click "Run"
4. Update Discord OAuth2 redirect URL with your Repl URL
5. Click "Deploy" to publish

## Tech Stack

**Backend**:
- Node.js 20+
- Express.js 5.1
- Discord API v10
- OpenAI GPT-4o-mini

**Frontend**:
- Vanilla JavaScript (ES6+)
- CSS3 with Flexbox/Grid
- No frameworks or build tools

**Storage**:
- Browser localStorage (no database needed)
- Session cookies (server-side)

**AI**:
- Replit AI Integrations
- OpenAI via proxy

## Browser Support

Works in all modern browsers:
- ✅ Chrome/Edge 90+
- ✅ Firefox 88+
- ✅ Safari 14+

Requires:
- JavaScript enabled
- Cookies enabled
- localStorage support

## Performance Notes

**Small footprint**:
- Single HTML file frontend
- Minimal dependencies
- No database required
- Fast startup time

**Scalability**:
- Stateless design (sessions in cookies)
- No persistent storage needed
- Can handle multiple concurrent users
- Webhooks scale with Discord API limits

## Security Considerations

**Implemented**:
- ✅ httpOnly cookies (prevent XSS)
- ✅ sameSite cookies (prevent CSRF)
- ✅ Auto-generated session secrets
- ✅ Secrets in environment variables
- ✅ OAuth2 authentication
- ✅ Session expiration

**User Responsibility**:
- Keep Discord tokens secret
- Don't share bot tokens
- Use HTTPS in production (Replit provides this)
- Enable 2FA on Discord account

## Future Enhancement Ideas

Want to extend this template? Ideas:
- [ ] Scheduled message sending
- [ ] Multiple embed fields support
- [ ] Message templates/presets
- [ ] Webhook analytics
- [ ] Role mention support
- [ ] File attachment support
- [ ] Message editing/deletion
- [ ] Database for message history
- [ ] User permissions system
- [ ] Webhook rotation/backup

## Support Files

- `LICENSE` - MIT license (free to use/modify)
- `.replit` - Replit IDE configuration
- `replit.nix` - Nix environment setup

---

**Note**: This template is designed specifically for Replit and uses Replit AI Integrations. Some features may need modification for deployment on other platforms.
