#!/bin/bash

# Session Start Hook - Maven Proxy Configuration
# Extracts proxy from environment and configures Maven settings.xml

echo "🔧 Configuring Maven proxy settings..."

# Extract proxy host and port from HTTPS_PROXY environment variable
if [ -n "$HTTPS_PROXY" ]; then
    # Format: http://hostname:jwt_token@ip:port
    # Extract the final IP:port after the @ symbol
    PROXY_HOST=$(echo $HTTPS_PROXY | sed -e 's|.*@||' -e 's|:.*||')
    PROXY_PORT=$(echo $HTTPS_PROXY | sed -e 's|.*:||')

    # Extract username (hostname) and password (jwt_token) from proxy URL
    # Format before @: hostname:jwt_token
    PROXY_AUTH=$(echo $HTTPS_PROXY | sed -e 's|^[^/]*//||' -e 's|@.*||')
    PROXY_USERNAME=$(echo $PROXY_AUTH | sed -e 's|:.*||')
    PROXY_PASSWORD=$(echo $PROXY_AUTH | sed -e 's|^[^:]*:||')

    echo "  Detected proxy: $PROXY_HOST:$PROXY_PORT (with auth)"

    # Create Maven settings directory if not exists
    mkdir -p ~/.m2

    # Generate Maven settings.xml with proxy configuration
    cat > ~/.m2/settings.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>$PROXY_HOST</host>
      <port>$PROXY_PORT</port>
      <username>$PROXY_USERNAME</username>
      <password>$PROXY_PASSWORD</password>
      <nonProxyHosts>localhost|127.0.0.1</nonProxyHosts>
    </proxy>
    <proxy>
      <id>https-proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>$PROXY_HOST</host>
      <port>$PROXY_PORT</port>
      <username>$PROXY_USERNAME</username>
      <password>$PROXY_PASSWORD</password>
      <nonProxyHosts>localhost|127.0.0.1</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
EOF

    echo "✓ Maven proxy configured at ~/.m2/settings.xml"
else
    echo "  No HTTPS_PROXY detected, skipping proxy configuration"
fi

echo "✓ Session start hook completed"
