
package org.example;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;

/**
 *
 * @author almacro
 */
public class Slapshot 
        extends Frame 
        implements ActionListener, MouseListener, MouseMotionListener, Runnable {

    MenuBar menubar;
    Menu fileMenu;
    MenuItem startMenuItem;
    MenuItem endMenuItem;
    MenuItem setSpeedMenuItem;
    MenuItem exitMenuItem;
    
    MediaTracker tracker;
    Image backgroundImage;
    Image memoryImage;
    Graphics memoryGraphics;
    Image[] gifImages = new Image[2];
    
    OkCancelDialog textDialog;
    Label label1, label2;
    
    int yourScore, theirScore;
    int maxVelocity = 10;
    int speed = 50;
    int retVal = 0;
    int offsetX = 0;
    int offsetY = 0;
    
    boolean runOK = true;
    boolean stop = true;
    boolean dragging = false;
    Thread thread;
    
    java.util.List<Puck> pucks = new ArrayList<>();
    
    Slapshot() {
        setTitle("Slapshot!");
        
        menubar = new MenuBar();
        fileMenu = new Menu("File");
        
        startMenuItem = new MenuItem("Start");
        fileMenu.add(startMenuItem);
        startMenuItem.addActionListener(this);
        
        endMenuItem = new MenuItem("End");
        fileMenu.add(endMenuItem);
        endMenuItem.addActionListener(this);
        
        setSpeedMenuItem = new MenuItem("Set speed");
        fileMenu.add(setSpeedMenuItem);
        setSpeedMenuItem.addActionListener(this);
        
        exitMenuItem = new MenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(this);
        
        menubar.add(fileMenu);
        setMenuBar(menubar);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        textDialog = new OkCancelDialog(this, "Set speed (1-100)", true);
        
        setLayout(null);
        label1 = new Label();
        label1.setText("0");
        label1.setBounds(180, 310, 20, 20);
        label1.setVisible(false);
        add(label1);
        
        label2 = new Label();
        label2.setText("0");
        label2.setBounds(400, 310, 20, 20);
        label2.setVisible(false);
        add(label2);

        
        tracker = new MediaTracker(this);
        URL u = getClass().getClassLoader().getResource("puck.gif");
        gifImages[0] = Toolkit.getDefaultToolkit().getImage(u);
        tracker.addImage(gifImages[0], 0);
        
        u = getClass().getClassLoader().getResource("blocker.gif");
        gifImages[1] = Toolkit.getDefaultToolkit().getImage(u);
        tracker.addImage(gifImages[1], 0);
        
        u = getClass().getClassLoader().getResource("rink.gif"); 
        backgroundImage = Toolkit.getDefaultToolkit().getImage(u);
        tracker.addImage(backgroundImage, 0);
        
        try {
            tracker.waitForID(0);
        } catch(InterruptedException e) {
            System.out.println(e);
        }

        setSize(backgroundImage.getWidth(this), backgroundImage.getHeight(this));
        setResizable(false);
        setVisible(true);
        
        memoryImage = createImage(getSize().width, getSize().height);
        memoryGraphics = memoryImage.getGraphics();
        
        thread = new Thread(this);
        thread.start();
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                runOK = false;
                System.exit(0);
            }
        });
    }
    
    @Override
    public void run() {
        Puck puck;
        while(runOK) {
            if (!stop) {
                int numberLeft;
                for (int i = 0; i < 12; i++) {
                    puck = pucks.get(i);

                    if (puck.gone()) {
                        continue;
                    }

                    movePuck(puck);

                    int struckPuck = -1;
                    for (int j = 0; j < 13; j++) {
                        Puck testPuck = pucks.get(j);
                        if (puck == testPuck || testPuck.gone()) {
                            continue;
                        }

                        if (puck.rectangle.intersects(testPuck.rectangle)) {
                            struckPuck = j;
                        }
                    }
                    
                    if (struckPuck >= 0) {
                        Puck puck1 = (Puck) pucks.get(struckPuck);
                        Puck puck2 = puck;

                        if (puck2.immovable()) {
                            puck1.velocity.x = -puck1.velocity.x;
                            movePuck(puck1);
                        } else if (puck1.immovable()) {
                            puck2.velocity.x = -puck2.velocity.x;
                            movePuck(puck2);
                        } else {
                            movePuck(puck1);
                            movePuck(puck2);
                        }
                    }
                    
                    int lowestTime = 10000;
                    int impactY = -1;
                    
                    for (int j = 0; j < 12; j++) {
                        Puck movingPuck = pucks.get(j);
                        Rectangle r = movingPuck.rectangle;
                        Point p = new Point(r.x, r.y);
                        Point v = movingPuck.velocity;

                        int w = backgroundImage.getWidth(this);
                        if (v.x > 0 && !movingPuck.gone()) {
                            int yHit = (v.y / v.x) * (w - p.x) + p.y;

                            if (yHit > 115 && yHit < 223) {
                                int time = (w - p.x) / v.x;
                                if (time <= lowestTime) {
                                    impactY = yHit;
                                }
                            }
                        }
                        
                        if(impactY > 0) {
                            Puck block = pucks.get(12);
                            int pos = block.rectangle.y;
                            
                            if(pos < impactY) {
                                block.slide(Math.min(pos + 40, impactY));
                            } else {
                                block.slide(Math.max(pos - 40, impactY));
                            }
                            repaint();
                        }
                        label2.setText(String.valueOf(theirScore));
                    }
                }
                
            }

            repaint();
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void movePuck(Puck puck) {
        retVal = puck.slide(
                pucks.get(13).rectangle,
                pucks.get(12).rectangle
        );
        
        int numberLeft = 0;
        for (int i = 0; i < 12; i++) {
            if (!pucks.get(i).gone()) {
                numberLeft++;
            }
        }
        
        if (retVal < 0) {
            if (yourScore + theirScore + numberLeft == 11) {
                label1.setText(String.valueOf(++yourScore));
            }
        }

        if (retVal > 0) {
            if (yourScore + theirScore + numberLeft == 11) {
                label2.setText(String.valueOf(++theirScore));
            }
        }
    }
    
    @Override
    public void update(Graphics g) {
        memoryGraphics.drawImage(backgroundImage, 0, 0, this);
        
        for(Puck puck: pucks) {
            if(!stop) {
                puck.drawPuckImage(memoryGraphics);
            }
        }
        
        g.drawImage(memoryImage, 0, 0, this);
    }
    
    public static void main(String[] args) {
        new Slapshot();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startMenuItem) {
            if(!stop) {
                stop = true;
                repaint();
            }
            init();
            label1.setVisible(true);
            label2.setVisible(true);
            stop = false;
            label1.setText("0");
            label2.setText("0");
            yourScore = 0;
            theirScore = 0;
        }
        
        if(e.getSource() == setSpeedMenuItem) {
            textDialog.setVisible(true);
            if(!textDialog.data.equals("")) {
                int newSpeed = Integer.parseInt(textDialog.data);
                newSpeed = 101 - newSpeed;
                if(newSpeed >= 1 && newSpeed <= 100) {
                    speed = newSpeed;
                }
            }
        }
        
        if(e.getSource() == exitMenuItem) {
            runOK = false;
            System.exit(0);
        }
    }

    public void init() {
        //Point position, velocity;
        pucks = new ArrayList<>();
        
        Rectangle edges = new Rectangle(
                10 + getInsets().left,
                getInsets().top,
                getSize().width - (getInsets().left + getInsets().right),
                getSize().height - (getInsets().top + getInsets().bottom)
        );
        
        for(int i=0; i<12; i++) {
            pucks.add(new Puck(gifImages[0], 0, maxVelocity, edges, this));
            try {
                Thread.sleep(20);
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        pucks.add(new Puck(gifImages[1], 1, maxVelocity, edges, this));
        pucks.add(new Puck(gifImages[1], 2, maxVelocity, edges, this));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Rectangle r = pucks.get(13).rectangle;
        if(r.contains(new Point(e.getX(), e.getY()))) {
            offsetX = e.getX() - r.x;
            offsetY = e.getY() - r.y;
            dragging = true;
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if(dragging) {
            int newY = e.getY() - offsetY;
            
            pucks.get(13).slide(newY);
            repaint();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {}
    
    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public void mouseMoved(MouseEvent me) {}
 }
