package xlash.rm.server;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import xlash.rm.client.Input;

public class ServerPacket implements Serializable{
	
	public Point mouse;
	public SendInput input;
	
	public ServerPacket(Point mouse, SendInput input) throws IOException{
		this.mouse = mouse;
		this.input = input;
	}

}
