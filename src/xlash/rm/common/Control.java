package xlash.rm.common;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import xlash.rm.client.Window;

public class Control {
	
	private static boolean[] key = new boolean[256];
	private static boolean[] mouse = new boolean[3];
	
	public static void takeControl(ControlPacket packet){
		double widthRatio = Window.screenSize.getWidth()/packet.panelSize.getWidth();
		double heightRatio = Window.screenSize.getHeight()/packet.panelSize.getHeight();
		try {
			Robot robot = new Robot();
			if(packet.mouse != null){
				robot.mouseMove((int) (packet.mouse.x * widthRatio), (int) (packet.mouse.y * heightRatio));
				for(int keyPress : packet.input.key){
					if(keyPress > 0 && !key[keyPress]){
						robot.keyPress(keyPress);
						key[keyPress] = true;
					}else if(keyPress < 0 && key[-keyPress]){
						robot.keyRelease(-keyPress);
						key[-keyPress] = false;
					}
				}
				for(int mousePress : packet.input.mouse){
					int action;
					switch(Math.abs(mousePress)){
					case 1:
						action = InputEvent.BUTTON1_DOWN_MASK;
						break;
					case 2:
						action = InputEvent.BUTTON2_DOWN_MASK;
						break;
					case 3:
						action = InputEvent.BUTTON3_DOWN_MASK;
						break;
						default:
							action = 0;
					}
					if(mousePress > 0 && !mouse[mousePress-1]){
						System.out.println("Clicking " + mousePress);
						robot.mousePress(action);
						mouse[mousePress-1] = true;
					}else if(mousePress < 0 && mouse[(-mousePress)-1]){
						System.out.println("Unclicking " + mousePress);
						robot.mouseRelease(action);
						mouse[(-mousePress)-1] = false;
					}
				}
				if(packet.input.wheel != 0){
					robot.mouseWheel(packet.input.wheel);
				}
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

}
