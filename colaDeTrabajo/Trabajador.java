import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
public class Trabajador{
    private static final String COLA = "cola_tarea";
    public static void main(String[]argc) throws Exception{
        ConnectionFactory rabbit = new ConnectionFactory();
        rabbit.setHost("localhost");
        Connection conexion = rabbit.newConnection();
        Channel canal = conexion.createChannel();
        canal.queueDeclare(COLA,true,false,false,null);
        canal.basicQos(1);
        DeliverCallback alrecibir  = (id,data)->{
            String mensaje = new String(data.getBody(),"UTF-8");
            System.out.println("recibiendo '"+mensaje+"'");
            try{
                trabajar(mensaje);
            }finally{
                System.out.println("Hecho");
                canal.basicAck(data.getEnvelope().getDeliveryTag(),false);
            }
        };
        canal.basicConsume(COLA,alrecibir,id->{});
    }
    private static void trabajar(String mensaje){
        for(char ch:mensaje.toCharArray()){
            if(ch == '.'){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException _ignored){
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}