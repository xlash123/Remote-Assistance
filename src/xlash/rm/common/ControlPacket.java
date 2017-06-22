package xlash.rm.common;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;

import xlash.rm.server.SendInput;

public class ControlPacket implements Serializable{
	
	public Point mouse;
	public SendInput input;
	public boolean processedFrame;
	public Dimension panelSize;
	
	public ControlPacket(Point mouse, SendInput input, Dimension panelSize, boolean processedFrame) throws IOException{
		this.mouse = mouse;
		this.input = input;
		this.processedFrame = processedFrame;
		this.panelSize = panelSize;
	}

}
