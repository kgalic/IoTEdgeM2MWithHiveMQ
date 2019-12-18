package com.edgemodule.telemetrysubscribermodule;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;


import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

public class App {
    
    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_FOUND = 404;

    private static MessageCallbackMqtt msgCallback = new MessageCallbackMqtt();
    private static EventCallback eventCallback = new EventCallback();
    private static final String INPUT_NAME = "input1";
    private static final String OUTPUT_NAME = "output1";
    private static final String BROKER_ENDPOINT = "hivemq.test.iotlab";
    private static final String CONNECT_METHOD_NAME = "Connect";
    private static final String DEFAULT_TOPIC_NAME = "telemetry";
    private static final Mqtt5AsyncClient mqttClient = Mqtt5Client.builder().serverHost(BROKER_ENDPOINT).buildAsync();
    private static final DirectMethodCallback directMethodCallback = new DirectMethodCallback();
           
    private static final MessageConsumer messageConsumer = new MessageConsumer();

    protected static class MessageConsumer implements Consumer<Mqtt5Publish> {
        private ModuleClient moduleclient;

        @Override
        public void accept(Mqtt5Publish t) {

            byte[] payload = t.getPayloadAsBytes();

            String str = new String(payload, StandardCharsets.UTF_8);

            System.out.println("Received message from the broker: " + str);

            Message moduleMessage = new Message(t.getPayloadAsBytes());

            this.moduleclient.sendEventAsync(moduleMessage, eventCallback, moduleMessage, App.OUTPUT_NAME);
        }

        public void setModuleClient(ModuleClient moduleClient) {
            this.moduleclient = moduleClient;
        }
    }

    protected static class EventCallback implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            if (context instanceof Message) {
                System.out.println("Send message with status: " + status.name());
            } else if (status != IotHubStatusCode.OK_EMPTY && status != IotHubStatusCode.OK) {
                System.out.println("Invalid context passed " + status.toString());
            }
            else
            {
                System.out.println("Direct method processed");
            }
        }
    }

    protected static class MessageCallbackMqtt implements MessageCallback {
        private int counter = 0;

        @Override
        public IotHubMessageResult execute(Message msg, Object context) {
            this.counter += 1;

            System.out.println(String.format("Received message %d: %s", this.counter,
                    new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET)));
            if (context instanceof ModuleClient) {
                ModuleClient client = (ModuleClient) context;
                client.sendEventAsync(msg, eventCallback, msg, App.OUTPUT_NAME);
            }
            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class ConnectionStatusChangeCallback implements IotHubConnectionStatusChangeCallback {

        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason,
                Throwable throwable, Object callbackContext) {
            String statusStr = "Connection Status: %s";
            switch (status) {
            case CONNECTED:
                System.out.println(String.format(statusStr, "Connected"));
                break;
            case DISCONNECTED:
                System.out.println(String.format(statusStr, "Disconnected"));
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                System.exit(1);
                break;
            case DISCONNECTED_RETRYING:
                System.out.println(String.format(statusStr, "Retrying"));
                break;
            default:
                break;
            }
        }
    }

    protected static class DirectMethodCallback implements DeviceMethodCallback {

        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            int status = METHOD_NOT_FOUND;
            String directMethodFeedback = "Method was not recognized";
            if (methodName.equals(CONNECT_METHOD_NAME))
            {
                status = METHOD_SUCCESS;
                directMethodFeedback = "Direct method executed";
                ConnectToMqttBroker(DEFAULT_TOPIC_NAME);
            }
            DeviceMethodData deviceMethodData = new DeviceMethodData(status, directMethodFeedback);
            return deviceMethodData;
        }
    }

    public static void ConnectToMqttBroker(String topicName)
    {
        mqttClient.connectWith()
        .send()
        .whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                System.out.println("Authentication failed. Please check your credentials!");
            } else {
                System.out.println("Connected to HiveMQ broker, subscribing to the topic ''" + topicName + "''.");
                mqttClient.subscribeWith()
                .topicFilter(topicName)
                .callback(messageConsumer)
                .send()
                .whenComplete((subAck, throwable2) -> {
                    if (throwable2 != null) {
                        System.out.println("Failed to subscribe to the topic ''" + topicName + "''.");
                    } else {
                        System.out.println("Subscribed to the topic '" + topicName +"''");
                    }
                });
            
            }
        });
    }

    public static void main(String[] args) {
        try {
            IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
            System.out.println("Start to create client with MQTT protocol");
            org.apache.log4j.BasicConfigurator.configure();
            ModuleClient client = ModuleClient.createFromEnvironment(protocol);
            System.out.println("Client created");
            client.setMessageCallback(App.INPUT_NAME, msgCallback, client);
            client.registerConnectionStatusChangeCallback(new ConnectionStatusChangeCallback(), null);
            client.setOperationTimeout(10000);
            client.open();

            client.subscribeToMethod(directMethodCallback, null, eventCallback, null);

            System.out.println("Connection opened");

            messageConsumer.setModuleClient(client);
            //Uncomment for debugging purposes
            //ConnectToMqttBroker(DEFAULT_TOPIC_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
