package xlash.rm.client;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import xlash.rm.server.Server;

public class Window extends JFrame{
	
	private JButton connect;
	private JTextField ip;
	private JButton ask;
	public static JLabel error;
	private JPanel panel;
	
	public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static Dimension panelSize;
	
	public static Server server;
	public static Client client;
	
	public static Input input;
	public static BufferedImage display;
	public static Point mouse;
	
	public Window(int width, int height){
		input = new Input();
		this.setSize(width, height);
		this.setPreferredSize(this.getSize());
		this.initComponents();
		this.setTitle("Remote Assistance");
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		Thread tick = new Thread("Tick"){
			@Override
			public void run(){
				tick();
			}
		};
		tick.start();
	}
	
	public void tick(){
		while(true){
			if(panel != null && panel.getMousePosition() != null && display != null){
				mouse = panel.getMousePosition();
			}
			if(client != null && !client.connected){
				client = null;
				error.setText("Disconnected from the server.");
				Window.this.setGuiVisible(true);
			}
			this.repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initComponents() {

        connect = new javax.swing.JButton();
        ip = new javax.swing.JTextField();
        ask = new javax.swing.JButton();
        error = new JLabel();
        error.setForeground(Color.black);
        panel = new JPanel(){
        	@Override
        	public void paintComponent(Graphics g){
        		Graphics2D g2d = (Graphics2D) g;
        		super.paintComponent(g2d);
        		draw(g2d);
        	}
        };
        
        panel.setFocusable(true);
        panel.addKeyListener(input);
        panel.addMouseListener(input);
        panel.addMouseWheelListener(input);
        
        this.panel.addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e){
        		Window.panelSize = Window.this.panel.getSize();
        	}
		});
        
        connect.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent arg0){
        		Thread connect = new Thread("Connect"){
        			@Override
        			public void run(){
        				if(!ip.getText().contains(".") && !ip.getText().contains(":")){
							try{
							int port = Integer.parseInt(ip.getText());
							if(port >= 0 && port <= 65535){
								server = new Server(port, false);
								error.setText("Server on port " + port + ".");
								Window.this.setGuiVisible(false);
							}else{
								error.setText("Valid ports are between 0 and 65535.");
								return;
							}
							}catch(NumberFormatException e){
								error.setText("Valid ports are between 0 and 65535.");
							}catch(IOException e){
								error.setText("Unable to start server.");
							}
						}else{
							try{
								client = new Client(Window.this.ip.getText(), false);
								panel.requestFocus();
								Window.this.setGuiVisible(false);
							}catch(NumberFormatException e){
								error.setText("Valid ports are between 0 and 65535.");
							}catch(IOException e){
								error.setText("Server not found. Try again.");
							}
						}
        			}
        		};
        		connect.start();
        	}
        });
        
        ask.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
						if(!ip.getText().contains(".") && !ip.getText().contains(":")){
							try{
							int port = Integer.parseInt(ip.getText());
							if(port >= 0 && port <= 65535){
								server = new Server(port, true);
								error.setText("Server on port " + port + ".");
								Window.this.setGuiVisible(false);
							}else{
								error.setText("Valid ports are between 0 and 65535.");
								return;
							}
							}catch(NumberFormatException e){
								error.setText("Valid ports are between 0 and 65535.");
							}catch(IOException e){
								error.setText("Unable to start server.");
							}
						}else{
							try{
								client = new Client(Window.this.ip.getText(), true);
								panel.requestFocus();
								Window.this.setGuiVisible(false);
							}catch(NumberFormatException e){
								error.setText("Valid ports are between 0 and 65535.");
							}catch(IOException e){
								error.setText("Server not found. Try again.");
							}
						}
					}
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        connect.setText("I'll help");

        ask.setText("I need help");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(273, Short.MAX_VALUE)
                .addComponent(connect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ip, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ask)
                .addContainerGap(260, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup())
            	.addComponent(error)
            .addGroup(layout.createSequentialGroup())
            	.addComponent(panel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(566, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(error)
                    .addComponent(connect)
                    .addComponent(ip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ask))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup())
            	.addComponent(panel)
        );

        pack();
    }
	
	public void draw(Graphics2D g2d){
		try {
			if(client == null && server == null){
				display = new Robot().createScreenCapture(new Rectangle(screenSize));
			}
			g2d.drawImage(display, 0, 0, panel.getWidth(), panel.getHeight(), null);
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public void setGuiVisible(boolean visible){
		this.ask.setVisible(visible);
		this.connect.setVisible(visible);
		this.ip.setVisible(visible);
		panel.requestFocus();
	}

}
