package xlash.rm.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import xlash.rm.server.ClientPacket;
import xlash.rm.server.SendInput;
import xlash.rm.server.ServerPacket;

public class Client {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public boolean connected;
	
	private volatile boolean processedFrame;
	
	private Socket socket;
	
	public Client(String ip) throws IOException, NumberFormatException{
		if(!ip.contains(":")) ip = ip + ":12345";
		String host = ip.substring(0, ip.indexOf(":"));
		int port = Integer.parseInt(ip.substring(ip.indexOf(":")+1));
		socket = new Socket(host, port);
		Window.error.setText("Found server");
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		connected = true;
		processedFrame = true;
		Thread listen = new Thread("Client Listen"){
			@Override
			public void run(){
				listen();
			}
		};
		Thread send = new Thread("Client Send"){
			@Override
			public void run(){
				send();
			}
		};
		listen.start();
		send.start();
		System.out.println("Started client threads");
	}
	
	public void send(){
		boolean run = true;
		while(run){
			try {
				ServerPacket sp = new ServerPacket(Window.mouse, new SendInput(Window.input), processedFrame);
				Window.input.keys.clear();
				Window.input.mouses.clear();
				Window.input.wheel = 0;
				out.writeObject(sp);
				out.flush();
			} catch (IOException e) {
				run = false;
			}
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		connected = false;
	}
	
	public void listen(){
		ClientPacket packet;
		boolean run = true;
		while(run){
			try {
				processedFrame = false;
				packet = (ClientPacket) in.readObject();
				Window.display = ImageIO.read(new ByteArrayInputStream(packet.byteArray));
				processedFrame = true;
			} catch (ClassNotFoundException e) {
				run = false;
				System.out.println("Disconnecting because of class not found");
			} catch (IOException e) {
				run = false;
				System.out.println("Disconnecting because of IO");
			} catch (ClassCastException e){
				run = false;
				System.out.println("Disconnecting because of class cast");
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}

}
