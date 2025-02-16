# Alert Plugin

A simple yet powerful broadcast plugin for Velocity proxy servers. This plugin allows server administrators to send formatted broadcast messages across all connected servers.

## Features

- Cross-server broadcasting
- MiniMessage format support for colored and styled messages
- Permission-based command access
- Clean and visible broadcast formatting

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/vbroadcast <message>` | Sends a formatted broadcast message to all players | `alert.broadcast` |

## Message Format

Broadcasts are formatted as follows:
```
» Broadcast « Your message here
```

The broadcast prefix is styled in gold and yellow colors, making it stand out in the chat.

## Permissions

- `alert.broadcast` - Allows users to use the `/vbroadcast` command

## Installation

1. Download the Alert plugin JAR file
2. Place the JAR file in your Velocity server's `plugins` folder
3. Restart your Velocity server
4. The plugin will automatically create any necessary configuration files

## Dependencies

- Velocity 3.0.0 or higher
- Java 8 or higher

## Building from Source

This plugin uses Maven for dependency management. To build the plugin:

1. Clone the repository
2. Run `mvn clean package`
3. The compiled JAR will be in the `target` directory

## License

This project is open source and available under the MIT License.

## Support

If you encounter any issues or have questions, please open an issue on the GitHub repository. 