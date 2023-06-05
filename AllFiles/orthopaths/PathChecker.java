package orthopaths;

/**
 * Class for checking whether a path is self-crossing. 
 * The path is given as a list of moves in the four 
 * directions (north, south, east, west) represented as a string of characters.
 * While checking, a perturbation is associated with each move.
 * The perturbation represents a small amount the segment must be shifted in
 * order to draw the path without interections.
*/
public class PathChecker
{
  /** If true, enable printing debug information. */
  public static boolean speaking = false;//true;
  /** If true, enable printing details of debug information. */
  public static boolean details = false;//true;

  /** Value defining the size of the array for containing the checked path. */
  public static int DEFAULT_CAPACITY = 100;

  /** The starting point for drawing the input path. */
  protected Vertex startPoint = null;

  /** Dictionary in which every traversed point (x,y) is a key.
   * The associated value is a radially sorted list of half segments. */
  protected VertexSet global_dict = new VertexSet();
  
  /** Last created half segment while scanning the input path. */
  protected HalfSegment lastSeg = null;
  
  /** Inner class for the checked path. It contains two aligned arrays:
   * one with the directions of each move, and one with the associated
   * perturbations. */
  public class CheckedPath
  {
    /** Array of half segments generated from the moves. */
    protected HalfSegment[] allMoves = null;

    /** The filled part of the arrays is from index 0 to currInd-1. */
    protected int currInd;

    /** True iff an crossing has been detected. */
    protected boolean intersecting;
    
    /** Create a checked path given the dimension of the arrays.
     * @param capacity number of positions to allocate */
    public CheckedPath(int capacity)
    {
      allMoves = new HalfSegment[capacity];
      intersecting = false;
      currInd = 0;
    }

    /** Add a new move with given direction and perturbation.
     * @param dir direction
     * @param pert perturbation */
    public void append(HalfSegment seg)
    {
       if (currInd==allMoves.length-1)
       {
         if (speaking) System.out.println("Checked path is full, resize it");
         int capacity = allMoves.length;
         HalfSegment[] newMoves = new HalfSegment[2*capacity];
         for (int i=0; i<capacity; i++)
         {  newMoves[i] = allMoves[i];
         }
         allMoves = newMoves;
       }
       allMoves[currInd] = seg;
       currInd++;
       if (speaking)
       {  System.out.println("checked path:");
          for (int j=0; j<currInd; j++)
             System.out.println("["+Direction.letterDir[allMoves[j].dir]+","+allMoves[j].perturb+"]");
          System.out.println("tot "+currInd+" moves.");       
       }
    }
    
    /** Return true iff the checked path is self-crossing.
     * @return true iff an intersection has been detected */
    public boolean hasIntersection()
    {  return intersecting;  }
    
    /** Return the length of the path checked so far.
     * @return number of already checked moves */
    public int length() {  return currInd;  }
    
    /** Return the character representing the direction of the i-th
     * move of the checked path.
     * @param i index of a move within the path
     * @return character representing the direction of the i-th move */
    public char getDirName(int i)  
    {  return Direction.letterDir[allMoves[i].dir];  }
    
    /** Return the direction of the i-th move of the checked path.
     * @param i index of a move within the path
     * @return direction of the i-th move */
    public int getDir(int i)  
    {  return allMoves[i].dir;  }

    /** Return the perturbation associated with the i-th move of
     * the checked path.
     * @param i index of a move within the path
     * @return perturbation of the i-th move */
    public float getPerturbation(int i)  
    {  return allMoves[i].perturb;  }
  }
  
  /** The checked version of the path: moves and associated perturbations. */
  protected CheckedPath output = null;

  /** Create a new path checker. */
  public PathChecker()
  {
    global_dict = new VertexSet();
    HalfSegment.COUNT = 0;
    lastSeg = null;
    output = null;
  }

