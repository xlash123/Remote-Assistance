package xlash.rm.client;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import xlash.rm.server.ClientPacket;
import xlash.rm.server.SendInput;
import xlash.rm.server.ServerPacket;

public class Client {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	public boolean connected;
	
	public Client(String ip) throws IOException, NumberFormatException{
		if(!ip.contains(":")) ip = ip + ":12345";
		String host = ip.substring(0, ip.indexOf(":"));
		int port = Integer.parseInt(ip.substring(ip.indexOf(":")+1));
		Socket socket = new Socket(host, port);
		Window.error.setText("Found server");
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
		connected = true;
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
				ServerPacket sp = new ServerPacket(Window.mouse, new SendInput(Window.input));
				Window.input.keys.clear();
				Window.input.mouses.clear();
				Window.input.wheel = 0;
				out.writeObject(sp);
				out.flush();
			} catch (IOException e) {
				run = false;
			}
			try {
				Thread.sleep(10);
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
				packet = (ClientPacket) in.readObject();
				Window.display = ImageIO.read(new ByteArrayInputStream(packet.byteArray));
				//AudioInputStream audio = AudioSystem.getAudioInputStream(new ByteArrayInputStream(packet.sound));
				//Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audio.getFormat()));
				//clip.open(audio);
				//clip.start();
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
		connected = false;
	}

}
