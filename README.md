# IoTEdgeM2MWithHiveMQ

This project demonstrates how to incorporate MQTT broker within IoT Edge, for machine to machine communication and machine to cloud communication.

## Setting up the solution
1. Create file .env that will contain container registry credentials

```
CONTAINER_REGISTRY_USERNAME_example=Cont_Reg_Username
CONTAINER_REGISTRY_PASSWORD_example=Cont_Reg_Password
```

2. In deployment.template.json - deployment.debug.template.json add container registry credentials and reference previously created environment variables:

```
            "registryCredentials": {
              "kresimiriotedgecontainerreg": {
                "username": "$CONTAINER_REGISTRY_USERNAME_example",
                "password": "$CONTAINER_REGISTRY_PASSWORD_example",
                "address": "example.azurecr.io"
              }
            }
```

3. Add IoT Edge Host IP Address to the Create Options of SubscriberModule
```
            "createOptions": {
                "HostConfig": {
                  "ExtraHosts": [
                    "hivemq.test.iotlab:192.168.200.11"
                    ]
                }
              }
```
