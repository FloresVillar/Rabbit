import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class Recv {

    private final static String QUEUE_NAME = "colaParaSaludo";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();//creando conexion con el servidor RabbitMQ
        factory.setHost("localhost"); //la direccion del serevidor rabbit ,en cmd rabbitmq-service.bat stop ,start
        Connection connection = factory.newConnection();//
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}