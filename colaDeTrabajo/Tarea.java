import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Tarea{
    private static final String COLA ="cola_tarea";
    public static void main(String [] argv) throws Exception{
        ConnectionFactory rabbit = new ConnectionFactory();
        rabbit.setHost("localhost");
        try(
            Connection conexion = rabbit.newConnection();
            Channel canal = conexion.createChannel();){
                canal.queueDeclare(COLA,true,false,false,null);
                String mensaje = String.join(" ",argv);
                canal.basicPublish("",COLA,MessageProperties.PERSISTENT_TEXT_PLAIN,mensaje.getBytes("UTF-8"));
                System.out.println("[x] enviando "+ mensaje + " ");
            }
    }

}