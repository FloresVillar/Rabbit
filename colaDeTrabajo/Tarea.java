import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Tarea{
    private static final String COLA_TAREA ="cola_tarea";
    public static void main(String [] argv) throws Exception{
        ConnectionFactory creador = new ConnectionFactory();
        creador.setHost("localhost");
        try(
            Connection conexion = creador.newConnection();
            Channel canal = conexion.createChannel();){
                canal.queueDeclare(COLA_TAREA,true,false,false,null);
                String mensaje = String.join(" ",argv);
                canal.basicPublish("",COLA_TAREA,MessageProperties.PERSISTENT_TEXT_PLAIN,mensaje.getBytes("UTF-8"));
                System.out.println("[x] enviando "+ mensaje + " ");
            }
    }

}