package orthopaths;  

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;

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
public class VisualCheck extends JPanel
{
  /** Enable printing debugging info. */
  public static boolean debug = false;//true;

  /** Minimum x coordinate of all points. */ protected float minX = -1;
  /** Maximum x coordinate of all points. */ protected float maxX = 7;
  /** Minimum y coordinate of all points. */ protected float minY = -1;
  /** Maximim y coordinate of all points. */ protected float maxY = 4;

  /** Number of segments in the drawn curve, the points are num+1. */
  int num; 

  /** Maximum perturbation in absolute value. */
  float maxPerturbation = 3.0f;

  /** Last point of the drawn curve. The fisrt point is (0,0). */
  Vertex lastPoint = null;
      
  /** Point coordinates will be amplified this factor times. A segment
   * with length=1 will become a segment of length BIG_FACTOR pixels. */
  public static float BIG_FACTOR = 100.0f;
  
  /** Perturbations will be amplified this factor times. A perturbation
   * equal to 1 will be rendered as an offset of PERTURB. After this,
   * it will be amplified BIG_FACTOR times. */
  public static float PERTURB = 0.05f;

  /** Each point has a virtual square with center the point and edge 
   * equal to 2*OFFSET. Each passage (pair of entering andexiting segment)
   * will incide into two points located on the border of the virtual
   * square. After this everything will be amplified BIG_FACTOR times. */
  public static float OFFSET = 0.2f;

  /** If true, draw just the raw curve. If false, draw the raw curve and
   * the curve without intersections. */
  protected boolean onlyRaw = false; 
  
  /** Button to exit the program. */
  protected JButton exitB;

  /** Button to exit interactive mode and enter a path. */
  protected JRadioButton pathB;
  /** Button to return to interactive mode. */
  protected JRadioButton userB;
  
  /** Button to continue drawing in interactive mode. */
  protected JButton eastB, northB, westB, southB;  
  /** Button to continue drawing in case of a read path. */
  protected JButton nextB;
  /** Button to finish drawing in case of a read path. */
  protected JButton finishB;
  /** Button to undo last drawn move. */
  protected JButton undoB;
  /** Button to undo all moves. */
  protected JButton resetB;
  
  /** Button to zoom in. */  protected  JButton zoomInB;
  /** Button to zoom out. */ protected  JButton zoomOutB;

  /** Button to show only the raw curve, or to show the raw curve and
   * the curve drawn without intersections over it. */
  protected JButton showB;
  
  /** To show the path. */            protected JTextField pathT;
  /** To show the number of moves. */ protected JTextField numT;
  /** To show a self-intersection.*/  protected JTextField intersectT;

  /** To recompute the perturbations when they are too close to each other
   * and fall below one pixel. */
  protected JButton redoPerturb;
  
  /** Pointer to the scroll panel containing this object. */
  protected JScrollPane container;

  /** Top window of the program. */
  JFrame window;
  
  /** Object used to check the current path. */
  protected PathChecker checker;
  
  /** Current checked version of the path. */
  protected PathChecker.CheckedPath path;
  
  /** The current path as a string of directions. */
  protected String rawPath = "";  

  /** This is true if there was an input path read from file 
   * or introduced by the user. */
  protected boolean inputPath = false;
    
  /** The part of the path which has not been processed yet. 
   * This is only for the case in which the path has been read
   * or inserted by the user. */
  protected String restPath = "";
  
