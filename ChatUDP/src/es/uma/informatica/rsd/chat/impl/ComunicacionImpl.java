package es.uma.informatica.rsd.chat.impl;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import es.uma.informatica.rsd.chat.ifaces.Comunicacion;
import es.uma.informatica.rsd.chat.ifaces.Controlador;
import es.uma.informatica.rsd.chat.impl.DialogoPuerto.PuertoAlias;

// Clase a implementar 
public class ComunicacionImpl implements Comunicacion {
	public MulticastSocket ms;
	public DatagramPacket dp;
	public String alias;
	public Controlador controlador;
	public NetworkInterface ni;
	public InetSocketAddress ia;
	
	
	@Override
	public void crearSocket(PuertoAlias pa){
		try {
			ms = new MulticastSocket(pa.puerto);
			ni = ms.getNetworkInterface();
			alias = pa.alias;
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
				String msj = new String(aux, "UTF-8");
				Scanner sc = new Scanner(msj);
				sc.useDelimiter("!");
				if(dp.getAddress().isMulticastAddress()) {
					sc.next();
					String nombre = sc.next();
					String mensaje = sc.next();
					sc.close();
					if(!(alias.equals(nombre))) {
						controlador.mostrarMensaje(new InetSocketAddress(dp.getAddress(),dp.getPort()), nombre, mensaje);			
					}
				}else {
					String nombre = sc.next();
					String mensaje = sc.next();
					sc.close();
					if(!(alias.equals(nombre))) {
						controlador.mostrarMensaje(new InetSocketAddress(dp.getAddress(),dp.getPort()), nombre, mensaje);
					}
					
				}
			}
		}catch(Exception e) {
			throw new RuntimeException("Error. No se ha producido el mensaje");
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
			String b = sa.getAddress().getHostAddress() + "!" + alias + "!" + mensaje;
			byte[] buf = b.getBytes();
			dp = new DatagramPacket(buf , buf.length, sa.getAddress(), sa.getPort());
			try {
				ms.send(dp);
			} catch (IOException e) {
				throw new RuntimeException("Error al enviar el mensaje");
			}
		}else {
			String b = "!" + alias + "!" + mensaje;
			byte[] buf = b.getBytes();
			dp = new DatagramPacket(buf, buf.length, sa.getAddress(), sa.getPort());
			try {
				ms.send(dp);
			} catch (IOException e) {
				throw new RuntimeException("Error al enviar el mensaje");
			}
		}
	}

	@Override
	public void joinGroup(InetAddress multi){
		try {
			ms.joinGroup(new InetSocketAddress(multi, ia.getPort()), ni);
		}catch(Exception e) {
			throw new RuntimeException("Error al uniser al grupo.");
		}
	}

	@Override
	public void leaveGroup(InetAddress multi){
		try {
			ms.leaveGroup(new InetSocketAddress(multi, ia.getPort()), ni);
			ms.close();
		}catch(Exception e) {
			throw new RuntimeException("Error al salirse del grupo.");
		}
	}

}
