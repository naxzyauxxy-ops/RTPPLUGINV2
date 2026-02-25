# 🟣 PurpleRTP
> A DonutSMP-style Random Teleport plugin for Minecraft 1.21.x

---

## ✨ Features
- **Purple-themed** messages, particles (Portal + Witch), and sounds
- **DonutSMP-style** wild teleport with title card on arrival
- **Async chunk loading** — no server lag during teleport search
- **Cooldown system** — persistent across server restarts
- **Configurable** range, cooldown, allowed worlds, messages
- **Admin commands** — force RTP, adjust cooldowns, reload config
- **Safe landing** — checks for solid ground, avoids water/lava/fire

---

## 📦 Requirements
- **Paper** (or Spigot) 1.21.x
- **Java 21**

---

## 🔧 Build Instructions
```bash
# Clone/download the project folder
cd PurpleRTP
mvn clean package
# Output: target/PurpleRTP-1.0.0.jar
```
Drop the jar into your server's `/plugins` folder and restart.

---

## 💬 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rtp` | Random teleport to the wilderness | `purplertp.use` |
| `/wild` | Alias for /rtp | `purplertp.use` |
| `/wilderness` | Alias for /rtp | `purplertp.use` |
| `/rtpadmin reload` | Reload config | `purplertp.admin` |
| `/rtpadmin cooldown <player> <seconds>` | Set a player's cooldown | `purplertp.admin` |
| `/rtpadmin clearcooldown <player>` | Clear a player's cooldown | `purplertp.admin` |
| `/rtpadmin forcertp <player>` | Force-RTP a player | `purplertp.admin` |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `purplertp.use` | Use /rtp | Everyone |
| `purplertp.admin` | Admin commands | OP |
| `purplertp.bypass.cooldown` | Skip cooldown | OP |

---

## ⚙️ Configuration (`config.yml`)

```yaml
cooldown: 300        # Seconds between uses
max-range: 10000     # Max distance from 0,0
min-range: 500       # Min distance from 0,0
max-attempts: 50     # Attempts to find safe spot
allowed-worlds:
  - world
```

---

## 🎨 Purple Theme Details
- **Particle effects**: `PORTAL` + `WITCH` particles on teleport
- **Sounds**: Enderman teleport + Amethyst block chime
- **Colors**: `&5` (dark purple) + `&d` (light purple/pink)
- **Title**: `✦ WILDERNESS ✦` shown on arrival
