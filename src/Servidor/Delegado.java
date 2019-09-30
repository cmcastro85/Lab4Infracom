package Servidor;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

public class Delegado extends Thread{

	public static final String ACK = "ACK";
	public static final String ARCH = "ARCH";
	public static final String HASH = "HASH";
	public static final String ERR = "ERR";
	public static final String HELLO = "HELLO";
	public static final String READY = "READY";
	public static final int BUFFER = 1;
	
    private static final Logger LOGGER = Logger.getLogger(Delegado.class.getName());

	private Counter contador;
    
	private Socket sc;
	
	private int ip;
	
	private byte[] hash;
	
	private byte[] bytes;
	

	Delegado(Socket pSc,int pIp,Counter pContador,byte[] pBytes, byte[] pHash) {
		sc = pSc;
		ip = pIp;
		contador = pContador;
		hash = pHash;
		bytes = pBytes;
		
	}
	
	public  void  run() {
		LOGGER.info("Delegado " + ip + ": Empezando conexión.");
		
		String linea;
		
		try {
			PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
			BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

			/***** Fase 1: Inicio *****/

			linea = dc.readLine();
			if (!linea.equals(HELLO)) {
				ac.println(ERR);
			    sc.close();
				throw new Exception(ip + ERR + linea +"-terminando.");
			} else {
				ac.println(ACK);
				LOGGER.info(ip + linea + "-OK, continuando.");
			}
			
			/***** Fase 2: Espera OK *****/

			linea = dc.readLine();
			if (!linea.equals(READY)) {
				ac.println(ERR);
			    sc.close();
				throw new Exception(ip + ERR + linea +"-terminando.");
			} else {
				LOGGER.info(ip + linea + "- Esperando a los demás usuarios.");
				contador.increment();
				synchronized(contador) {
					contador.wait();
				}
				
			}
			
			/***** Fase 3: Archivo *****/
			
			LOGGER.info(ip + ": Transmitiendo archivo");
			
			Long inicio = System.currentTimeMillis();
			send();
			Long fin = System.currentTimeMillis();
			float total = fin -inicio;
	        total = total / 1000;
			LOGGER.info("Demora: "+ total+ " segundos.");
	        
	        sc.close();
			
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error en el printwriter/buffer",e);
		}
		
	}
	private void send() {
		
		try {
			PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
			LOGGER.info("Creando hash");
	        
	        
	        ac.println(hash);
	        String hasho = DatatypeConverter.printHexBinary(hash).toUpperCase();
	        System.out.println(hasho);
	        LOGGER.info("Hash enviado");
			
			
			ac.println(bytes.length);
			
			OutputStream out = sc.getOutputStream();
			
			byte[] buffer = new byte[BUFFER];
			int pos = 0;
			int i  = 0;
			int restante = bytes.length;
			for(byte b:bytes) {
				buffer[pos] = b;
				pos++;
				i++;
				if(pos == buffer.length) {
					out.write(buffer);
					pos = 0;
					restante -= buffer.length;
					
					if(restante >= BUFFER) {
						buffer = new byte[BUFFER];
					}else {
						buffer = new byte[restante];
						System.out.println("Last pass: " + restante);
					}
				}
			}
			System.out.println("FINAL I = "+ i);
			
	        
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error al mandar el archivo.",e);
		}

	}
	
}
