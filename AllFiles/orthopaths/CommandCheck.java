package orthopaths;  

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;

/**
<P>Panel to interactively draw a curve without intersections.</P>
<P>Initially the curve is empty.
Buttons allow the user to continue the curve in one of the four 
cardinal directions: east / west are positive / negative x direction,
and north / south are positive / negative y direction.</P>
<P>If possible without intersections, the curve is extended.
Otherwise an intersection is detected. </P>
<P>
The curve cannot contain U-turn (i.e., it cannot go north and then
south, east and thn west, etc.).
The starting point is (0,0) and the curve cannot go left to this point,
or south to this point on line x=0.</P>
*/
public class CommandCheck extends VisualCheck
{
  String output_name = "output.png";
  //boolean redraw = false;
  int initX =0, initY = 0;

  public void createThePath()
  {
    for (int i=0; i<restPath.length(); i++)
    {
      char m = restPath.charAt(i);
      performMove(Direction.decodDir(m));
     }
     rawPath = restPath;
     restPath = ""; 
     //if (redraw) 
     //   checker.global_dict.adjustPerturbations();
     repaint();
  }

  public BufferedImage createTheImage()
  {
    
    //window.setSize(new Dimension(1200,1000));
     float dim1 = BIG_FACTOR*(maxX-minX+2);
     float dim2 = BIG_FACTOR*(maxY-minY+2);
     //System.out.println("Image limits "+minX+","+minY+" -- "+maxX+","+maxY);
     //System.out.println("Zoom "+BIG_FACTOR);
     //System.out.println("Image dimensions "+dim1+" x "+dim2);
     BufferedImage imm = (BufferedImage) createImage((int)dim1,(int)dim2);//getWidth(),getHeight());
     Graphics2D grimm = imm.createGraphics();
     grimm.setStroke(new BasicStroke(1.0f));
     // Draw the first raw point (0,0)
     grimm.setColor(Color.green);
     int xi = mapX(initX);
     int yi = mapY(initY);
//     System.out.println("Disegno rettangolo verde centrato in "+xi+", "+yi);
     grimm.fillRect(xi-4, yi-4, 8,8);
     // Draw the grid
     grimm.setColor(new Color(200,200,200));
     drawGrid(grimm);
     if ((rawPath!=null)&&(rawPath.length()>0))
     {  // ------------- Draw the raw curve in yellow
        grimm.setColor(Color.yellow);
        drawRawCurve(grimm, initX, initY);
     }
     if ((!onlyRaw) && (path!=null)&&(path.length()>0))
     {  // ------------- Draw the curve 
        grimm.setColor(Color.black);
        drawCurve(grimm, initX, initY);
     }
     try
     {
       ImageIO.write(imm,"png", new File(output_name));
       System.out.println("Image saved to file "+ output_name);
     }
     catch(Exception exc)
     {
       exc.printStackTrace();
     }
     return imm;
   }
      
  /** Main: create the window and show it.
   * After showing, it saves the image to a png file.
   * The input parameters are:<UL>
   * <LI> the path as a string of four letters NSWE</LI>
   * <LI> the output figure name, without extension</LI>
   * <LI> the coordinates of the starting point for drawing
   * <LI> (optional) -R to mean "reconfigure" the path after drawing
   * </UL>
   * @param arg command line arguments */
  public static void main(String[] arg)
  {
    CommandCheck vc = new CommandCheck();
    JFrame fr = vc.createWindow();
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fr.setVisible(true);
    fr.setSize(new Dimension(800,800));
    Scanner sc = new Scanner(System.in);
    String st;
    int i;
    float f;
    st = sc.next();
    vc.setPathToCheck(st);
    vc.createThePath();
    st = sc.next();
    vc.output_name = st + ".png";
    i = sc.nextInt();
    vc.initX = i;
    i = sc.nextInt();
    vc.initY = i;
    while (sc.hasNext())
    {
      st = sc.next();
      if (st.equalsIgnoreCase("-R"))
      {
        vc.checker.global_dict.adjustPerturbations();
        vc.repaint();
      }
      else if (st.equalsIgnoreCase("-Z"))
      {
        f = sc.nextFloat();
        //System.out.println("Zoom attuale "+vc.BIG_FACTOR+" che moltiplico per "+f);
        vc.BIG_FACTOR *= f;
        vc.resetSize();
        //System.out.println("Nuovo zoom  "+vc.BIG_FACTOR);
        vc.repaint();
        /*if (f>1)
        {
           Dimension dim = vc.window.getSize();
           vc.window.setSize(new Dimension((int)(f*dim.getWidth()),(int)(f*dim.getHeight())));
        }*/
      }
      else
          System.err.println("Unknown option "+st);
    }
    vc.createTheImage();
    System.exit(1);
  }

}