  /** Store the last segment, in order to continue checking the path.
   * @param s the new last half segment */
  protected void storeLastSegment(HalfSegment s)
  {
    lastSeg = s;
    if (speaking) System.out.println("==MATE  "+s.mate);
    if (speaking) System.out.println("==NEXT  "+s);
    if (s.perturb==HalfSegment.UNDEF)
       System.out.println("ERR: segment "+s.name+" has undefined perturbation");
    if (s.mate.perturb==HalfSegment.UNDEF)
       System.out.println("ERR: segment "+s.mate.name+" has undefined perturbation");
    output.append(s);
  }

  /** Start the process of path cheking. The pair (x,y) defines the starting 
   * point, and dir is the direction of the first move of the path.
   * @param x x of the starting point
   * @param y y of the starting point
   * @param dir direction of the first move */
  public void startTest(int x, int y, int dir)
  {   startTest(x,y, dir, false);  }

  /** Reset the path checker. */
  public void reset()
  { // clear previous dictionary and segment counter
    global_dict = new VertexSet();
    HalfSegment.COUNT = 0;
  }  
  
  /** Create the initial half segment of the curve, starting from point (x,y)
   * in direction dir. Add the created half segment to global dictionary 
   * and store the half segment. If clear is true, delete all previously
   * created half segments.
   * @param x x of the starting point
   * @param y y of the starting point
   * @param dir direction of the first move
   * @param clear if true, delete all existing half segments */
  public void startTest(int x, int y, int dir, boolean clear)
  {
    // Initialization:
    if (clear) reset();
    startPoint = new Vertex(x,y);
    output = new CheckedPath(DEFAULT_CAPACITY);
    if (speaking) System.out.println("Start_curve parte dal punto "+startPoint);
    // Passage through first point:
    HalfSegment first = new HalfSegment(startPoint, dir);
    first.updatePerturb();
    // For the first half segment, we set a fake mate half segment oriented 
    // in direction WEST from the starting point. 
    // By convention the starting point must be leftmost,
    // so the first segment cannot be in direction WEST. 
    HalfSegment fake = first.makeMate(2);
    fake.updatePerturb();// VED SE IN PYTHON C'E'
    HalfSegmentSkipList new_list = global_dict.conditionalInsert(x,y);
    new_list.addFirstPair(first,fake);
    storeLastSegment(first);
  }
  
  /** Variable lastSeg contains the last created half segment, and
   * the next move is in direction dir.
   * Create the opposite half segment of lastSeg and the next half segment.
   * Add them to global dictionary. Store the next half segment.
   * Return false if it is not possible to continue without crossing, 
   * otherwise return true.
   * @param dir direction of the next move
   * @return true iff it is possible to perform the next move without
   * self-crossing */
  public boolean continueTest(int dir)
  {
    Vertex point = Direction.movePoint(lastSeg.start, lastSeg.dir);
    if (speaking) System.out.println("Continue_curve passa nel punto "+point);
    HalfSegment oppos = lastSeg.makeOpposite(point);
    if (speaking&&details) System.out.println("--OPPOSTO(entrante) "+oppos);
    //if ( oppos.perturb==HalfSegment.UNDEF)
    //   System.out.println("ERR opposite segment has undefined perturbation");    
    HalfSegment next = oppos.makeMate(dir);
    if (speaking&&details) System.out.println("--PROSS(uscente) "+next);
    HalfSegmentSkipList radial_list = global_dict.conditionalInsert(point);
    if (radial_list.size()==0)
    {
      // this is the first passage at the point
      oppos.updatePerturb();
      next.updatePerturb();
      radial_list.addFirstPair(oppos,next);
      if (speaking)
        System.out.println("Primo passaggio, nel punto ci sono:\n"+
            radial_list.toString());
      storeLastSegment(next);
      return true;
    }
    // there are already half segments around point
    if (speaking)
       System.out.println("Ulteriore passaggio, nel punto ci sono:\n"+
            radial_list.toString());
    HalfSegmentSkipList.Node nodei = radial_list.addEntering(oppos);
    if (nodei==null)
    {
      // An interction has been detected:
      if (speaking) System.out.println("ERR Non inserito entrante");
      output.intersecting = true; //markIntersect();
      return false;
    }
    // No intersection:
    if (speaking) System.out.println("Inserito entrante. Nel punto ci sono:\n"+
       radial_list.toString());
    HalfSegmentSkipList.Node nodej = radial_list.addExiting(next, nodei);
    if (nodej==null)
    {
      // An interction has been detected:
      if (speaking) System.out.println("Non inserito uscente");
      output.intersecting = true; //markIntersect();
      return false;
    }
    if (speaking) System.out.println("Inserito uscente. "+
        "Nel punto ci sono:\n"+radial_list.toString());
    storeLastSegment(next);
    return true;
  }

