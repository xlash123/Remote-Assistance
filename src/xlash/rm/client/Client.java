package xlash.rm.client;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import xlash.rm.common.Control;
import xlash.rm.common.ControlPacket;
import xlash.rm.common.PicturePacket;
import xlash.rm.server.SendInput;

public class Client {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public boolean connected;
	
	private volatile boolean processedFrame;
	private boolean helpWanted;
	
	private Socket socket;
	private volatile boolean clientProcessedFrame;
	
	public Client(String ip, boolean helpWanted) throws IOException, NumberFormatException{
		this.helpWanted = helpWanted;
		if(!ip.contains(":")) ip = ip + ":12345";
		String host = ip.substring(0, ip.indexOf(":"));
		int port = Integer.parseInt(ip.substring(ip.indexOf(":")+1));
		socket = new Socket(host, port);
		Window.error.setText("Found server");
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		connected = true;
		processedFrame = true;
		clientProcessedFrame = true;
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
				if(helpWanted){
					while(!clientProcessedFrame);
					clientProcessedFrame = false;
					BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(Window.screenSize));
					out.writeObject(new PicturePacket(screenshot));
					out.flush();
				}else{
					ControlPacket sp = new ControlPacket(Window.mouse, new SendInput(Window.input), Window.panelSize, processedFrame);
					out.writeObject(sp);
					Window.input.keys.clear();
					Window.input.mouses.clear();
					Window.input.wheel = 0;
				}
				out.flush();
			} catch (IOException | AWTException e) {
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
		boolean run = true;
		while(run){
			try {
				if(helpWanted){
					ControlPacket packet = (ControlPacket) this.in.readObject();
					clientProcessedFrame = packet.processedFrame;
					Control.takeControl(packet);
				}else{
					processedFrame = true;
					PicturePacket packet = (PicturePacket) in.readObject();
					processedFrame = false;
					Window.display = ImageIO.read(new ByteArrayInputStream(packet.byteArray));
				}
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
