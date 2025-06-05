import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Receptor{
    private static final String COLA = "historial";
    public static void main(String[]argc) throws Exception{
        ConnectionFactory rabbit = new ConnectionFactory();
        rabbit.setHost("localhost");
        Connection conexion = rabbit.newConnection();
        Channel canal = conexion.createChannel();
        canal.exchangeDeclare(COLA,"fanout");
        String nombre = canal.queueDeclare().getQueue();
        canal.queueBind(nombre,COLA,"");//enlanzando la cola al intercambia
        System.out.println("esperando mensajes ctrl c para salir");
        DeliverCallback alrecibir = (id,entrega)->{
            String mensaje = new String(entrega.getBody(),"UTF-8");
            System.out.println("recibido '"+mensaje+"'");
        };
        canal.basicConsume(nombre,true,alrecibir,id->{});
    }
}