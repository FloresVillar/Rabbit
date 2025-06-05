import com.rabbitmq.client.*;
import java.util.Scanner;
import java.util.ArrayList;

public class ReceptorDNI {
  private static ArrayList<Sujeto> data;
  private static final String COLA = "direct_logs";
  static class Sujeto{
    int id;
    String nombre;
    String correo;
    int clave;
    int dni;
    int telefono;
    int [] amigos;
    Sujeto(int ide,String nom,String corr,int clv,int di,int tel,String amig){
        id = ide;
        nombre = nom;
        clave = clv;
        dni = di;
        telefono = tel;
        String [] partes = amig.split(",");
        amigos = new int[partes.length];
        for(int i =0;i<amigos.length;i++){
          amigos[i] = Integer.parseInt(partes[i].trim());
        }
    } 
    @Override
public String toString() {
    // Devuelve una fila con los datos alineados
    String amigosStr = "";
    for (int i = 0; i < amigos.length; i++) {
        amigosStr += amigos[i];
        if (i < amigos.length - 1) amigosStr += ",";
    }
    return String.format("%-3d %-10s %-18s %-6d %-10d  %-10d %-12s", id, nombre, correo, clave, dni,telefono, amigosStr);
    }

public static String etiquetas() {
    return String.format("%-3s %-10s %-18s %-6s  %-10s %-10s %-12s",  "ID", "Nombre", "Correo", "Clave","dni", "Teléfono", "Amigos");
}

    }
  
  public static void main(String[] argv) throws Exception {
    data = new ArrayList<Sujeto>(); 
    System.out.println("generando los datos");
    String [] datos = {"1;Juan;juan@mail.com;1234;25426587;987654321;2,3,5",
                        "2;Ana;ana@mail.com;5678;22222222;123456789;1",
                        "3;Gregory;jn@mail.com;1234;33333333;987654345;2,1",
                        "4;Antonio;aa@mail.com;5678;44444444;123456789;2,3",
                        "5;Bruno;br@mail.com;5678;55555555;124556789;1,2"};
    for(int i=0;i<datos.length;i++){
      String []partes = datos[i].split(";");
      Sujeto s =new Sujeto(Integer.parseInt(partes[0].trim()),
                          partes[1].trim(),
                          partes[2].trim(), 
                          Integer.parseInt(partes[3].trim()),
                          Integer.parseInt(partes[4].trim()),
                          Integer.parseInt(partes[5].trim()),
                          partes[6].trim());
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
          //enviar a emisor
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