  /** If the string is a valid path, set the path to be processed and
   * return true. Otherwise return false.
   * @param p string of moves
   * @return true iff the input string represents a correct path */
  public boolean setPathToCheck(String p)
  {
    p = p.toUpperCase();
    if (!PathUtils.isGood(p))
    { 
      // something wrong in the path, but try to recover at
      // least a part of it
      int k = PathUtils.problemIndex;
      if (k<=0) return false;
      String p2 = p.substring(k);
      p = p.substring(0,k);
      int ok = JOptionPane.showConfirmDialog(window, 
             "Error at position "+k+": "+PathUtils.problem+"\n"
             +p+" [ "+p2+" ] only the first part will be set, agree?",
             "Confirm", JOptionPane.YES_NO_OPTION);
      if (ok!=JOptionPane.YES_OPTION)
          return false;
    }
    restPath = p;
    rawPath = "";
    if (checker!=null) {  checker.reset(); path = null;  }
    num = 0;
    inputPath = true;
    enableDisable();
    // hide buttons of interactive mode and show next button
/*    eastB.setVisible(false);
    westB.setVisible(false);
    northB.setVisible(false);
    southB.setVisible(false);
    nextB.setVisible(true);
    finishB.setVisible(true);*/
//    userB.setEnabled(true);
    return true;
  }
  
  /** Return window coordinate, in pixels, for given x.
   * @param x the real value of x coordinate
   * @return the pixel coordinate for the real x */
  public int mapX(float x) {  return (int)(BIG_FACTOR*(x-minX));  }

  /** Return window coordinate, in pixels, for given y.
   * @param y the real value of x coordinate
   * @return the pixel coordinate for the real y */
  public int mapY(float y) {  return (int)(BIG_FACTOR*(maxY-y));  }

  /** Resize the window based on current window limints (minX, maxX,
   * (min, maxY). To be called when a new segment is added to the curve, 
   * after recomputing the window limits. */
  public void resetSize()
  {
    Dimension size = new Dimension(mapX(maxX), mapY(minY));
    setPreferredSize(size);
    if (container!=null) container.validate();
//    System.out.println("Dimensione X "+size.getWidth());
//    System.out.println("Dimensione Y "+size.getHeight());
  }

  /** Check if the given string represents a direction.
   * @param s the string to be checked
   * @return true iff s contains just one letter and it is a direction */
  protected static boolean isDir(String s)
  {
    if (s.length()!=1) return false;
    char c = s.charAt(0);
    if ((c=='E') || (c=='e')) return true;
    if ((c=='N') || (c=='n')) return true;
    if ((c=='W') || (c=='w')) return true;
    if ((c=='S') || (c=='s')) return true;
    return false;
  }
  
  /** Return incremented x coordinate in direction dir.
   * @param x x coordinate
   * @param dir a direction
   * @return modified x after moving one unit in direction dir */
  protected static float incrX(float x, char dir)
  {
    if ((dir=='E') || (dir=='e')) return x+1.0f;
    if ((dir=='W') || (dir=='w')) return x-1.0f;
    return x;
  }
  
  /** Return incremented y coordinate in direction dir.
   * @param y y coordinate
   * @param dir a direction
   * @return modified y after moving one unit in direction dir */
  protected static float incrY(float y, char dir)
  {
    if ((dir=='N') || (dir=='n')) return y+1.0f;
    if ((dir=='S') || (dir=='s')) return y-1.0f;
    return y;
  }
    
  /** Apply perturbation to given x coordinate. The coordinate
   * gets modified by the perturbation only if direction dir is 
   * vertical. Otherswise it gets modified by the offset. 
   * @param x x coordinate
   * @param dir a direction
   * @param perturb perturbation to be applied
   * @return perturbed x coordinate */
  protected static float perturbX(float x, int dir, float perturb)
  {
    if (dir==0) return x+OFFSET; // east
    if (dir==2) return x-OFFSET; // west
    return x+PERTURB*perturb;
  }
  
  /** Apply perturbation to given y coordinate. The coordinate
   * gets modified by the perturbation only if direction dir is
   * horizontal. Otherswise it gets modified by the offset.
   * @param y y coordinate
   * @param dir a direction
   * @param perturb perturbation to be applied
   * @return perturbed y coordinate */ 
  protected static float perturbY(float y, int dir, float perturb)
  {
    if (dir==1) return y+OFFSET; // north
    if (dir==3) return y-OFFSET; // south
    return y+PERTURB*perturb;
  }
  
