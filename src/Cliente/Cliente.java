package Cliente;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

public class Cliente {

	private static final Logger LOGGER = Logger.getLogger(Cliente.class.getName());

	private Socket sc;
	private static final String SERVERIP = "127.0.0.1";
	private static final int SERVERPORT = 5555;

	public final static String PATH = "/Users/Camilo/Desktop/Lab/";
	
	private static byte[] hash;
	
	private static byte[] bytes;

	private Cliente(InetAddress serverAddress, int serverPort) {
		try {
			this.sc = new Socket(serverAddress, serverPort);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Se murio creando el socket papá", e);
		}
	}

	private Cliente() {
		try {
			SocketAddress sockaddr = new InetSocketAddress(SERVERIP, SERVERPORT);
			sc = new Socket();
			sc.connect(sockaddr);

		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, "Se murio creando el socket papá", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Se murio creando el socket papá", e);
		}
	}

	private void start() throws IOException {
		try {
			String linea;
			PrintWriter ac = new PrintWriter(sc.getOutputStream(), true);
			BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

			ac.println("HELLO");
			linea = dc.readLine();
			if (!linea.equals("ACK")) {
				ac.println("ERR");
				sc.close();
			} else {
				ac.println("READY");
				LOGGER.info(linea + "-OK, continuando.");
			}

			recieve();
			System.out.println("Comienza Hash check:");
			
			
			String hasho = DatatypeConverter.printHexBinary(hash).toUpperCase();
			System.out.println(hasho);
			
			
			String checko = DatatypeConverter.printHexBinary(bytes).toUpperCase();
			System.out.println(checko);
			
			if(checko.equals(hasho)) {
				LOGGER.info("Todo gucci");
			}
			else LOGGER.info("No gucci");
			
			
		} catch (Exception e) {

		}

		
	}

	private byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}
	
	private void recieve() {
		InputStream inputStream;
		try {
			BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

			inputStream = sc.getInputStream();
			System.out.println("Reading: ");
			
	        int size;

	        hash  = dc.readLine().getBytes();
	        LOGGER.info("Hash recibido...");
	        
	        size =  Integer.parseInt(dc.readLine());
	        System.out.println(size);
	        bytes =  new byte[size];
	        
	        int i = 0;
	        while(i<size) {
	        	
	        	
	        	bytes[i] = (byte) inputStream.read(); 
	        	i++;
	        }
	        System.out.println("FINAL I = " + i);
	        
	        
	        
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"Error recibiendo el archivo",e);
		}

        
	}

	public static void main(String[] args) throws Exception {
		/*
		 * Cliente cliente = new Cliente( InetAddress.getByName(args[0]),
		 * Integer.parseInt(args[1]));
		 */
		Cliente cliente = new Cliente();
		cliente.start();
	}
}
