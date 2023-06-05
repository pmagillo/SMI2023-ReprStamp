package orthopaths;

/**
 * <P>A half segment has a starting point and a direction.
 * It contains pointers to its opposite half 
 * segment (having different starting point and opposite direction)
 * and to its mate half segment (having the same starting point and
 * different direction).</P>
 * <P>The starting point is represented as a tuple of two integer 
 * coordinates (x,y), see class Vertex.</P>
 * <P>The direction is represented by a number 0=EAST, 1=NORTH, 2=WEST, 
 * 3=SOUTH, see class Direction.</P>
 * <P>In addition, the half segment has a perturbation which must be 
 * applied to it (to x coordinate if direction is N or S, to y 
 * coordinate if direction is E or W). The perturbation is used to
 * define a relative order between two segments with the same direction
 * incident in the same starting point (see class HalfSegmentSkipList)
 * and to draw the curve without intersections.</P>
 */
public class HalfSegment
{
  /** If this is true, print debug information during the operations. */
  public static boolean speaking = false;
  
  /** Conventional constant meaning undefined perturbation. */
  protected static final float UNDEF = 99999;
  
  /** Counter of created segments. */
  protected static int COUNT = 0;
  
  /** Name of the segment (progressive number). */
  public final int name;
  /** One endpoint of the segment, starting point of the half segment. */
  protected final Vertex start;
  /** Direction as seen from starting point. */
  protected int dir;
  /** Other half of the same segment (in opposite direction). */
  protected HalfSegment opposite;
  /** Other half segment in the same vertex. */
  protected HalfSegment mate;
  /** Perturbation. The perturbation applies to the x coordinate
   * for a vertical segment and to the y coordinate for a horizontal
   * segment. */
  protected float perturb;

  /** Create a new half segment, given starting point and direction.
   * @param point he starting point
   * @param dir the direction, it must be in {0,1,2,3} */
  public HalfSegment(Vertex point, int dir)
  {
    COUNT ++;
    this.name = COUNT;
    this.start = point;
    this.dir = dir;
    this.opposite = null;
    this.mate = null;
    this.perturb = UNDEF;
  }
 
  /** Return a string representing this half segment.
   * @return a string representing this half segment */ 
  public String toString()
  {
    return "HalfSegment "+name+": " +start.toString()+
       " dir "+Direction.letterDir[dir]+", pert "+perturb;
  }

  /** Create the other half of the same segment, given the other endpoint.
   * @param point he starting point of the opposite half segment
   * @return a half segment starting at point and having direction
   * opposite to this one
   */
  public HalfSegment makeOpposite(Vertex point)
  {
    int dir1;
    if (this.dir>=2) dir1 = this.dir-2; else dir1 = this.dir+2;
    HalfSegment s = new HalfSegment(point, dir1);
    s.opposite = this;
    this.opposite = s;
    s.perturb = this.perturb;
    return s;
  }
  
  /** Create the mate half segment in the same vertex, given the direction.
   * @param dir the direction of the mate, it must be in {0,1,2,3} and
   * different from the direction of this half segment
   * @return the created mate */
  public HalfSegment makeMate(int dir)
  {
    HalfSegment s = new HalfSegment(this.start, dir);
    s.mate = this;
    this.mate = s;
    return s;
  }
  
  /** Given another half segment, with the same starting point
   * and same direction as this one, test if this half segment lies
   * before the other one in counterclockwise order, based on
   * perturbations.
   * @param other another half segment, which must have same starting point
   * and same direction as this one.
   * @return true iff this half segment is before the other one */
  public boolean equalBefore(HalfSegment other)
  {
    if (speaking)
      System.out.println("  equal_before, segments are:\n"+ this+"\n"+other);
    if (perturb==other.perturb)
      System.out.println("ERR equal perturbation="+perturb);
    if ((perturb ==UNDEF) || (other.perturb==UNDEF))
           return false;
    if ((dir==0) || (dir==3)) // EAST or SOUTH
       return other.perturb>perturb;
    else // NORTH or WEST
       return other.perturb<perturb;
  }
  
