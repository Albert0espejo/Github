package es.uma.informatica.rsd.chat.impl;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import es.uma.informatica.rsd.chat.ifaces.Comunicacion;
import es.uma.informatica.rsd.chat.ifaces.Controlador;
import es.uma.informatica.rsd.chat.impl.DialogoPuerto.PuertoAlias;

// Clase a implementar 
public class ComunicacionImpl implements Comunicacion {
	public MulticastSocket ms;
	public DatagramPacket dp;
	public String alias;
	public int puerto;
	public Controlador controlador;
	public NetworkInterface ni;
	
	
	@Override
	public void crearSocket(PuertoAlias pa){
		alias = pa.alias;
		puerto = pa.puerto;
		try {
			ms = new MulticastSocket(puerto);
		}catch(Exception e) {
			throw new RuntimeException("Error. Al crear el socket.");
		}
	}

	@Override
	public void setControlador(Controlador c) {
		controlador = c;
	}

	@Override  //CAMBIAR EL IF DESPUES DEL TRY
	public void runReceptor(){
		try {
			while(true) {
				byte[] aux = new byte[500];	
				dp = new DatagramPacket(aux, aux.length);
				ms.receive(dp);
				String msj = new String(dp.getData(),Charset.forName("UTF-8"));
				System.out.println(msj);
				String[] sc = msj.split("!"); 
				if(sc[0].isEmpty()) {//unicast
					controlador.mostrarMensaje(new InetSocketAddress(dp.getAddress(), dp.getPort()), sc[1], sc[2]);
				}else {//multicast
					InetAddress dirReal = InetAddress.getByName(sc[0]);
					if(sc[1]!=alias) {
						System.out.println("No es mi mensaje");
						controlador.mostrarMensaje(new InetSocketAddress(dirReal, dp.getPort()), sc[1], sc[2]);
					}
				}
			}
		}catch(NoSuchElementException e) {
			System.err.print("Error en la introduccion de datos.");
		}catch(SocketException e) {
			System.err.print("Error en recibir mensaje");
		}catch(IOException e) {
			System.err.print("Error general");
		}
	}
	
	
/*	@Override  //CAMBIAR EL IF DESPUES DEL TRY
	public void runReceptor(){
		byte[] aux = new byte[500];	
		dp = new DatagramPacket(aux, aux.length);
		if(dp.getAddress().isMulticastAddress() == true) {
			try {
				ms.receive(dp);
				String msj = new String(aux, "UTF-8");
				Scanner sc = new Scanner(msj);
				sc.useDelimiter("!");
				sc.next();
				String nombre = sc.next();
				String mensaje = sc.next();
				sc.close();
				if(nombre != alias) {
				controlador.mostrarMensaje(ia, nombre, mensaje);					
				}
			}catch(Exception e) {
				throw new RuntimeException("Error. No se ha producido el mensaje");
			}
		}else {
			try {
				//dp = new DatagramPacket(aux, aux.length, ia.getAddress(), ia.getPort());
				ms.receive(dp);
				String msj = new String(aux, "UTF-8");
				Scanner sc = new Scanner(msj);
				sc.useDelimiter("!");
				sc.next();
				String nombre = sc.next();
				String mensaje = sc.next();
				sc.close();
				controlador.mostrarMensaje(ia, nombre, mensaje);
			}catch(Exception e) {
				throw new RuntimeException("Error. No se ha producido el mensaje");
			}
		}
	}*/


	
	@Override
	public void envia(InetSocketAddress sa, String mensaje){
		if(sa.getAddress().isMulticastAddress()) {
			String b = sa.getAddress() + "!" + alias + "!" + mensaje;
			String[] line2 = b.split("/");
			byte[] buf = new byte[500];
			b = line2[1];
			try {
				buf = b.getBytes("UTF-8");
				dp = new DatagramPacket(buf , buf.length, sa.getAddress(), sa.getPort());
				ms.send(dp);
				System.out.println(b);
			} catch (IOException e) {
				throw new RuntimeException("Error al enviar el mensaje");
			}
		}else {
			String b = "!" + alias + "!" + mensaje;
			byte[] buf = new byte[500];
			try {
				buf = b.getBytes("UTF-8");
				dp = new DatagramPacket(buf, buf.length, sa.getAddress(), sa.getPort());
				ms.send(dp);
				System.out.println(b);
			} catch (IOException e) {
				throw new RuntimeException("Error al enviar el mensaje");
			}
		}
	}

	@Override
	public void joinGroup(InetAddress multi){
		try {
			ms.joinGroup(new InetSocketAddress(multi, ms.getLocalPort()), NetworkInterface.getByName("192.168.245.59"));
		}catch(IOException e) {
			System.err.println("Error al uniser al grupo.");
		}
	}

	@Override
	public void leaveGroup(InetAddress multi){
		try {
			ms.leaveGroup(multi);
			ms.close();
		}catch(IOException e) {
			System.err.println("Error al uniser al grupo.");
		}
	}

}
