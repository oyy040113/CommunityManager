#!/bin/sh
# Fix ownership of mounted volumes (runs as root before dropping privileges)
chown -R appuser:appgroup /data /app/uploads 2>/dev/null || true
exec su-exec appuser java -jar /app/app.jar "$@"
