package com.msft.iotlab;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
/**
 * Hello world!
 *
 */
public class App 
{
    public static final Mqtt5AsyncClient mqttClient = Mqtt5Client.builder().serverHost("hivemq.test.iotlab").buildAsync();
    private static final MessageConsumer messageConsumer = new MessageConsumer();

    protected static class MessageConsumer implements Consumer<Mqtt5Publish> {
      
        @Override
        public void accept(Mqtt5Publish t) {

            byte[] payload = t.getPayloadAsBytes();

            String str = new String(payload, StandardCharsets.UTF_8);

            System.out.println("Received message from the broker: " + str);
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ConnectToMqttBroker();
        try
        {
            System.in.read();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void ConnectToMqttBroker()
    {
        mqttClient.connectWith()
        .simpleAuth()
            .username("admin")
            .password("hivemq".getBytes())
            .applySimpleAuth()
        .send()
        .whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                System.out.println("Authentication failed. Please check your credentials!");
            } else {
                // Handle successful publish, e.g. logging or incrementing a metric
                
                System.out.println("Connected to HiveMQ broker, subscribing to the topic telemetry.");
                mqttClient.subscribeWith()
                .topicFilter("telemetry")
                .callback(messageConsumer)
                .send()
                .whenComplete((subAck, throwable2) -> {
                    if (throwable2 != null) {
                        System.out.println("Failed to subscribe to the topic 'telemetry'");
                    } else {
                        System.out.println("Subscribed to the topic 'telemetry'");
                    }
                });
            }
        });
    }
}
