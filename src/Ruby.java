/**
 * @(#)Ruby.java
 *
 * Ruby application
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/15
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public final class Ruby extends JFrame implements KeyListener {

	private boolean KEY_LEFT;
	private boolean KEY_RIGHT;
	private boolean KEY_UP;
	private boolean KEY_DOWN;
	private boolean KEY_DELETE;
	private boolean KEY_PAGE_DOWN;
	private boolean KEY_W;
	private boolean KEY_A;
	private boolean KEY_S;
	private boolean KEY_D;
	private boolean KEY_B;
	private boolean KEY_L;
	private boolean KEY_O;
	private boolean KEY_SPACE;
	private Dimension resolution;
	//private int colorDepth;
	//private int refreshRate;

	public Dimension getResolution() {
		return resolution;
	}

    @Override
	public void keyPressed(KeyEvent ke) {
		switch (ke.getKeyCode()) {
	    case KeyEvent.VK_LEFT:
			KEY_LEFT=true;
	        break;
	    case KeyEvent.VK_RIGHT:
			KEY_RIGHT=true;
	    	break;
	    case KeyEvent.VK_UP:
			KEY_UP=true;
	        break;
	    case KeyEvent.VK_DOWN:
			KEY_DOWN=true;
	    	break;
	    case KeyEvent.VK_DELETE:
			KEY_DELETE=true;
	    	break;
	    case KeyEvent.VK_PAGE_DOWN:
			KEY_PAGE_DOWN=true;
	    	break;
	    case KeyEvent.VK_W:
			KEY_W=true;
	        break;
	    case KeyEvent.VK_A:
			KEY_A=true;
	    	break;
	    case KeyEvent.VK_S:
			KEY_S=true;
	    	break;
	    case KeyEvent.VK_D:
			KEY_D=true;
	    	break;
	    case KeyEvent.VK_B:
			KEY_B=true;
	    	break;
	    case KeyEvent.VK_L:
			KEY_L=true;
	    	break;
	    case KeyEvent.VK_O:
			KEY_O=true;
	    	break;
	    case KeyEvent.VK_ESCAPE:
			//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow().dispose();
			//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
			System.exit(0);
	    	break;
	    case KeyEvent.VK_SPACE:
	    	KEY_SPACE=true;
	    	break;
	    }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
		switch (ke.getKeyCode()) {
	    case KeyEvent.VK_LEFT:
			KEY_LEFT=false;
	        break;
	    case KeyEvent.VK_RIGHT:
			KEY_RIGHT=false;
	    	break;
	    case KeyEvent.VK_UP:
			KEY_UP=false;
	        break;
	    case KeyEvent.VK_DOWN:
			KEY_DOWN=false;
	    	break;
	    case KeyEvent.VK_DELETE:
			KEY_DELETE=false;
	    	break;
	    case KeyEvent.VK_PAGE_DOWN:
			KEY_PAGE_DOWN=false;
	    	break;
	    case KeyEvent.VK_W:
			KEY_W=false;
	        break;
	    case KeyEvent.VK_A:
			KEY_A=false;
	    	break;
	    case KeyEvent.VK_S:
			KEY_S=false;
	    	break;
	    case KeyEvent.VK_D:
			KEY_D=false;
	    	break;
	    case KeyEvent.VK_B:
			KEY_B=false;
	    	break;
	    case KeyEvent.VK_L:
			KEY_L=false;
	    	break;
	    case KeyEvent.VK_O:
			KEY_O=false;
	    	break;
	    case KeyEvent.VK_SPACE:
	    	KEY_SPACE=false;
	    	break;
	    }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void hideCursor() {
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		setCursor(blankCursor);
    }

    Ruby(boolean fullscr) {
    	this(Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height,fullscr);
    	setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    @SuppressWarnings("LeakingThisInConstructor")
	Ruby(int width, int height, boolean fullscr) {
		super("Ruby 3D Engine!");
		setIconImage(Toolkit.getDefaultToolkit().getImage("../led-icons/ruby.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setLayout(new GridLayout(1,1));
		//addComponentListener(this);
//		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		resolution = new Dimension();
		resolution.width = width;
		resolution.height = height;
		//resolution.width = (int)Math.round(dim.width);
		//resolution.height = (int)Math.round(dim.height);

		Surface3D surface;
        surface = new Surface3D(resolution.width,resolution.height) {
      double angleVelocityX=0.0f;
      double angleVelocityY=0.0f;
      double angleVelocityZ=0.0f;
      double angleAcceleration=1.0f;
      double speedX=0;
      double speedY=0;
      double speedZ=0;
      boolean AA=false;
      boolean light=true;
      boolean outline=false;
      boolean B_Pressed=false;
      boolean L_Pressed=false;
      boolean O_Pressed=false;
      boolean spacePressed=false;
      TimeSync sync=new TimeSync();

      public void checkKeys() {
              if(KEY_LEFT) {
              angleVelocityY+=sync.perSec(1);
          }
              else
              if(KEY_RIGHT) {
              angleVelocityY-=sync.perSec(1);
          }

              if(KEY_UP) {
              angleVelocityX-=sync.perSec(1);
          }
              else
              if(KEY_DOWN) {
              angleVelocityX+=sync.perSec(1);
          }

              if(KEY_DELETE) {
              angleVelocityZ+=sync.perSec(1);
          }
              else
              if(KEY_PAGE_DOWN) {
              angleVelocityZ-=sync.perSec(1);
          }

              if(KEY_W){
                      speedZ=20;
              }
              else
              if(KEY_S) {
              speedZ=-20;
          }

              if(KEY_A){
                      angleVelocityY=sync.perSec(1); //speedX=sync.perSec(100);
              }
              else
              if(KEY_D) {
              angleVelocityY=-sync.perSec(1);
          } //speedX=-sync.perSec(100);

              if(KEY_B && !B_Pressed) {
                      if(AA) {
                      AA=false;
                  }
                      else {
                      AA=true;
                  }

                      B_Pressed=true;
              }
              else if(!KEY_B) {
                      B_Pressed=false;
              }

              if(KEY_L && !L_Pressed) {
                      if(light) {
                      light=false;
                  }
                      else {
                      light=true;
                  }

                      L_Pressed=true;
              }
              else if(!KEY_L) {
                      L_Pressed=false;
              }

              if(KEY_O && !O_Pressed) {
                      if(outline) {
                      outline=false;
                  }
                      else {
                      outline=true;
                  }

                      O_Pressed=true;
              }
              else if(!KEY_O) {
                      O_Pressed=false;
              }

              if(KEY_SPACE && !spacePressed) {
                      saveImage("Ruby.png");
                      spacePressed=true;
              }
              else if(!KEY_SPACE) {
                      spacePressed=false;
              }
      }

      Object3D ruby, sapphire, emerald, jewels, STLModel;
      Object3D cube, sphere, triForce, torus1, torus2, torus3, planet,
      cylinder, fuse, axis, arrowVector, bomb, leafs, feathers, chess, matrix;

            @Override
      public void initScene() {
              setOrthoSquareUnit(100); // Sets Y axis to -100 to 100 and X axis from -(w/h)*100 to (w/h)*100 to get square units.

              //ruby=createRuby(13, new Color(255,0,0,240), 0.3f); 
              //ruby.setPosition(0,0,100);
              //ruby.setColorContrast(0.7f);

//				sphere=createSphere(100,new Color(255,0,0),50);
//				sphere.setPosition(0,0,200);
//
              /*torus1=createTorus(30, new Color(255,200,0), 30, 20);
              torus1.setPosition(0,0,100);

              torus2=createTorus(15, new Color(255,200,0), 30, 8);
              torus2.setPosition(0,0,100);

              torus3=createTorus(10, new Color(255,200,0), 20, 2);
              torus3.setPosition(0,0,100);*/
              
              STLModel=loadSTL("stl/AlexCut.stl", 1, new Color(200,200,200)); //Chocolate color (131,75,45) bluish color (100,200,255)
              //STLModel.rotate(Math.PI/2,0,0);
              //STLModel.rotate(Math.PI/10,0,0);
              //STLModel.shiftPosition(0,-15,25);
              STLModel.setPosition(0,-15,50);
				
