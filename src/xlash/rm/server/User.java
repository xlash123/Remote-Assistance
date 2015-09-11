package xlash.rm.server;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import xlash.rm.client.Window;

public class User {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	public boolean connected;
	public TargetDataLine line;
	public boolean soundStopped;
	public boolean soundSupported;
	public ByteArrayOutputStream soundStream;
	public AudioInputStream ais;
	
	private AudioFormat format;
	private DataLine.Info info;
	
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
		startSound();
		listen.start();
		send.start();
		System.out.println("Started server threads");
	}
	
	public void recordSound(){
		try {
            line.start();
            
            ais = new AudioInputStream(line);
            
            File file = new File("C:/Users/Savannah/Desktop/sound.au");
            file.createNewFile();
 
            AudioSystem.write(ais, AudioFileFormat.Type.AU, file);
            
            System.exit(0);
            
            soundStopped = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
	}
	
	public void stopSound(){
		line.stop();
		line.close();
		while(!soundStopped){}
	}
	
	public void startSound(){
		format = this.getAudioFormat();
		info = new DataLine.Info(TargetDataLine.class, format);
		soundSupported = true;
		if (!AudioSystem.isLineSupported(info)) {
            soundSupported = false;
        }
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		if(soundSupported && !soundStopped){
			Thread record = new Thread("Record Sound"){
				@Override
				public void run(){
					soundStopped = false;
					soundStream = new ByteArrayOutputStream();
					recordSound();
				}
			};
			record.start();
		}
	}
	
	public void send(){
		boolean run = true;
		while(run){
			try {
				stopSound();
				out.writeObject(new ClientPacket(new Robot().createScreenCapture(new Rectangle(Window.screenSize)), this.soundStream));
				out.flush();
				startSound();
			} catch (IOException e) {
				run = false;
			} catch (AWTException e) {
				e.printStackTrace();
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
		ServerPacket packet;
		boolean run = true;
		boolean[] key = new boolean[256];
		boolean[] mouse = new boolean[3];
		while(run){
			try {
				packet = (ServerPacket) in.readObject();
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
	
	private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }

}
