package husq; /**
 * Created by brice on 24/05/2017.
 */
import com.rabbitmq.client.*;


import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import org.json.*;
//import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class StatusCheckListener implements Runnable{

    private final static String QUEUE_NAME = "HeartBeatQueue";
    Thread listenerthread ;
    StatusCheckListener()
    {
        listenerthread = new Thread(this, "my runnable thread");
        System.out.println("my thread created" + listenerthread);
        listenerthread.start();
    }
    public void run() {
try{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.3.51.32");
        factory.setUsername("control");
        factory.setPassword("Student1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages to ControlRoom. To exit press CTRL+C");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] msg)
                    throws IOException {
                String message = new String(msg, "UTF-8");



                    JSONObject obj = new JSONObject(message);
                    String type = obj.getString("Type");
                    JSONObject body = obj.getJSONObject("Body");
                    String uuid = body.get("uuid").toString();
                    int timestampsnd = body.getInt("timestampsnd");
                    int timestampres = body.getInt("timestampres");
                    int delay = timestampres-timestampsnd;
                    int var = body.getInt("var");
                    int version = body.getInt("version");
                    System.out.println("type: " + type + "\nuuid: " + uuid + "\ndelay: " + delay + "\nvar: " + var + "\nversion: " + version);
                }
            };
    channel.basicConsume(QUEUE_NAME, true, consumer);
    }catch (Exception e) {
        e.printStackTrace();
    }
    }
    private static TransportClient setupClient() {
        // Set up transport client
        Settings settings = Settings.builder().put("cluster.name", "IntegrationProject").build();

        TransportClient client = null;
        try {

            client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.1.2"), 8080));

        } catch (UnknownHostException e) {

            e.printStackTrace();
        }

        return client;
    }
}