//				cylinder=createCylinder(300, Color.red, 20, 50);
//				cylinder.setPosition(0,0,130);
//				cylinder.setColorContrast(0.5f);
//				cylinder.shiftPosition(0,-25,0);

//				leafs = new Object3D();
//
//				for(int i=0; i < 50; i++) {
//					Object3D l=createLeaf(0.3f);
//					int r = (int)(Math.random()*127)+127;
//					int g = (int)(Math.random()*100)+155;
//					l.setObjectColor(new Color(r,g,39));
//					l.shiftPosition(Math.random()*100-50,Math.random()*100-50,Math.random()*100-50);
//					l.rotate(Math.random()*Math.PI*2,Math.random()*Math.PI*2,Math.random()*Math.PI*2);
//					leafs.addMesh(l);
//
//				}
//				leafs.setPosition(0,0,150);

//				feathers = new Object3D();
//
//				for(int i=0; i < 5; i++) {
//					Object3D l=createFeather(0.5f, new Color(255,0,0));
//					//l.setObjectColor(new Color(255,0,0));
//					l.rotate(Math.random()*Math.PI*2,Math.random()*Math.PI*2,Math.random()*Math.PI*2);
//					l.shiftPosition(Math.random()*100-50,Math.random()*100-50,Math.random()*100-50);
//					//l.shiftPosition(0,0,-50);
//
//					feathers.addMesh(l);
//
//				}
//
//				feathers.setPosition(0,0,150);
//				feathers.setColorContrast(1.0f);

