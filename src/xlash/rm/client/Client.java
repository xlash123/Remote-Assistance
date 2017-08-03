package xlash.rm.client;

import java.io.IOException;
import java.net.Socket;

import xlash.rm.server.User;

public class Client extends User{
	
	public Client(String ip, boolean helpWanted) throws IOException, NumberFormatException{
		super(getConnected(ip), helpWanted);
	}
	
	private static Socket getConnected(String ip) throws IOException, NumberFormatException{
		if(!ip.contains(":")) ip = ip + ":12345";
		String host = ip.substring(0, ip.indexOf(":"));
		int port = Integer.parseInt(ip.substring(ip.indexOf(":")+1));
		Socket socket = new Socket(host, port);
		Window.error.setText("Found server");
		return socket;
	}

}