  /** Update window limits (minX, minY, maxX, maxY) based on
   * lastPoint, and return true if something changed. 
   * @return true iff something changed */
  protected boolean updateLimits()
  {
    int x = 0, y = 0;
    if (lastPoint!=null) { x = lastPoint.x; y = lastPoint.y; }
    if ((x-1>=minX)&&(x+1<=maxX)&&
        (y-1>=minY)&&(y+1<=maxY)) return false;
    if (x+1>maxX) maxX = x+1; 
    else if (x-1<minX) minX = x-1;
    if (y+1>maxY) maxY = y+1;
    else if (y-1<minY) minY = y-1;
    return true;
  }

  /** Draw the curve as it is: a curve connecting all the points,
   * without perturbations.
   * @param gr graphical context 
   * @param x x of the starting point
   * @param y y of the starting point */
  public void drawRawCurve(Graphics2D gr, int x, int y)
  {
     Vertex v1;
     int lastXi, lastYi;
     Vertex v2 = new Vertex(x,y);
     int xi = mapX(x), yi = mapY(y);
     gr.fillRect(xi, yi, 2,2);
     gr.setStroke(new BasicStroke(2.0f));
     for (int i=0; i<rawPath.length(); i++)
     {
       v1 = v2;
       lastXi = xi;
       lastYi = yi;
       int d = Direction.decodDir(rawPath.charAt(i));
       v2 = Direction.movePoint(v1, d);
       xi = mapX(v2.x);
       yi = mapY(v2.y);
       gr.drawLine(lastXi, lastYi, xi, yi);  
       gr.fillRect(xi, yi, 2,2);   
     }
  }
  
  /** Draw the curve with perturbations, which avoid intersections.
  * @param gr graphical context 
   * @param x x of the starting point
   * @param y y of the starting point */
  public void drawCurve(Graphics2D gr, int x, int y)
  {
     Vertex v1;
     int lastXi, lastYi;
     Vertex v2 = new Vertex(x,y);
     int xi = mapX(x), yi = mapY(y);
//     System.out.println("drawCurve rettangolo iniz centrato in "+xi+", "+yi);
//     gr.fillRect(xi, yi, 2,2);
     float p = 0.0f;
     gr.setStroke(new BasicStroke(2.0f));
     for (int i=0; i<path.length(); i++)
     {
       v1 = v2;
       lastXi = xi;
       lastYi = yi;
       int d = path.getDir(i);
       v2 = Direction.movePoint(v1, d);
       p =  path.getPerturbation(i);
//System.out.println("Disegno seg ("+v1.x+","+v1.y+")-("+v2.x+","+v2.y+") con pert= "+p);
       // with previous point and new direction
//       if (debug) System.out.println("   Ero a ("+v1.x+" "+v1.y+") vecchio");
       xi = mapX(perturbX(v1.x,d,p));
       yi = mapY(perturbY(v1.y,d,p));
//       System.out.println("drawCurve p1: "+perturbX(v1.x,d,p)+","+
//          perturbY(v1.y,d,p)+"--> "+xi+", "+yi);
       gr.drawLine(lastXi, lastYi, xi, yi);
//       gr.fillRect(xi, yi, 2,2);
//       if (debug) System.out.println("Direzione "+d+": vado a ("+v2.x+" "+v2.y+")");
//       if (debug) System.out.println("Direzione opposta "+opposite(d));
       // with new point and opposite of new direction
       lastXi = xi;
       lastYi = yi;
       xi = mapX(perturbX(v2.x,Direction.oppositeDir(d),p));
       yi = mapY(perturbY(v2.y,Direction.oppositeDir(d),p));
//       System.out.println("drawCurve p2: "+perturbX(v2.x,opposite(d),p)+","+
//           perturbY(v2.y,opposite(d),p)+"--> "+xi+", "+yi);
       gr.drawLine(lastXi, lastYi, xi, yi);
      }
     // for next time, update maxPerturbation and recompute OFFEST
     if (p<0.0f) p = -p;
     if (p>maxPerturbation)
     { 
       maxPerturbation = p;
       PERTURB = OFFSET / (maxPerturbation+1.0f);
     }
  }

