package xlash.rm.server;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import xlash.rm.client.Window;
import xlash.rm.common.Control;
import xlash.rm.common.ControlPacket;
import xlash.rm.common.PicturePacket;

public class User {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	public boolean connected;
	private int[][] prevImage;
	private boolean helpWanted;
	
	private volatile boolean clientProcessedFrame;
	private volatile boolean processedFrame;
	
	public User(InputStream is, OutputStream os, boolean helpWanted) throws IOException{
		this.helpWanted = helpWanted;
		out = new ObjectOutputStream(os);
		in = new ObjectInputStream(is);
		connected = true;
		clientProcessedFrame = true;
		processedFrame = true;
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
	
	private int[] getNewPixels(int[][] imgArray){
		int width = imgArray[0].length;
		int height = imgArray.length;
		int index = 0;
		int[] almostResult = new int[width*height*3];
		for(int h=0; h<height; h++){
			for(int w=0; w<width; w++){
				if(prevImage[h][w]!=imgArray[h][w]){
					almostResult[index] = h;
					almostResult[index+1] = w;
					almostResult[index+2] = imgArray[h][w];
					index += 3;
				}
			}
		}
		return Arrays.copyOf(almostResult, index);
	}
	
	private int[][] getImageArray(BufferedImage img){
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] result = new int[width][height];
		for(int h=0; h<height; h++){
			for(int w=0; w<width; w++){
				result[h][w] = img.getRGB(w, h);
			}
		}
		return result;
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
		connected = false;
	}

}
