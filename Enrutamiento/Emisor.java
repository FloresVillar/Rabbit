import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import java.util.Scanner;
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
                canal.basicPublish(COLA,tipo_operacion,null,dni.getBytes());
                System.out.println("enviando '"+dni+"'");
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