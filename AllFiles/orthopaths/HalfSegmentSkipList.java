package orthopaths;

/**
 * Class for a doubly linked list containing half segments, where
 * each node has, in addition, a pointer to the node containing the
 * mate half segment of the half segment contained in this node.
 * Such a pointer is used to go directly to the mate, skiping 
 * all nodes located in between.
 * All half segments in a skip list have the same starting point,
 * and are radially sorted in counterclockwise order.
 * In the overall program, a skip list is associated with each vertex.
*/
public class HalfSegmentSkipList
{
  /** If true, debug information is printed during the operations. */
  public static boolean speaking = false;
  
  /** Inner class for the nodes. Each node points to a half segment
   * (the content), to precedent and next nodes in the list, and to
   * the node contraining the mate half segmen t of its content. */
  public class Node
  {
    /** Half segment contained in this node. */
    public HalfSegment content;

    /** Pointer to next node. */
    public Node next;
    /** Pointer to previous node. */
    public Node prev;
    /** Pointer to the node containing the mate half segment. */
    public Node matepos;
    
    /** Create a new node containing the given half segment.
     * @param seg a half segment */
    public Node(HalfSegment seg)
    {
      content = seg;
      next = prev = matepos = null;
    }
  }
  
  /** Pointer to first node. */
  protected Node first;
  /** Number of nodes. */
  protected int num;

  /** Create an empty skip list. */
  public HalfSegmentSkipList()
  {
    first = null;
    num = 0;
  }
  
  /** Return the number of elements of the skip list.
   * @return the size of this list */
  public int size()  {  return num;  }

  /** Add the first node, containing the given half segment.
   * This skip list must be empty.
   * @param seg a half segment
   * @return the new node containing seg, that has been
   * placed as first node of this list */
  protected Node addFirstNode(HalfSegment seg)
  {
    Node node = new Node(seg);
    node.prev = node.next = node;
    first = node;
    num = 1;               
    return first;
  }
  
  /** Add the first two nodes, containing two mate half segments.
   * This skip list must be empty.
   * @param seg1 a half segment
   * @param seg2 another half segment, with same starting point
   * but different direction of seg1 */
  public void addFirstPair(HalfSegment seg1, HalfSegment seg2)
  {
    Node node1 = addFirstNode(seg1);
    Node node2 = addBeforeNode(node1, seg2);
    node1.matepos = node2;
    node2.matepos = node1;
  }
  
  /** Add a new node containing seg to the list, before curr.
   * @param curr a node in this list
   * @param seg a half segment
   * @return the new node containing seg, that has been 
   * placed before curr */
  protected Node addBeforeNode(Node curr, HalfSegment seg)
  {
    Node node = new Node(seg);
    node.prev = curr.prev;
    node.next = curr;
    curr.prev.next = node;
    curr.prev = node;
    num ++;
    return node;
  }
  
  /** Find the correct place to add a new node with the given half
   * segment, add it and return the node containing it. 
   * The half segment seg is the entering segment, the perturbation of seg is
   * already set, and this skip list contains an even number of elements
   * (a set of pairs of mate segments).
   * If it is not possible to find a place for the given segment, return
   * null (but this should not happen).
   * @param seg a half segment
   * @return the new node containing seg, that has been placed in
   * the correct radial order in this list */
  public Node addEntering(HalfSegment seg)
  {
    if (speaking) System.out.println("add_enter "+seg.toString());
    if (first==null)
      return addFirstNode(seg);
    if (num<2)
      System.out.println("ERR, numero elementi non >=2 : "+num);
    Node curr = first;
    while (true)
    {
      if (speaking)
         System.out.println("  ciclo, segmenti "+curr.prev.content.name+" "+curr.content.name);
      if (seg.isBetween(curr.prev.content, curr.content))
      {
        if (speaking)
        {
          System.out.println("... inserted");
          System.out.println("Inserito entrante tra "+curr.prev.content.name+
             " e "+curr.content.name);
        }
        return addBeforeNode(curr, seg);
      }
      curr = curr.next;
      if (curr==first) return null;
    }
  }