  /** Test if this half segment lies in the radial sector from seg1 to seg2
   * in counterclockwise order. Call the function with same name and
   * additional boolean parameter (setting it to false).
   * @param seg1 a half segment with same starting point as this one
   * @param seg2 a half segment with same starting point as this one
   * @return true iff this half segment lies after seg1 and before seg2 */
  public boolean isBetween(HalfSegment seg1, HalfSegment seg2)
  {  return isBetween(seg1, seg2, false);  }
  
  /** Test if this half segment lies in the radial sector from seg1 to seg2
   * in counterclockwise order. The three half segments must have the
   * same starting point. The result depends on directions and, if 
   * directions are equal, on pertubations as well. If change is true,
   * them the perturbation of this segment can be changed in order to make
   * it lie between seg1 and seg2.
   * @param seg1 a half segment with same starting point as this one
   * @param seg2 a half segment with same starting point as this one
   * @param change true iff the perturbation of this half segment
   * is still to be set
   * @return true iff this half segment can be after seg1 and before seg2
   * (if change: provided that its perturbation is conventiently set) */
  public boolean isBetween(HalfSegment seg1, HalfSegment seg2, boolean change)
  {
    int d1 = seg1.dir, d2 = seg2.dir, d = dir;
    if (d1==d2)
    {   if (d!=d1) return false;
        if (change) return updatePerturb(seg1, seg2);
        return seg1.equalBefore(this) && this.equalBefore(seg2);
    }
    if (d1<d2)
    {   if ((d<d1) || (d>d2)) return false;
    }
    else // d2>d1
    {   if ((d<d1) && (d>d2)) return false; 
    }
    // here this half segment  is loosely between seg1 and seg2
    if (change) return updatePerturb(seg1, seg2);
    if ((d==d1) && ! seg1.equalBefore(this)) return false;
    if ((d==d2) && ! this.equalBefore(seg2)) return false;
    return true;          
  }

  /** Set the perturbation of this half segment to zero.
   * To be called when there are no constraints.
   * @return always true */
  public boolean updatePerturb()
  {  return updatePerturb(null, null);  }
    
  /** Given the two half segments succ and prec (with the same start point
   * and same direction as this one),
   * if possible, update perturbation of this segment in such a way that
   * it can lie between prec and succ in the counterclockwise order around
   * the starting point, and return true; otherwise, return false.
   * @param prec a half segment with same starting point as this one
   * @param succ a half segment with same starting point as this one
   * @return true iff the perturbation of this half segment has been
   * set in such a way to lie after prec and before succ */
  public boolean updatePerturb(HalfSegment prec, HalfSegment succ)
  {
    if (speaking)
       System.out.println("--- update_perturb "+this.toString()
              +"prec="+prec+toString()+", succ="+succ.toString());
    if ((prec!=null) && (prec.dir!=dir)) prec=null;
    if ((succ!=null) && (succ.dir!=dir)) succ=null;
    if (speaking)
       System.out.println("dopo correz prec="+prec+", succ="+succ);
    if ((prec==null) && (succ==null)) // no constraint
    {  perturb = 0.0f; return true;
    }
    float incr = 0.0f;
    if ((dir==0) || (dir==3)) incr = 1; //EAST, SOUTH
    else incr = -1; // WEST, NORTH
    if (prec==null) perturb = succ.perturb-incr;
    else if (succ==null) perturb =  prec.perturb+incr;
    else
    {
      if ((incr==1) && (succ.perturb<prec.perturb)) return false;
      if ((incr==-1) && (succ.perturb>prec.perturb)) return false;
      perturb =  0.5f*(prec.perturb+succ.perturb);
    }
    return true;
  }
  
  /** Return the other half segment incident in the same vertex.
   * @return the mate half segment */
  public HalfSegment getMate() { return mate; }
  
  /** Return the oher half of the same segment (in opposite direction).
   * @return the opposite half segment */
  public HalfSegment getOpposite() { return opposite; }
  
  /** Return the starting point of this half segment.
   * @return the starting point */
  public Vertex getStartingPoint() { return start;  }
  
  /** Return the direction of this half segment. The direction is
   * taken with respect to the starting point.
   * @return the direction */
  public int getDir() {  return dir;  }

}