//				jewels = new Object3D();
//
//				ArrayList<Color> colors = new ArrayList<Color>();
//				colors.add(Color.red);
//				colors.add(Color.green);
//				colors.add(Color.blue);
//				colors.add(Color.magenta);
//
//				for(int i=0; i < 5; i++) {
//					Random r = new Random();
//					int index=r.nextInt(colors.size());
//					Color c = colors.get(index);
//					int num=(index==0)?13:(index==1)?3:(index==2)?4:5;
//					Object3D l=createRuby(num, new Color(c.getRed(),c.getGreen(),c.getBlue(),180), 0.3f);
//					l.rotate(Math.random()*Math.PI*2,Math.random()*Math.PI*2,Math.random()*Math.PI*2);
//					l.shiftPosition(Math.random()*1800-900,-20,Math.random()*1800-900);
//					jewels.addMesh(l);
//				}
//
//				jewels.setColorContrast(1.0f);

              //feathers.setPosition(0,0,150);
              //feathers.rotateEgo(5*Math.PI/16,0,0);

              //cube=createCube(50,new Color(100,150,255));
              //cube.setPosition(0,0,200);
              //cube.setColorContrast(1.0f);

              //axis = createAxis(75,0.3f);
              //axis.setPosition(0,0,200);

              //arrowVector = createArrow(Color.red, 18, 0.2f);
              //arrowVector.setPosition(0,0,100);
              //arrowVector.setColorContrast(1.0f);

              //triForce=createTriForce(50,5,new Color(255,240,0));
              //triForce.setPosition(0,0,150);
              //triForce.shiftPosition(0,-10,0);

//				cylinder=createCylinder(1000, Color.white, 50, 100);
//				cylinder.shiftPosition(0,-50,0);
//				cylinder.setPosition(0,0,200);

              //chess=createChess(50,2000);
              //chess.setPosition(0,-50,0);
              //chess.setLight(false);
              //chess.setColorContrast(0.1f);

              //matrix=createRandomMatrix(50, 2000, Color.white);
              //matrix.setPosition(0,-50,0);
              //chess.setColorContrast(0.1f);


              setFocusable(true);
      }

            @Override
      public void drawScene() {
              sync.begin();
              clearScreen(new Color(25,30,100)); //(50,60,200)

              setLineThickness(3);
              
              if(AA) {
              setAntiAliasing(true);
          }
              else {
              setAntiAliasing(false);
          }

              if(light) {
              STLModel.setLight(true);
          }
              else {
              STLModel.setLight(false);
          }

              if(outline) {
              setOutline(true);
          }
              else {
              setOutline(false);
          }

              //triForce.drawObject();
              //triForce.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //speedZ=500*Math.cos(angleVelocityY);
              //chess.drawObject();
              //chess.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //chess.rotate((angleVelocityX),(angleVelocityY),(angleVelocityZ));
              //chess.shiftPosition(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));


              //matrix.rotate((angleVelocityX),(angleVelocityY),(angleVelocityZ));
              //matrix.shiftPosition(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));
              //matrix.drawObject();
              //triForce.shiftPosition(speedX,speedY,speedZ);
//
              //cylinder.drawObject();
              //cylinder.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //torus.shiftPosition(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));

              //ruby.rotate(sync.perSec(1),sync.perSec(2),sync.perSec(1));
              //ruby.drawObject();

//				torus.drawObject();
//				torus.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
//				torus.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));

              STLModel.drawObject();
              //torus1.rotate(sync.perSec(angleVelocityX),0,0);
              STLModel.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));


              //torus2.drawObject();
              //torus2.rotate(sync.perSec(0.5f),sync.perSec(2),sync.perSec(0.5f));
              //torus3.drawObject();
              //torus3.rotate(sync.perSec(0.5f),sync.perSec(0.5f),sync.perSec(3));

              //arrowVector.drawObject();
              //arrowVector.rotate(sync.perSec(1),sync.perSec(1),sync.perSec(1));

              //torus1.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));

              //planet.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //planet.drawObject();

              //cylinder.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //cylinder.drawObject();

              //cylinder.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));

//				feathers.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
//				feathers.drawObject();

              //axis.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //axis.drawObject();

              //fuse.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //fuse.drawObject();

//				angleVelocityX+=(Math.random()-0.5f)*0.1f;
//				angleVelocityY+=(Math.random()-0.5f)*0.1f;
//				angleVelocityZ+=(Math.random()-0.5f)*0.1f;
//
//				double angle=(Math.random()-0.5f);

