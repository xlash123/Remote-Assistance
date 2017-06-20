package xlash.rm.server;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import xlash.rm.client.Window;

public class User {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	public boolean connected;
	
	private volatile boolean clientProcessedFrame;
	
	public User(InputStream is, OutputStream os) throws IOException{
		out = new ObjectOutputStream(os);
		in = new ObjectInputStream(is);
		connected = true;
		Thread listen = new Thread("User Listen"){
			@Override
			public void run(){
				listen();
			}
		};
		Thread send = new Thread("Server Send"){
			@Override
			public void run(){
				send();
			}
		};
		listen.start();
		send.start();
		System.out.println("Started server threads");
	}
	
	public void send(){
		boolean run = true;
		while(run){
			try {
				while(!clientProcessedFrame);
				clientProcessedFrame = false;
				out.writeObject(new ClientPacket(new Robot().createScreenCapture(new Rectangle(Window.screenSize))));
				out.flush();
			} catch (IOException e) {
				run = false;
			} catch (AWTException e) {
				e.printStackTrace();
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
		ServerPacket packet;
		boolean run = true;
		boolean[] key = new boolean[256];
		boolean[] mouse = new boolean[3];
		while(run){
			try {
				packet = (ServerPacket) in.readObject();
				clientProcessedFrame = packet.processedFrame;
				Robot robot = new Robot();
				if(packet.mouse != null) robot.mouseMove(packet.mouse.x, packet.mouse.y);
				for(int keyPress : packet.input.key){
					if(keyPress > 0 && !key[keyPress]) robot.keyPress(keyPress);
					else robot.keyRelease(-keyPress);
				}
				for(int mousePress : packet.input.mouse){
					if(mousePress > 0 && !mouse[mousePress-1]) robot.mousePress((int) Math.pow(2, 10 + (mousePress-1)));
					else robot.mouseRelease((int) Math.pow(2, 10 + ((-mousePress)-1)));
				}
				if(packet.input.wheel != 0) robot.mouseWheel(packet.input.wheel);
			} catch (ClassNotFoundException e) {
				run = false;
				System.out.println("Disconnecting because of class not found");
			} catch (IOException e) {
				run = false;
				System.out.println("Disconnecting because of IO");
			} catch (ClassCastException e){
				run = false;
				System.out.println("Disconnecting because of class cast");
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		connected = false;
	}

}
