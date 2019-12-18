# IoTEdgeM2MWithHiveMQ

This project demonstrates how to incorporate MQTT broker within IoT Edge, for machine to machine communication and machine to cloud communication.

## Setting up the solution

1. In deployment.template.json - deployment.debug.template.json add container registry credentials
`
            "registryCredentials": {
              "kresimiriotedgecontainerreg": {
                "username": "$CONTAINER_REGISTRY_USERNAME_kresimiriotedgecontainerreg",
                "password": "$CONTAINER_REGISTRY_PASSWORD_kresimiriotedgecontainerreg",
                "address": "kresimiriotedgecontainerreg.azurecr.io"
              }
            }
`

2. Add IoT Edge Host IP Address to the Create Options of SubscriberModule
`
            "createOptions": {
                "HostConfig": {
                  "ExtraHosts": [
                    "hivemq.test.iotlab:192.168.200.11"
                    ]
                }
              }
`