//				for(int i=0; i < feathers.getMesh().size(); i++) {
//					feathers.getMesh().get(i).rotate(sync.perSec((Math.random()-0.5f)*angleVelocityX),sync.perSec((Math.random()-0.5f)*angleVelocityY),sync.perSec((Math.random()-0.5f)*angleVelocityZ));
//				}

              //torus.rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));
              //ruby.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));
              //torus.drawObject();

              //ruby.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));
              //torus.move(sync.perSec(speedX),sync.perSec(speedY),sync.perSec(speedZ));

              //rotate(sync.perSec(angleVelocityX),sync.perSec(angleVelocityY),sync.perSec(angleVelocityZ));

              speedZ=0;

              //setCamRotate(0,angleVelocityY,0);
              //angleVelocityX=0;
              //angleVelocityY=0;
              //angleVelocityZ=0;
              //angleVelocityY+=sync.perSec(0.5f);
              //angleVelocityX+=sync.perSec(1);

              if(angleVelocityX > 2*Math.PI) {
              angleVelocityX -= 2*Math.PI;
          }
              checkKeys();
              resize();
      }

            @Override
      public void drawSurface(Graphics2D g2) {
              /////////////// Raster graphics //////////////
              g2.setColor(Color.red);
              g2.drawString("FPS: "+sync.getFPS(),0,10);
              g2.drawString("Number of polygons on scene: "+getNumberOfPolygons(),0,30);
              g2.drawString("Number of models on scene: "+getNumberOfModels(),0,50);
              g2.drawString("ms: "+sync.getMS(),0,70);
              g2.drawString("Width: "+getWidth(),0,90);
              g2.drawString("Height: "+getHeight(),0,110);
              if(AA) {
              g2.drawString("Anti-aliasing [B]: ON",0,130);
          }
              else {
              g2.drawString("Anti-aliasing [B]: OFF",0,130);
          }
              if(outline) {
              g2.drawString("Outline [O]: ON",0,150);
          }
              else {
              g2.drawString("Outline [O]: OFF",0,150);
          }
              if(isAccelerated()) {
              g2.drawString("Hardware Acceleration: ON",0,170);
          }
              else {
              g2.drawString("Hardware Acceleration: OFF",0,170);
          }
              if(KEY_LEFT) {
              g2.drawString("Left key: PRESSED",0,190);
          }
              else {
              g2.drawString("Left key: RELEASED",0,190);
          }
              if(KEY_RIGHT) {
              g2.drawString("Right key: PRESSED",0,210);
          }
              else {
              g2.drawString("Right key: RELEASED",0,210);
          }
              if(KEY_UP) {
              g2.drawString("Up key: PRESSED",0,230);
          }
              else {
              g2.drawString("Up key: RELEASED",0,230);
          }
              if(KEY_DOWN) {
              g2.drawString("Down key: PRESSED",0,250);
          }
              else {
              g2.drawString("Down key: RELEASED",0,250);
          }
              g2.drawString("Press [ESC] to quit",0,270);
      }
};
		setFocusable(true);
		if(fullscr) {
			setUndecorated(true);
			setResizable(false);
			hideCursor();
			/*GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			device.setFullScreenWindow(this);
			DisplayMode currentDisplayMode = device.getDisplayMode();
			DisplayMode displayMode = new DisplayMode(width,height,currentDisplayMode.getBitDepth(),currentDisplayMode.getRefreshRate());

			if (displayMode != null && device.isDisplayChangeSupported()) {
				try {
					device.setDisplayMode(displayMode);
				}
				catch (IllegalArgumentException ex) {
				}
			}*/
		}
		addKeyListener(this);
		add(surface);
		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frame = getSize();
		int CenterX = Math.max(0, (screen.width  - frame.width) / 2);
		int CenterY = Math.max(0, (screen.height  - frame.height) / 2);
		if(fullscr) {
                    setLocation(0,0);
                }
		else {
                    setLocation(CenterX, CenterY);
                }
		setVisible(true);
		//
		//surface.requestFocusInWindow();
		//setFocusTraversalKeysEnabled(false);
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

    public static void main(String[] args) {
    	try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e){}

		int value=JOptionPane.showConfirmDialog(null, "Would You Like To Run In Fullscreen Mode?", "Start FullScreen?", JOptionPane.YES_NO_CANCEL_OPTION);
		boolean fullscreen;
		if(value == JOptionPane.YES_OPTION) {
                    fullscreen=true;
                }
		else {
                    fullscreen=false;
                }

		if(value==JOptionPane.CANCEL_OPTION) {
                    System.exit(0);
                }

    	Ruby ruby = new Ruby(fullscreen);
	}
}