  /** Draw the unit grid.
   * @param gr graphical context */
  public void drawGrid(Graphics2D gr)
  {
    float x, y;
    int lim1, lim2, i;
    lim1 = mapY(minY); lim2 = mapY(maxY);
    for (x=minX; x<=maxX; x++)
    {
      i = mapX(x);
      gr.drawLine(i, lim1, i, lim2);
    }
    gr.setStroke(new BasicStroke(1.0f));
    gr.drawLine(mapX(0.0f), lim1, mapX(0.0f), lim2);
    gr.setStroke(new BasicStroke(0.5f));
    lim1 = mapX(minX); lim2 = mapX(maxX);
    for (y=minY; y<=maxY; y++)
    {
      i = mapY(y);
      gr.drawLine(lim1, i, lim2, i); 
    }
    gr.setStroke(new BasicStroke(3.0f));
    gr.drawLine(lim1, mapY(0.0f), lim2, mapY(0.0f));
    gr.setStroke(new BasicStroke(1.0f));
  }
      
  /** Draw the panel.
   * @param gr graphical context */
  public void paintComponent(Graphics gr)
  {
     super.paintComponent(gr);
     Graphics2D gr2 = (Graphics2D)gr;
     if (debug)
     {  System.out.println("OFFEST="+OFFSET);
        System.out.println("maxPerturbation="+maxPerturbation);
        System.out.println("PERTURB="+PERTURB);
        System.out.println("BIG_FACTOR="+BIG_FACTOR);
     }
     gr2.setStroke(new BasicStroke(1.0f));
//     System.out.println("num="+num+" rawPath=["+rawPath+"]");
     if (debug&&(path!=null))System.out.println("path has "+path.length()+" segments");
     // Draw the first raw point (0,0)
     gr.setColor(Color.green);
     int xi = mapX(0);
     int yi = mapY(0);
//     System.out.println("Disegno rettangolo verde centrato in "+xi+", "+yi);
     gr.fillRect(xi-4, yi-4, 8,8);
     // Draw the grid
     gr.setColor(new Color(200,200,200));
     drawGrid(gr2);
     if ((rawPath!=null)&&(rawPath.length()>0))
     {  // ------------- Draw the raw curve in yellow
        if (debug) System.out.println("Curve:  "+rawPath.length()+" segments.");
        if (onlyRaw) gr.setColor(Color.black);
        else gr.setColor(Color.yellow);
        drawRawCurve(gr2, 0,0);
     }
     if ((!onlyRaw) && (path!=null)&&(path.length()>0))
     {  // ------------- Draw the curve 
        if (debug) System.out.println("Curve: "+path.length()+" segments.");
        gr.setColor(Color.black);
        drawCurve(gr2, 0,0);
     }
  }
  
  /** Enable or disable buttons according to the current status
   * of the drawing process. */
  protected void enableDisable()
  {
    numT.setText("Drawn moves: "+num);
    if (restPath.isEmpty())
         pathT.setText("Path: "+rawPath);
    else
         pathT.setText("Path: "+rawPath+ "  [  " +restPath+ "  ]");
    undoB.setEnabled(num>0);
    resetB.setEnabled(num>0);
    if (restPath.isEmpty())
    {
      enableDisableENWS(); 
      nextB.setEnabled(false);
      finishB.setEnabled(false);
    }
    else 
    {
      disableENWS(); 
      nextB.setEnabled(true);
      finishB.setEnabled(true);
    }
    intersectT.setVisible((num>0)&&path.hasIntersection());
//    userB.setEnabled(inputPath);
  }
  
  protected void disableENWS()
  {
//System.out.println("Disabilito punti cardinali");
    eastB.setEnabled(false);
    westB.setEnabled(false);
    northB.setEnabled(false);
    southB.setEnabled(false);
  }
  