  /** Find the correct place to add a new node with the given half
   * segment, starting from the given node containing its mate.
   * If a correct place is not found, return null (this means that
   * the curve has an intersection).
   * If a correct position is found, add the half segment and return 
   * the node containing it. 
   * The half segment seg is the exiting segment, the perturbation of seg is
   * to be set (it will be set in such a way that seg can be inserted), 
   * and this skip list contains an even number of elements (a set of
   * pairs of mate segments), plus the mate of seg in node mate_node.
   * @param seg a half segment
   * @param mate_node the node of this list, containing the mate of seg
   * @return the new node containing seg, that has been placed in
   * the correct radial order in this list (or null if a correct
   * position for seg does not exist) */
  public Node addExiting(HalfSegment seg, Node mate_node)
  {
    if (speaking) System.out.println("add_exit "+seg.toString());
    if (num<1)
      System.out.println("ERR, numero elementi non >=1 : "+num);
    if (num==1)
    {
      seg.updatePerturb();
      return addBeforeNode(first, seg);
    }      
    int i = 1;
    Node curr = mate_node;
    while (i<=num)
    {
      curr = curr.next;
      if (i==num)
      {
        if (speaking) System.out.println("  try to put seg before its mate");
        if (seg.isBetween(mate_node.prev.content, mate_node.content, true))
        {
          if (speaking) System.out.println("... inserted");
          Node node = addBeforeNode(mate_node, seg);
          mate_node.matepos = node;
          node.matepos = mate_node;
          return node;
        }
        else return null;
      }
      if (speaking) 
        System.out.println("   consider seg "+curr.prev.content.name+" and "+curr.content.name);
      if (seg.isBetween(curr.prev.content,curr.content, true))
      {
        if (speaking) System.out.println("... inserted");
        Node node = addBeforeNode(curr, seg);
        mate_node.matepos = node;
        node.matepos = mate_node;
        return node;                                  
      }
      if (i==num-1) break; // LAST TIME
      if (speaking) System.out.println("Check if in forbidden sector "+curr.content.name+" "+curr.content.mate.name);
      if (seg.isBetween(curr.content, curr.content.mate, false))
      {
        if (speaking) System.out.println("  segment in forbidden sector");
        return null;
      }
      curr = curr.matepos;
      if (speaking) System.out.println("fine giro, curr "+curr);
    } // end while           
    if (speaking) System.out.println("no good position found");
    return null;
  }
  
  /** Return a string representing this skip list.
   * @return string representing this skip list */
  public String toString()
  {
    if (first==null) return "Empty list";
    String s = "";
    Node curr = first;
    while (true)
    {
      s += "\n"+curr.content.toString();
      curr = curr.next;
      if (curr==first) break;
    }
    return s;        
  }
  
  /** Return the half segment contained in the given node.
   * @param node a node of this skip list
   * @return the contained half segment */
  public HalfSegment segmentAt(Node node)
  {  return node.content;  }
  
  /** NOT USED ***************
   * This skip list contains at least two elements.
   * Search for the node containing a segment with the
   * given direction, delete it, and return the node
   * containing its mate. 
   * @param dir a direction in {0,1,2,3}
   * @return if a node with a half segment in direction dir 
   * was present and has been removed, the node containing the
   * mate of the removed hals segment (otherwise, null) */
  public Node searchRemoveDir(int dir)
  {
    if (first==null) return null;
    Node curr = first;
    while (true)
    {
      if (curr.content.dir == dir)
      {
        Node found = curr.matepos;
        if (curr==first) first = curr.next;
        curr.prev.next = curr.next;
        curr.next.prev = curr.prev;
        num --;
        return found;
      }
      curr = curr.next;
      if (curr==first) break;
    }
    return null;
  }

