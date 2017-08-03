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
import java.net.Socket;
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
	protected int[][] prevImage;
	protected boolean helpWanted;
	
	protected volatile boolean clientProcessedFrame;
	protected volatile boolean processedFrame;
	
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
		Thread send = new Thread("User Send"){
			@Override
			public void run(){
				send();
			}
		};
		listen.start();
		send.start();
		System.out.println("User threads");
	}
	
	public User(Socket socket, boolean helpWanted) throws IOException, NumberFormatException{
		this(socket.getInputStream(), socket.getOutputStream(), helpWanted);
	}
	
	public void send(){
		boolean run = true;
		while(run){
			try {
				if(helpWanted){
					while(!clientProcessedFrame);
					clientProcessedFrame = false;
					int[] newArray;
					int[][] screenArray;
					do{
						BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(Window.screenSize));
						screenArray = this.getRGBArray(screenshot);
						newArray = getNewPixels(screenArray);
					} while(newArray.length == 0);
					out.writeObject(new PicturePacket(newArray));
					out.flush();
					prevImage = screenArray;
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
	
	private int[][] getRGBArray(BufferedImage image) {

	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      final int width = image.getWidth();
	      final int height = image.getHeight();
	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

	      int[][] result = new int[height][width];
	      if (hasAlphaChannel) {
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      } else {
	         final int pixelLength = 3;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
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
					this.updateDisplay(packet.newPixels);
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
	
	private void updateDisplay(int[] newPixels){
		for(int i=0; i<newPixels.length; i++){
			Window.display.setRGB(newPixels[i], newPixels[i+1], newPixels[i+2]);
		}
	}

}
