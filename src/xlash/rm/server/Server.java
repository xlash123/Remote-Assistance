package xlash.rm.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import xlash.rm.client.Window;

public class Server{
	
	public ServerSocket server;
	public User user;
	private boolean helpWanted;

	public Server(int port, boolean helpWanted) throws IOException{
		this.helpWanted = helpWanted;
		server = new ServerSocket(port);
		Thread handshake = new Thread("Handshake"){
			@Override
			public void run(){
				handshake();
			}
		};
		handshake.start();
	}
	
	public void handshake(){
		while(true){
			if(user != null && !user.connected){
				user = null;
				Window.error.setText("User has diconnected.");
			}
			while(user == null){
				try {
					Socket client = server.accept();
					Window.error.setText("Connection with user.");
					user = new User(client.getInputStream(), client.getOutputStream(), helpWanted);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
