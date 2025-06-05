import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import java.util.Scanner;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP;
public class Emisor{ 
    private static final String COLA = "direct_logs";
    public static void main(String[]argv)throws Exception{
        ConnectionFactory rabbit = new ConnectionFactory();
        rabbit.setHost("localhost");
        try(Connection conexion = rabbit.newConnection();
        Channel canal = conexion.createChannel();
        Scanner in = new Scanner(System.in);){
            canal.exchangeDeclare(COLA,"direct");
            //String tipo_operacion = obtenerSeveridad(argv);
            //String mensaje = obtenerMensaje(argv);
            System.out.println("para validar 'validar_dni' y luego el dni, 'salir' para finalizar");
            boolean flag = true;
            while(true){
                String tipo_operacion = in.nextLine();
                String dni = in.nextLine();
                String seguir = in.nextLine();
                flag = seguir.equalsIgnoreCase("salir")?false:true;
                String replyQueue = canal.queueDeclare().getQueue();
                String corrId = java.util.UUID.randomUUID().toString();
                AMQP.BasicProperties respuesta = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueue).build();        
                canal.basicPublish(COLA,tipo_operacion,respuesta,dni.getBytes());
                System.out.println("enviando '"+dni+"'");
                final boolean[] respuestaRecibida = {false};
                DeliverCallback respuestaCallback = (consumerTag, entrega) -> {
                    if (entrega.getProperties().getCorrelationId().equals(corrId)) {
                    String respuest = new String(entrega.getBody(), "UTF-8");
                    System.out.println("Respuesta recibida: " + respuest);
                    //recibido la respuesta llama a ReceptorVENTAS
                    respuestaRecibida[0] = true;
                }
            };
canal.basicConsume(replyQueue, true, respuestaCallback, consumerTag -> {});

            }
        }
    } 
    public static String obtenerSeveridad(String[]arr){
        return arr[0];
    }
    public static String obtenerMensaje(String[]arr){
        if(arr.length<2) return "";
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<arr.length;i++){
            sb.append(arr[i]).append("");
        }
        return sb.toString().trim();
    }
}