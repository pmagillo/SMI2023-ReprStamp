package orthopaths;

/** <P>A list of vertices, where a skip list of half segments is
 * associated with each vertex.
 * The list is intended to contain all the vertices of a curve, and stores 
 * the vertices in lexicographic order of their coordinates.
 * <P>The class simulates a dictionary in which every point (x,y) is a key.
 * The associated value is a radially sorted list of half segments
 * implemented as a skip list (class HalfSegmentSkipList).</P>
 */
public class VertexSet
{
  /** Inner class for an element (node) of the list of vertices.
   * The list is a simply connected linked list. */
  public class Element
  {
    /** X coordinate of the vertex. */
    public int x;
    /** Y coordinate of the vertex. */
    public int y;

    /** Skip list associated with the vertex. */
    public HalfSegmentSkipList star;

    /** Pointer to next element. */
    Element next;
    
    /** Create a new element (node) with vertex (x,y) and
     * associate an empty skip list.
     * @param x x coordinate of the vertex
     * @param y y coordinate of the vertex */
    public Element(int x, int y)
    {
      this.x = x;
      this.y = y;
      star = new HalfSegmentSkipList();
      next = null;
    }
    
  /** Check if this vertex has the given coordinates.
   * @param x x coordinate
   * @param y y coordinate
   * @return this iff this vertex has coordinates (x,y) */
  public boolean equal(int x, int y)
  {  return  (x==this.x)&&(y==this.y);  }
  
  /** Check if the coordinates of this vertex are lexicographically
   * strictly before the given pair (x,y).
   * @param x x coordinate
   * @param y y coordinate
   * @return this iff this vertex lies before (x,y) */
  public boolean lexBefore(int x, int y)
  {  return  (this.x < x) || ((this.x==x) && (this.y<y)); }
  
  /** Check if the coordinates of this vertex are lexicographically
   * strictly after the given pair (x,y).
   * @param x x coordinate
   * @param y y coordinate
   * @return this iff this vertex lies after (x,y) */
  public boolean lexAfter(int x, int y)
  {  return  (this.x > x) || ((this.x==x) && (this.y>y)); }
    
  }
  
  /** Total number of vertices in the list. */
  protected int count;
  /** Pointer to first vertex in the list. */
  protected Element first;
  
  /** Create an empty set of vertices. */
  public VertexSet()
  {  first = null; count = 0; }

  /** Return the number of elements of this set.
   * @return size of this set */
  public int size() {  return count;  }

  /** Insert a vertex (x,y) in this set if it is not already present.
   * If present, just locate it in the list.
   * Return the skip list associated with the vertex.
   * @param vert a vertex to be added
   * @return the skip list associated with vert */  
  public HalfSegmentSkipList conditionalInsert(Vertex vert)
  {  return conditionalInsert(vert.x, vert.y);  }

  /** Insert a vertex (x,y) in this set if it is not already present.
   * If present, just locate it in the list.
   * Return the skip list associated with the vertex.
   * @param x x coordinate of the vertex to be added
   * @param y y coordinate of the vertex to be added
   * @return the skip list associated with (x,y) */  
  public HalfSegmentSkipList conditionalInsert(int x, int y)
  {
    Element curr = first;
    Element prev = null;
    while ( (curr!=null) && (curr.lexBefore(x,y)) )
    { 
      prev = curr;
      curr = curr.next;
    }
    if ((curr!=null)&&(curr.x==x)&&(curr.y==y)) return curr.star; // found
    Element added = new Element(x,y);
    added.next = curr;
    if (prev!=null) prev.next = added;
    else first = added; // first was null
    count ++;
    return added.star;
  }
  
  /** Print this set of vertices.
   * @param newline true iff we want to go on new line after printing */
  public void print(boolean newline)
  {
    if (first==null) 
    {  System.out.print("Empty list");
       if (newline) System.out.println();
    }
    Element curr = first;
    while (curr!=null)
    {
      System.out.print(" ("+curr.x+","+curr.y+") with "+curr.star.size()+" half segments");
      curr = curr.next;
    }
    if (newline) System.out.println();
  }
  
 void adjustPerturbations()
 {
   Element el = first;
   while (el!=null)
   {
     el.star.adjustPerturbations();
     el = el.next;
   }
 }
 
}
