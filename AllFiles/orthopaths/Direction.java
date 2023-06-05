package orthopaths;

/** <P>Directions are stored as integers: 0=EAST, 1=NORTH, 2=WEST, 3=SOUTH.
 * In input, directions are characters, i.e., the first letter of
 * the direction name.</P>
 * <P>This class contains constants and functions to operate on 
 * directions.</P>
 */
public class Direction
{
   /** Constant number representing cardinal direction East. */
   public static final int E = 0;
   /** Constant number representing cardinal direction North. */
   public static final int N = 1;
   /** Constant number representing cardinal direction West. */
   public static final int W = 2;
   /** Constant number representing cardinal direction South. */
   public static final int S = 3;

   /** Constant character representing cardinal direction East. */
   public static final char EAST  = 'E';
   /** Constant character representing cardinal direction North. */
   public static final char NORTH = 'N';
   /** Constant character representing cardinal direction West. */
   public static final char WEST  = 'W';
   /** Constant character representing cardinal direction South. */
   public static final char SOUTH = 'S';

   /** Array defining the corresponding character of each direction. */
   public static char[] letterDir = { EAST, NORTH, WEST, SOUTH };

   /** Return true iff the given character represents a direction.
    * @param c a character
    * @return true iff c is one of 'e', 'w', 'n', 's' (case insensitive) */
   public static boolean isDirection(char c)
   {
     if ((c=='E') || (c=='e')) return true;
     if ((c=='N') || (c=='n')) return true;
     if ((c=='S') || (c=='s')) return true;
     if ((c=='W') || (c=='w')) return true;
     return false;
   }
      
   /** Decode direction from given character.
    * @param c character representing the direction, it must
    * be one of 'e', 'w', 'n', 's' (case insensitive)
    * @return the direction corresponding to c */
   public static int decodDir(char c)
   {
     switch (c)
     {
       case 'E': case 'e': return E;
       case 'N': case 'n': return N;
       case 'W': case 'w': return W;
       case 'S': case 's': return S;
     }
     return -9999; // error
   }
   
   /** Array of four elements storing the x component of the unit vectors 
    * in each direction. */
   private static int[] xDirList = { 1,0,-1,0 };
   
   /** Array of four elements storing the y component of the unit vectors 
    * in each direction. */
   private static int[] yDirList = { 0,1,0,-1 };
   
   /** Return the x component of the unit vector in the given direction.
    * The pair (xDir(dir),xDir(dir)) is (1,0) for E, (0,1) for N, etc.
    * They accept also directions -1=3 and 4=0.
    * @param dir an integer in {-1,0,1,2,3,4}
    * @return the x component of the unit vector in direction dir */
   public static int xDir(int dir)
   {
     if (dir<0) dir += 4; // if -1
     else if (dir>3) dir -= 4; // if 4
     return xDirList[dir];  
   }
    
   /** Return the y component of the unit vector in the given direction.
    * The pair (xDir(dir),xDir(dir)) is (1,0) for E, (0,1) for N, etc.
    * They accept also directions -1=3 and 4=0.
    * @param dir an integer in {-1,0,1,2,3,4}
    * @return the y component of the unit vector in direction dir */
   public static int yDir(int dir)
   {
     if (dir<0) dir += 4; // if -1
     else if (dir>3) dir -= 4; // if 4
     return yDirList[dir];  
   }
   
   /** Return a point obtained by moving the given point 
    * one unit in the given direction.
    * @param point a point
    * @param dir an integer (direction) in {-1,0,1,2,3,4}
    * @return a new point equal to the given one, moved in direction dir */
   public static Vertex movePoint(Vertex point, int dir)
   {  return new Vertex(point.x+xDir(dir), point.y+yDir(dir));  }

   /** Change the given point by moving it one unit in the given direction.
    * @param point a point
    * @param dir an integer (direction) in {-1,0,1,2,3,4} */
   public static void moveThisPoint(Vertex point, int dir)
   {
     point.x += xDir(dir);
     point.y += yDir(dir);
   }

   /** Return the opposite direction to the give one.
    * Assume that dir is in {0,1,2,3}.
    * @param dir an integer (direction) in {0,1,2,3}
    * @return the opposite direction */
   public static int oppositeDir(int dir)
   {
     if (dir<2) return dir+2;
     else return dir-2;
   }
           
  /** Given a character representing a direction, return the
   * character representing the opposite direction.
   * @param d a character representing a direction
   * @return the character representing the opposite direction */
  public static char oppositeDir(char d)
  {
    switch (d)
    {
      case NORTH: return SOUTH;
      case SOUTH: return NORTH;
      case WEST: return EAST;
      case EAST: return WEST;
    }
    return 'X'; // it should not happen
  }

}