  /** Undo the last testing step and restore the value of all
   * variables. Return false if there is no step to undo. Return 
   * true otherwise.
   * @return true iff there was something to undo */
  public boolean undoTest()
  {
    if (lastSeg == null) return false;
    // If the path is not self-crossing, remove lastSeg and lastSeg.mate,
    // remove the last direction and perturbation,
    // the last segment will be lastSeg.mate.oppos.
    // If the path is self-crossing, remove lastSeg.opposite, and
    // make the path non-self-crossing (new chance).
    if (output.intersecting)
    {
      HalfSegment s = lastSeg.opposite;
      Vertex point = s.start;
      HalfSegmentSkipList radial_list = global_dict.conditionalInsert(point);
      radial_list.searchRemove(s);
      if (speaking) 
         System.out.println("Eliminato "+s.name+" ora in "+point+" ci sono "+radial_list);  
      output.intersecting = false;
    }
    else
    { 
      Vertex point = lastSeg.start;
      HalfSegmentSkipList radial_list = global_dict.conditionalInsert(point); 
      radial_list.searchRemove(lastSeg);
      if (speaking)
         System.out.println("Eliminati "+lastSeg.name+ " e "+lastSeg.mate.name
               +" ora in "+point+ " ci sono "+radial_list);
      lastSeg = lastSeg.mate.opposite;
      output.currInd--;
    }
    return true;
  }

  /** Return the checked path (with perturbations).
   * @return the checked version of the path */
  public CheckedPath getCheckedPath()
  {  return output;  }
  
  /** Return the last point of the checked path.
   * @return the last vertex of the checked path */
  public Vertex getLastPoint()
  {
    if (lastSeg==null) return startPoint; 
    if (speaking) System.out.println("getLastPoint: last seg="+lastSeg);
    return Direction.movePoint(lastSeg.start, lastSeg.dir);
  }

  /** Return true iff the checked path is self-crossing.
   * @return true iff the path was discovered to be self-crossing */
  public boolean getAnswer()
  {  return output.hasIntersection();  }

  /** Path is a string of directions, (x,y) is the starting point.
   * Scan the path and determine:<UL>
   * <LI> whether the path is self-crossing
   * <LI> a list of directions and perturbations to draw the path without
   * crossing.</UL>
   * If the path is self-crossing, the scan arrives only at
   * the first crossing point.
   * @param x x of the starting point
   * @param y y of the starting point
   * @param path string of moves representing the path
   * @param clear if true, any previous processed half segment is removed
   * @return the ckecked version of the path (with perturbations) */
  public CheckedPath test(int x, int y, String path, boolean clear)
  {
    int dir = Direction.decodDir(path.charAt(0)); 
    startTest(x,y, dir, clear);
    if (speaking) System.out.println("==FIRST "+lastSeg);
  
    for (int i=1; i<path.length(); i++)
    {
      dir = Direction.decodDir(path.charAt(i));
      if (continueTest(dir))
      {
        //System.out.println("==NEXT  "+lastSeg);
      }
      else break;
    }
    if (output.hasIntersection())
      System.out.println("//// crossing /////");
    else
      System.out.println("//// OK /////");
    return output;
  }
  
}