  protected void enableDisableENWS()
  {
//System.out.println("enableDisableENWS");
    if (!userB.isSelected())
    {
       disableENWS();
       return;
    }
    // here userB is enabled
//System.out.println("Abilito almeno qualche punto cardinale");
//System.out.println("num="+num);
    if (num>0)
    {
      char lastMove = rawPath.charAt(num-1);
      boolean moving = !path.hasIntersection() && (lastPoint.x!=0 || lastPoint.y!=0);

//System.out.println("lastPoint.x="+lastPoint.x+" lastPoint.y="+lastPoint.y);
//System.out.println("moving="+moving);
      eastB.setEnabled(moving && (lastMove!=Direction.WEST));
      westB.setEnabled(moving && (lastMove!=Direction.EAST));
      northB.setEnabled(moving && (lastMove!=Direction.SOUTH));
      southB.setEnabled(moving && (lastMove!=Direction.NORTH));
    }
    else // num==0, lastPoint=(0,0)
    {
      eastB.setEnabled(true);
      westB.setEnabled(true);
      northB.setEnabled(true);
      southB.setEnabled(true);
    }
  }
  
  /** Auxiliary function to create a panel containing 
   * a label with the given string, and the given text field.
   * @param s string for the label
   * @param t text field
   * @return panel containing the label on the left and the
   * text field on the right */
  protected JComponent createPair(String s, JTextField t)
  {
    JPanel aux = new JPanel(new BorderLayout());
    aux.add(new JLabel(s), BorderLayout.WEST);
    aux.add(t, BorderLayout.CENTER);
    return aux;
  }

