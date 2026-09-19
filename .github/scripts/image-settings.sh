#!/usr/bin/env bash
# Print the build settings of a container image as GitHub step outputs.
# Usage: image-settings.sh <service>
set -euo pipefail

service="$1"
backend="backend/gitdock-backend"
owner="$(echo "${GITHUB_REPOSITORY_OWNER:-local}" | tr '[:upper:]' '[:lower:]')"
publish=true

case "$service" in
  gitdock-auth|gitdock-discovery|gitdock-gateway|gitdock-notification|gitdock-project|gitdock-sync)
    kind=java;   context="$backend/$service" ;;
  gitdock-ai|gitdock-sentinel|gitdock-yam)
    kind=python; context="$backend/$service" ;;
  gitdock-gamification)
    kind=dotnet; context="$backend/gitdock-gamification/backend/backend" ;;
  gitdock-task-php)
    kind=php;    context="$backend/gitdock-task/infra/php";   publish=false ;;
  gitdock-task-nginx)
    kind=nginx;  context="$backend/gitdock-task/infra/nginx"; publish=false ;;
  *)
    echo "Unknown service: $service" >&2; exit 1 ;;
esac

echo "kind=$kind"
echo "context=$context"
echo "publish=$publish"
echo "image=ghcr.io/$owner/$service"
