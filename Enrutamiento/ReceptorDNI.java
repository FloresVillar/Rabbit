import com.rabbitmq.client.*;
import java.util.Scanner;
import java.util.ArrayList;

public class ReceptorDNI {
  private static ArrayList<Sujeto> data;
  private static final String COLA = "cola_validardni";
  static class Sujeto{
    int id;
    int dni;
    String nombre;
    String apellidos;
    String nacimiento;
    String  ubigeo;
    String  direccion;
    int [] amigos;
    Sujeto(int ide,int dn,String nom,String apell,String nac,String ubig,String dir,String amig){
        id = ide;
        dni=dn;
        nombre = nom;
        apellidos = apell;
        nacimiento = nac;
        ubigeo = ubig;
        direccion = dir;
        String [] partes = amig.split(",");
        amigos = new int[partes.length];
        for(int i =0;i<amigos.length;i++){
          amigos[i] = Integer.parseInt(partes[i].trim());
        }
    } 
    @Override
  public String toString() {
    // Devuelve una fila con los datos alineadoss
    String amigosStr = "";
    for (int i = 0; i < amigos.length; i++) {
        amigosStr += amigos[i];
        if (i < amigos.length - 1) amigosStr += ",";
    }
    return String.format("%-3d %-8d %-10s %-15s %-12s %-8s %-15s %-12s",
                id, dni,nombre, apellidos, nacimiento, ubigeo, direccion, amigosStr.toString());
    }

  public static String etiquetas() {
        return String.format("%-3s %-8s %-10s %-15s %-12s %-8s %-15s %-12s",
                "ID", "dni","Nombre", "Apellidos", "Nacimiento", "Ubigeo", "Dirección", "Amigos");
    }

}
 //------------------------------------------------------------------------ 
  public static void main(String[] argv) throws Exception {
    data = new ArrayList<Sujeto>(); 
    System.out.println("generando los datos");
    String[] datos = {
    "1;11111111;Juan;Fernandez;1990-01-01;150101;Av. Lima 123;2,3,5",
    "2;22222222;Ana;Lopez;1988-05-12;150102;Calle Sol 456;1",
    "3;333333333;Gregory;Carter;1992-07-20;150103;Jr. Luna 789;2,1",
    "4;44444444;Antonio;Delaware;1985-03-08;150104;Psj. Estrella 321;2,3",
    "5;55555555;Bruno;Illinois;1991-11-30;150105;Av. Río 654;1,2"
};
    for(int i=0;i<datos.length;i++){
      String []partes = datos[i].split(";");
      Sujeto s =new Sujeto(Integer.parseInt(partes[0].trim()),
                          Integer.parseInt(partes[1].trim()),
                          partes[2].trim(), 
                          partes[3].trim(), 
                          partes[4].trim(), 
                          partes[5].trim(),
                          partes[6].trim(),
                          partes[7].trim());
      data.add(s);
    }
    System.out.println(Sujeto.etiquetas());
    System.out.println("-----------------------------------------------------------");
    for (Sujeto s : data) {
      System.out.println(s);
    }

    ConnectionFactory rabbit = new ConnectionFactory();
    rabbit.setHost("localhost");
    Connection conexion = rabbit.newConnection();
    Channel canal = conexion.createChannel();

    canal.exchangeDeclare(COLA, "direct");
    String nombre = canal.queueDeclare().getQueue();

    if (argv.length < 1) {
        System.err.println("Usando: obteniendoLogsDirect [info] [warning] [error]");
        System.exit(1);
    }

    for (String severidad : argv) {
        canal.queueBind(nombre, COLA, severidad);
    }
    System.out.println(" [*] esperando mensajes, para salir CTRL+C");

    DeliverCallback alrecibir = (id, entrega) -> {
        String mensaje = new String(entrega.getBody(), "UTF-8");
        System.out.println(" [x] recibido '" + entrega.getEnvelope().getRoutingKey() + "':'" + mensaje + "'");
        String resultado = validarDni(mensaje);//trabajar
        System.out.println("resultado de consuta: "+resultado);
        if(resultado.equalsIgnoreCase("si existe")){
          //enviar a emisorVENTAS
          AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(entrega.getProperties().getCorrelationId()).build();
          canal.basicPublish("", entrega.getProperties().getReplyTo(), replyProps, resultado.getBytes("UTF-8"));
        }
    };
    canal.basicConsume(nombre, true, alrecibir, id -> { });
  }
  //-----------------------------------------------------
  public static String validarDni(String mensaje){
      for(Sujeto s:data){
          if (s.dni==Integer.parseInt(mensaje)){
              return "si existe";
          }
          
      }
      return "no existe";
  }
}