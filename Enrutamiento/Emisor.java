import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import java.util.Scanner;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP;
import java.util.UUID;
public class Emisor{ 
    private static final String COLA = "cola_validardni";
    //---------------------------------------------------
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
                    if (respuest.equals("si existe")) {
                    // Enviar el dni a ReceptorVENTAS
                        String corrIdVentas = UUID.randomUUID().toString();
                        String replyVentas = canal.queueDeclare().getQueue();

                        AMQP.BasicProperties propsVentas = new AMQP.BasicProperties.Builder().correlationId(corrIdVentas).replyTo(replyVentas).build();
                        String dnii =dni;
                        if (dni.equals("11111111")) {
                        dnii = "1;Juan;juan@mail.com;1234;25426587;987654321;2,3,5";
                        } else if (dni.equals("22222222")) {
                            dnii = "2;Ana;ana@mail.com;5678;22222222;123456789;1";
                        } else if (dni.equals("33333333")) {
                             dnii = "3;Gregory;jn@mail.com;1234;33333333;987654345;2,1";
                        } else if (dni.equals("44444444")) {
                            dnii = "4;Antonio;aa@mail.com;5678;44444444;123456789;2,3";
                        } else if (dni.equals("55555555")) {
                            dnii = "5;Bruno;br@mail.com;5678;55555555;124556789;1,2";
                        }
                        canal.basicPublish("", "cola_ventas", propsVentas, dnii.getBytes("UTF-8"));
                        final boolean[] recibidoVentas = {false};
                        DeliverCallback respuestaVentas = (tag, entregaa) -> {
                        if (entregaa.getProperties().getCorrelationId().equals(corrIdVentas)) {
                            String datosVentas = new String(entregaa.getBody(), "UTF-8");
                            System.out.println("Info de actualizacion recibida: " + datosVentas);
                            recibidoVentas[0] = true;
                            }
                        };
                        canal.basicConsume(replyVentas, true, respuestaVentas, tag -> {});
                    }
                    respuestaRecibida[0] = true;
                }
            };
            canal.basicConsume(replyQueue, true, respuestaCallback, consumerTag -> {});
            }
        }
    } 
    //------------------------------------------
    public static String obtenerSeveridad(String[]arr){
        return arr[0];
    }
    //--------------------------------------------------
    public static String obtenerMensaje(String[]arr){
        if(arr.length<2) return "";
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<arr.length;i++){
            sb.append(arr[i]).append("");
        }
        return sb.toString().trim();
    }
    //-----------------------------------------------------
}