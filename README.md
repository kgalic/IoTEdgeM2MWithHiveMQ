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

## Publishing the messages(simulating the machine)

As a machine simulator, the following project can be used to publish generic messages to the MQTT broker topic:
https://github.com/kgalic/DeviceSimulator


## Subscribing the messages(simulating the machine)

Subscribing to the dedicated topic of the HiveMQ broker deployed on IoT Edge will result with triggering the method for handling certain topics. An example how to leverage HiveMQ MQTT client library can be found in the folder `HiveMQMachineSubscriber`
