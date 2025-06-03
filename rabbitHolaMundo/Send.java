import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Send{
    private final static String COLA_DE_MENSAJES = "colaParaSaludo";
    public static void main(String[]argv) throws Exception{
        ConnectionFactory creador = new ConnectionFactory();
        creador.setHost("localHost");
        try(
            Connection conexion = creador.newConnection();
            Channel canal = conexion.createChannel();
        ){
            canal.queueDeclare(COLA_DE_MENSAJES,false,false,false,null);//creando
            String mensaje = "Hello World";
            canal.basicPublish("",COLA_DE_MENSAJES,null,mensaje.getBytes(StandardCharsets.UTF_8));//publicando el mensaje
            System.out.println("[x] enviando '"+mensaje+"'");
        }
    }

}