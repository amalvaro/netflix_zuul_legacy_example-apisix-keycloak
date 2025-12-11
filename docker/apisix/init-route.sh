#!/usr/bin/env sh
set -euo pipefail

APISIX_ADMIN=http://apisix:9180
API_KEY=edd1c9f034335f136f87ad84b625c8f1

until curl -sSf "${APISIX_ADMIN}/apisix/admin/routes" -H "X-API-KEY: ${API_KEY}" > /dev/null; do
  echo "Waiting for APISIX admin API..."
  sleep 3
done

echo "Configuring default Zuul route..."
curl -sS -X PUT "${APISIX_ADMIN}/apisix/admin/routes/zuul-gateway" \
  -H "X-API-KEY: ${API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "zuul-gateway",
    "uri": "/*",
    "plugins": {
      "session": {
        "secret": "local-session-secret",
        "cookie": {"secure": false}
      },
      "openid-connect": {
        "client_id": "apisix",
        "client_secret": "secret",
        "discovery": "http://keycloak:8080/realms/apisix/.well-known/openid-configuration",
        "scope": "openid email profile",
        "bearer_only": false,
        "logout_path": "/logout",
        "redirect_uri": "http://localhost:9080/callback",
        "set_access_token_header": true,
        "set_id_token_header": true,
        "set_userinfo_header": true,
        "realm": "apisix"
      }
    },
    "upstream": {
      "type": "roundrobin",
      "nodes": {"host.docker.internal:8080": 1}
    }
  }'

echo "Route configured."
