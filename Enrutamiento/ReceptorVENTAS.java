import com.rabbitmq.client.*;
import java.util.ArrayList;
public class ReceptorVENTAS {
  private static final String COLA = "cola_ventas";
  private static ArrayList<Sujeto> data=new ArrayList<Sujeto>();
  //--------------------------------------------
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
        correo = corr;
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
  //---------------------------------------------
  public static void main(String[] args) throws Exception {
    ConnectionFactory rabbit = new ConnectionFactory();
    rabbit.setHost("localhost");
    Connection conexion = rabbit.newConnection();
    Channel canal = conexion.createChannel();

    canal.queueDeclare(COLA, false, false, false, null);
    System.out.println("[*] Esperando DNI(datos) para generar datos de ventas");

    DeliverCallback alrecibir = (idRecv, entrega) -> {
      String dni = new String(entrega.getBody(), "UTF-8");
      String respuesta = generarDatosVentas(dni);

      AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(entrega.getProperties().getCorrelationId()).build();

      canal.basicPublish("", entrega.getProperties().getReplyTo(), props, respuesta.getBytes("UTF-8"));
      System.out.println("[x] Datos enviados: " + respuesta);
    };

    canal.basicConsume(COLA, true, alrecibir, idRecv -> {});
  }
    //-----------------------------------------------------
  public static String generarDatosVentas(String dnii) {
    // Aquí generas información de ventas relacionada con el DNI
    String []partes = dnii.split(";");
    Sujeto s =new Sujeto(Integer.parseInt(partes[0].trim()),
                          partes[1].trim(),
                          partes[2].trim(), 
                          Integer.parseInt(partes[3].trim()),
                          Integer.parseInt(partes[4].trim()),
                          Integer.parseInt(partes[5].trim()),
                          partes[6].trim());
    data.add(s);
    System.out.println(Sujeto.etiquetas());
    System.out.println("-----------------------------------------------------------");
    for (Sujeto ss : data) {
      System.out.println(ss);
    }
    return "DNI " + dnii + " se agregaron sus datos";
  }
  //-----------------------------------------------------------
}