  /** Given a node in this skip list, remove it.
   * @param n a node of this skip list */
  protected void auxRemove(Node n)
  {
    if (num==1) 
    {
      first = null;
      num = 0;
    }
    else
    {
      Node prev = n.prev;
      Node next = n.next;
      prev.next = next;
      next.prev = prev;
      if (n==first) first = next;
      num--;
    }
  }
  
  /** Search the given half segment and, if found, remove it 
   * from the list, together with its mate half segment.
   * Return true if s was present, false otherwise.
   * @param seg a half segment
   * @return true if seg was in this list and has been removed,
   * false if seg was not in this list */
  public boolean searchRemove(HalfSegment seg)
  {
    if (first==null) return false;
    Node curr = first;
    while (true)
    {
      if (curr.content==seg)
      {
        Node aux = curr.matepos;
        auxRemove(curr);
        if (aux!=null) auxRemove(aux);  
        return true;
      }
      else curr = curr.next;
      if (curr==first) break;
    }
    return false;
  }
  
  void rescaleValues(HalfSegment array[], int len)
  {
    int i, pos = len/2;
    array[pos].perturb = 0.0f;
    if (array[pos].opposite!=null) array[pos].opposite.perturb = 0.0f;
    for (i=-1; i+pos>=0; i--)
    {  array[i+pos].perturb = i;
       if (array[i+pos].opposite!=null) array[i+pos].opposite.perturb = i;
    }
    for (i=1; i+pos<len; i++)
    {
       array[i+pos].perturb = i;
       if (array[i+pos].opposite!=null) array[i+pos].opposite.perturb = i;
    }
  }
  
  /** Perform the job of adjustPerturbations for the given
   * direction dir. */
  protected void adjustPerturbations(int dir)
  {
    HalfSegment[] aux = new HalfSegment[num];
    int cont=0;
    Node nod = first;
    if (nod.content.dir==dir)
    { while (nod.prev.content.dir==dir)
      {  nod=nod.prev;
         if (nod==first) break;
      }
    }
    else
    { while (nod.content.dir!=dir)
      {  nod=nod.next;
         if (nod==first) break;
      }
    }
    // now nod is the first one of the nodes with dir==d
    if (nod.content.dir!=dir) return;
    int i=0, j;
    do
    {
      aux[i++] = nod.content;
      nod = nod.next;
    }
    while (nod.content.dir==dir);
    if (i<2) return;
    // perturbations need to be sorted in increasing
    // order, if necessary we reverse the order
    if (aux[0].perturb>aux[1].perturb)
    {
      HalfSegment temp;
      for (j=0; j<i/2; j++)
      {  temp=aux[j]; aux[j]=aux[i-j-1]; aux[i-j-1]=temp;
      }
    }
    // check if they actually need rescaling
    float min_dist = aux[1].perturb-aux[0].perturb;
    for (j=2; j<i; j++)
      if ((aux[j].perturb-aux[j-1].perturb) < min_dist) 
         min_dist = aux[j].perturb-aux[j-1].perturb;
    if (min_dist>=1.0f) return;
    // now rescale
    /* controllo
    System.out.print("Perturbazioni da riscalare: ");
    for (int k=0; k<i; k++) System.out.print(" "+aux[k].perturb);
    System.out.println();*/
    rescaleValues(aux,i);
    /* controllo
    System.out.print("Perturbazioni riscalate: ");
    for (int k=0; k<i; k++) System.out.print(" "+aux[k].perturb);
    System.out.println();*/
  }
  
  /** If the min distance between two consecutive perturbations is
   * too small, then rescale all perturbations in such a way that the 
   * minimum distance is 1 again (the maximum perturbation will increase). */
  protected void adjustPerturbations()
  {
    // perturbations cannot be unbalanced with less than 5 segments
    // (i.e., with at most two passages through the vertex)
    if (num<5) return;
    // if the number of segments is odd, the path must continue from here
    // and we should not modify the situation
    if (num%2 == 1) return;
    
    for (int d=0; d<4; d++) // for each direction
    {
      adjustPerturbations(d);
    }    
  }
  
}
