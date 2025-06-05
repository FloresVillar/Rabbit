import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class Emisor{
    private static final String COLA = "historial";
    public static void main (String[]argv) throws Exception{
        ConnectionFactory rabbit = new ConnectionFactory();
        rabbit.setHost("localhost");
        try(
            Connection conexion = rabbit.newConnection();
            Channel canal = conexion.createChannel();
        ){
            canal.exchangeDeclare(COLA,"fanout");
            String mensaje  = argv.length < 1 ? "no hay data" : String.join("",argv);
            canal.basicPublish(COLA,"",null,mensaje.getBytes("UTF-8"));
            System.out.println("enviando '"+mensaje+"'");
        }
    }

}

