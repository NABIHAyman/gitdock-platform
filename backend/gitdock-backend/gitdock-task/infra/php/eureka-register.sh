#!/bin/sh

# On attend que le serveur Eureka soit prêt
echo "⏳ Waiting for Eureka..."
sleep 15

# Enregistrement auprès d'Eureka
# Note: nginx_webserver est le nom du service dans ton docker-compose
curl -X POST -H "Content-Type: application/json" \
-d '{
  "instance": {
    "hostName": "nginx_webserver",
    "app": "GITDOCK-TASK",
    "ipAddr": "nginx_webserver",
    "status": "UP",
    "port": {"$": 80, "@enabled": "true"},
    "vipAddress": "GITDOCK-TASK",
    "dataCenterInfo": {
      "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
      "name": "MyOwn"
    }
  }
}' http://gitdock-discovery:8761/eureka/apps/GITDOCK-TASK

echo "✅ Registered in Eureka as GITDOCK-TASK"
