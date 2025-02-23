# PandaBroadcast

A simple yet powerful broadcast plugin for Velocity proxy servers. This plugin allows server administrators to send formatted broadcast messages across all connected servers or to specific target servers.

## Features

- Cross-server broadcasting with server targeting support
- Support for both MiniMessage format and legacy color codes
- Fully configurable broadcast format and styling
- Permission-based command access
- Optional server scope visibility (can be limited to staff)
- Optional broadcast logging to file
- Hot-reload configuration support

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/broadcast <message>` | Sends a broadcast message to all servers | `pandabroadcast.broadcast` |
| `/broadcast <server1,server2,...> <message>` | Sends a broadcast message to specific servers | `pandabroadcast.broadcast` |
| `/pandabroadcast` | Shows help menu | None |
| `/pandabroadcast reload` | Reloads the plugin configuration | `pandabroadcast.reload` |

## Permissions

| Permission | Description |
|------------|-------------|
| `pandabroadcast.broadcast` | Allows sending broadcasts and viewing server scope |
| `pandabroadcast.reload` | Allows reloading the plugin configuration |

## Examples

### Broadcasting to All Servers
```
/broadcast Welcome to the server!
```
Output (to all players):
```
» Broadcast « Welcome to the server!
```

### Broadcasting to Specific Servers
```
/broadcast lobby,survival Welcome to the server!
```
Output (to players with broadcast permission):
```
» Broadcast « Welcome to the server! (Sent to: lobby, survival)
```
Output (to regular players):
```
» Broadcast « Welcome to the server!
```

### Using MiniMessage Format (Default)
```
/broadcast <red>Important: <yellow>Server restart in 5 minutes!
```
Output:
```
» Broadcast « Important: Server restart in 5 minutes!
```
(With red and yellow colors applied)

### Using Legacy Color Codes
When `use-legacy-colors` is set to `true`:
```
/broadcast &cImportant: &eServer restart in 5 minutes!
```
Output:
```
» Broadcast « Important: Server restart in 5 minutes!
```
(With red and yellow colors applied)

### Staff Announcements
```
/broadcast staff,admin Staff meeting in 10 minutes!
```
Output (to staff with broadcast permission):
```
» Broadcast « Staff meeting in 10 minutes! (Sent to: staff, admin)
```
Output (to staff without broadcast permission):
```
» Broadcast « Staff meeting in 10 minutes!
```

## Configuration

The plugin is highly configurable through the `config.yml` file:

```yaml
# PandaBroadcast Configuration

# Message format settings
format:
  prefix: '» '
  title: 'Broadcast'
  suffix: ' « '
  message-color: '<gray>' # or &7 for legacy colors
  prefix-color: '<gold>' # or &6 for legacy colors
  title-color: '<yellow>' # or &e for legacy colors
  use-legacy-colors: false # Set to true to use & color codes instead of MiniMessage format

# Server targeting settings
targeting:
  default_behavior: 'all' # 'all' to broadcast to all servers by default, 'specific' to require server specification
  show_server_scope: true # Whether to show which servers the broadcast was sent to
  scope_format: '<gray>(Sent to: %servers%)' # Format for showing which servers received the broadcast
  scope_permission_only: false # If true, only shows server scope to users with pandabroadcast.broadcast permission

# Logging settings
logging:
  enabled: false
  file: 'broadcasts.log'
  format: '[%date%] %player%: %message%'
```

### Message Formatting

You can customize the broadcast format in two ways:

1. **MiniMessage Format** (Default)
   - Uses modern MiniMessage formatting
   - More flexible and powerful
   - Example: `<red>Important: <yellow>Server restart!`
   - Supports gradients, hover events, and more
   - Documentation: [MiniMessage Format](https://docs.adventure.kyori.net/minimessage/format.html)

2. **Legacy Color Codes**
   - Traditional Minecraft color codes
   - Set `use-legacy-colors: true` in config
   - Example: `&cImportant: &eServer restart!`
   - Supports standard color codes (&a-f, &0-9, &k-o, &r)

### Server Targeting

The plugin supports two modes of operation:

1. **All Servers (Default)**
   - Using `/broadcast <message>` sends to all servers
   - Configured with `targeting.default_behavior: 'all'`
   - No need to specify server names

2. **Specific Servers**
   - Using `/broadcast server1,server2,server3 <message>`
   - Target multiple servers by separating names with commas
   - Shows server scope to staff (configurable)
   - Set `targeting.scope_permission_only: true` to only show scope to users with broadcast permission

### Server Scope Visibility

You can control who sees the server scope information:

1. **Show to Everyone** (Default)
   ```yaml
   targeting:
     scope_permission_only: false
   ```
   All players will see which servers received the broadcast

2. **Show to Staff Only**
   ```yaml
   targeting:
     scope_permission_only: true
   ```
   Only players with `pandabroadcast.broadcast` permission will see which servers received the broadcast

## Installation

1. Download the PandaBroadcast plugin JAR file
2. Place the JAR file in your Velocity server's `plugins` folder
3. Start your server once to generate the configuration file
4. Configure the plugin in `plugins/pandabroadcast/config.yml`
5. Use `/pandabroadcast reload` to apply changes

## Dependencies

- Velocity 3.0.0 or higher
- Java 17 or higher

## Building from Source

This plugin uses Maven for dependency management. To build the plugin:

1. Clone the repository
2. Run `mvn clean package`
3. The compiled JAR will be in the `target` directory

## License

This project is open source and available under the MIT License.

## Support

If you encounter any issues or have questions, please open an issue on the GitHub repository. 