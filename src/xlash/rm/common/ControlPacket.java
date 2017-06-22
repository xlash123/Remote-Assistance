package xlash.rm.server;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;

public class ServerPacket implements Serializable{
	
	public Point mouse;
	public SendInput input;
	public boolean processedFrame;
	
	public ServerPacket(Point mouse, SendInput input, boolean processedFrame) throws IOException{
		this.mouse = mouse;
		this.input = input;
		this.processedFrame = processedFrame;
	}

}