  /** Create a window containing this object and all the control
   * elements for user interaction.
   * @return window containing this panel and all the necessary 
   * devices to manage user interaction */
  public JFrame createWindow()
  {
    JPanel movesP, controlP, pathP, actionP, zoomP;
    JPanel aux1, aux2;
    
    window = new JFrame();
    window.getContentPane().setLayout(new BorderLayout());
    container = new JScrollPane(this);
    container.setPreferredSize(new Dimension(800,500));
    resetSize();
    window.getContentPane().add(BorderLayout.CENTER, container);

    // set up buttons for the moves
    movesP = new JPanel(new GridLayout(3,3));
    eastB = new JButton("EAST");
    westB = new JButton("WEST");
    northB = new JButton("NORTH");
    southB = new JButton("SOUTH");
    movesP.add(new JLabel()); 
    movesP.add(northB);
    movesP.add(new JLabel()); 
    movesP.add(westB);
    movesP.add(new JLabel()); 
    movesP.add(eastB);
    movesP.add(new JLabel());
    movesP.add(southB);
    movesP.add(new JLabel());
    
    // set up buttons for entering the path
    controlP = new JPanel(new GridLayout(4,1));
    controlP.add(new JLabel("Current mode:"));
    controlP.add(pathB = new JRadioButton("type in an entire path"));
    controlP.add(userB = new JRadioButton("enter the next move"));
    userB.setSelected(true);
    ButtonGroup gr = new ButtonGroup();
    gr.add(pathB);
    gr.add(userB);
    controlP.add(showB = new JButton("Hide drawn curve"));

    // set up buttons for processing the entered path
    actionP = new JPanel(new GridLayout(4,1));
    actionP.add(nextB = new JButton("next move"));
    actionP.add(finishB = new JButton("all moves"));
    actionP.add(undoB = new JButton("undo last move"));
    actionP.add(resetB = new JButton("undo all moves"));
        
    // set up zoom buttons
    zoomP = new JPanel(new GridLayout(2,1));
    zoomP.add(zoomInB = new JButton("Zoom +"));
    zoomP.add(zoomOutB = new JButton("Zoom -"));

    // set up the part about current path
    pathP = new JPanel(new GridLayout(3,1));
    pathP.add(numT = new JTextField("Moves: "+num));
    pathP.add(pathT = new JTextField("Path: "+rawPath));
    pathP.add(intersectT = new JTextField("-- INTERSECTION --"));
    
    // put all subpanels together
    aux1 = new JPanel(new FlowLayout());
    aux1.add(exitB = new JButton("exit"));
    aux1.add(controlP);
    aux1.add(movesP);
    aux1.add(actionP);
    aux1.add(zoomP);
    aux1.add(redoPerturb = new JButton("Redraw"));
    
//    aux1.add(showB = new JButton("Hide drawn curve"));
    aux2 = new JPanel(new BorderLayout());
    aux2.add(aux1, BorderLayout.CENTER);
    aux2.add(pathP, BorderLayout.SOUTH);
    window.getContentPane().add(BorderLayout.SOUTH, aux2);

/*    nextB.setVisible(false);
    finishB.setVisible(false);  // because interactive mode */
//    userB.setEnabled(false); // because interactive mode
//    undoB.setEnabled(false);//************
    intersectT.setVisible(false);
    intersectT.setEditable(false);
    intersectT.setBackground(Color.red);
    pathT.setEditable(false);
    numT.setEditable(false);
    enableDisable();

    exitB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {   System.exit(0);   }
    });      

    pathB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          if (!pathB.isSelected()) return;
          int ok;
          if (num==0) ok = JOptionPane.YES_OPTION;
          else
             ok = JOptionPane.showConfirmDialog(window, 
             "The path will replace the current one, are you sure?",
             "Confirm", JOptionPane.YES_NO_OPTION);
          if (ok==JOptionPane.YES_OPTION)
          {
            String p = JOptionPane.showInputDialog(window, "Input path");
            if (!setPathToCheck(p))
            {
              JOptionPane.showMessageDialog(window, 
                    "String "+p+ " is not a valid path\n"+PathUtils.problem,
                    "Path not set", JOptionPane.ERROR_MESSAGE);
              ok = JOptionPane.NO_OPTION;
            }
          }
          if (ok==JOptionPane.YES_OPTION)
          {
            pathT.setText("Path: "+rawPath+ "  [  " +restPath+ "  ]");
            repaint();
          } 
          else 
          {
if(debug){
System.out.println("pathB non ha eseguito");
System.out.println("  restPath="+restPath);
System.out.println("  inputPath="+inputPath);
}
            userB.setSelected(true);
            pathB.setSelected(false);
/*if (restPath.isEmpty())System.out.println("seleziono pathB");
else System.out.println("seleziono userB");
            pathB.setSelected(restPath.isEmpty());
            userB.setSelected(!restPath.isEmpty());*/
          }
          enableDisable();
        }
    });

    userB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          if (!userB.isSelected()) return;
          int ok;
          if (restPath.isEmpty()) ok = JOptionPane.YES_OPTION;
          else 
             ok = JOptionPane.showConfirmDialog(window, 
             "The rest of the path will be lost, are you sure?",
             "Confirm", JOptionPane.YES_NO_OPTION);
          if (ok==JOptionPane.YES_OPTION)
          {
            restPath = "";
            inputPath = false;
            pathT.setText("Path: "+rawPath);
          }
          else 
          {
if(debug){
System.out.println("userB non ha eseguito");
System.out.println("  restPath="+restPath);
System.out.println("  inputPath="+inputPath);
}
            userB.setSelected(false);
            pathB.setSelected(true);
/*if (restPath.isEmpty())System.out.println("seleziono pathB");
else System.out.println("seleziono userB");
            pathB.setSelected(restPath.isEmpty());
            userB.setSelected(!restPath.isEmpty());*/
          }
          enableDisable();
        }
    });
    
    eastB.addActionListener(continueL);
    westB.addActionListener(continueL);
    southB.addActionListener(continueL);
    northB.addActionListener(continueL);

    nextB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          char m = restPath.charAt(0);
          rawPath = rawPath + m;
          restPath = restPath.substring(1);
          performMove(Direction.decodDir(m));
          repaint();
        }
    });

    finishB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          char m;
          while (restPath.length()>0)
          {
            m = restPath.charAt(0);
            rawPath = rawPath + m;
            restPath = restPath.substring(1);
            performMove(Direction.decodDir(m));
          }
          repaint();
        }
    });  

    undoB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          if (checker.undoTest())
          {
            num--;
            lastPoint = checker.getLastPoint();
            if (!inputPath) // just remove the last move
              rawPath = rawPath.substring(0,num);
            else
            { // put back into restPath the last move of rawPath 
              char m = rawPath.charAt(num);
              rawPath = rawPath.substring(0,num);
              restPath = ""+m+restPath;
            }
//System.out.println("Undo, ora rawPath="+rawPath+" e restPath="+restPath);
            // not necessary path = checker.getCheckedPath();
            // because checker did not update the path after the intersection
            enableDisable();
            repaint();
          }
        }
    });

    resetB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          if (checker!=null) {  checker.reset(); path = null;  }
          num = 0;
          if (!inputPath)
            restPath = rawPath = "";
          else
          {
            restPath = rawPath + restPath;
            rawPath = "";
          }
          lastPoint = null;
          enableDisable();
          repaint();
        }
    });

    zoomInB.addActionListener(zoomL);
    zoomOutB.addActionListener(zoomL);
    
    showB.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
          onlyRaw = !onlyRaw;
          if (onlyRaw)
            showB.setText("Show drawn curve");
          else
            showB.setText("Hide drawn curve");
          repaint();
        }
    });

    redoPerturb.addActionListener(new ActionListener()
    {   public void actionPerformed(ActionEvent ev)
        {
           checker.global_dict.adjustPerturbations();
           repaint();
        }
    });
    
    window.pack();
    return window;
  }


  /** Action listener for buttons to continue drawing the curve
   * in one of the four directions. */
  protected ActionListener continueL = new ActionListener()
  {
    public void actionPerformed(ActionEvent e)
    {
      if (debug) System.out.println("SCATTA continueL"); 
      JButton b = (JButton)e.getSource();
      int d = 0;
      if (b==eastB) d = Direction.E;
      else if (b==westB) d = Direction.W;
      else if (b==northB) d = Direction.N;
      else if (b==southB) d = Direction.S;
      // no other possibility
      rawPath = rawPath + Direction.letterDir[d];
      performMove(d);
      repaint();
    }
  };    

  /** Extend the path by drawing a new segment in direction d.
   * @param dir direction of the next move */
  protected void performMove(int dir)
  {
      if (checker==null) checker = new PathChecker();
      if (num==0)  checker.startTest(0,0, dir);
      else  checker.continueTest(dir);
      path = checker.getCheckedPath();
      num++;
      lastPoint = checker.getLastPoint();
      if (debug) System.out.println("lastPoint="+lastPoint);
      if (updateLimits()) resetSize();
      enableDisable();
  }

  /** Action listener for zoom buttons. */
  protected ActionListener zoomL = new ActionListener()
  {
    public void actionPerformed(ActionEvent e)
    {
      if (debug) System.out.println("SCATTA zoomL"); 
      JButton b = (JButton)e.getSource();
      if (b==zoomInB) BIG_FACTOR += 10;
      else if (b==zoomOutB) BIG_FACTOR -= 10;
      // no other possibility
      zoomOutB.setEnabled(BIG_FACTOR>10);
      resetSize();
      repaint();
    }
  };
    
  /** Main: create the window and show it.
   * If a file name is given on the command line, read the input path
   * from file, otherwise consider an empty input path.
   * @param arg command line arguments */
  public static void main(String[] arg)// throws java.io.FileNotFoundException
  {
    VisualCheck vc = new VisualCheck();
    JFrame fr = vc.createWindow();
    if (arg.length>0)
    {
      try
      {
        System.err.println("Read path from file "+arg[0]);
        String path = PathUtils.pathFromFile(arg[0]);
        System.err.println("Read path="+path);
        vc.setPathToCheck(path);
      }
      catch(IOException exc)
      {
        System.err.println("Cannot read path from given file "+arg[0]);
        System.err.println(exc.getMessage());
        System.err.println("The program will start with an empty path");
        JOptionPane.showMessageDialog(null,
          "Cannot read path from given file "+arg[0]+"\n"+exc.getMessage()+
          "\n"+"The program will start with an empty path", 
          "READ ERROR", JOptionPane.ERROR_MESSAGE);
      }
    }    
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fr.setVisible(true);
  }

}
