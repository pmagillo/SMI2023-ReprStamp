package orthopaths;

/** A vertex of the curve, i.e., a pair of integer coordinates (x,y). */
public class Vertex
{
  /** X coordinate of the vertex. */
  public int x;
  /** Y coordinate of the vertex. */
  public int y;
  
  /** Constructor of this vertex, given the two coordinates.
   * @param x value for the X coordinate
   * @param y value for the Y coordinate */
  public Vertex(int x, int y)
  {  this.x = x; this.y = y;  }

  /** Return a string representing this vertex. */
  public String toString()
  {
    return "Vertex OID="+super.toString()+", ("+x+","+y+")";
  }
}
