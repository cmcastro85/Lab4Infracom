package Servidor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//  www.java2s.com

public class MyServerSocket {
  
    private static final Logger LOGGER = Logger.getLogger(MyServerSocket.class.getName());

    private static int socket = 5555;
    

    private static final File file1 = new File("/Users/Camilo/Desktop/foto.jpg");
	
    private static final File file2 = new File("/Users/Camilo/Desktop/foto.jpg");
    
    private static byte[] bytes; 
    
    private static byte[] hash;

    private static File file;
    
    private static Counter contador = new Counter();
    
	public static void main(String[] args) throws Exception {
    
	ExecutorService exec = Executors.newFixedThreadPool(25);
	
	
	InputStreamReader isr = new InputStreamReader(System.in);
	BufferedReader br = new BufferedReader(isr);
	
	System.out.println("¿A cuantos clientes se le compartirá el archivo? ");
	int nCl = Integer.parseInt(br.readLine());
	
	System.out.println("¿Que archivo se debe mandar? ");
	if(br.readLine().equals("1")) {
		file = file1;
		bytes = new byte[(int)file.length()];
		FileInputStream fi = new FileInputStream(file);
		fi.read(bytes);
		fi.close();
		MessageDigest ms = MessageDigest.getInstance("MD5");
        hash = ms.digest(Files.readAllBytes(file.toPath()));
	}
	else {
		file = file2;
		bytes = new byte[(int)file.length()];
		FileInputStream fi = new FileInputStream(file);
		fi.read(bytes);
		fi.close();
		MessageDigest ms = MessageDigest.getInstance("MD5");
        hash = ms.digest(Files.readAllBytes(file.toPath()));
	}
	
	@SuppressWarnings("resource")
	ServerSocket serverSocket = new ServerSocket(socket, 25,
        InetAddress.getByName("localhost"));
    
	int id = 0;
    LOGGER.info("Server started at " + serverSocket);

    while (true) {
      LOGGER.info("Waiting for a  connection...");
      
      final Socket activeSocket = serverSocket.accept();

      LOGGER.info("Received a  connection from  " + activeSocket);
      
      exec.execute(new Delegado(activeSocket, id,contador,bytes,hash));
      
      id++;
      
      if(id == nCl) {
    	  break;
      }
    }
    while(true) {
    	if(contador.getCount() == nCl) {
        	synchronized(contador) {
        		LOGGER.info("Comenzando envio...");
        		contador.notifyAll();
        	}
        	exec.awaitTermination(8, TimeUnit.SECONDS);
        	serverSocket.close();
        	break;
        }
    }
    
  }

}