package orthopaths;

import java.io.*;
import java.util.*; // Scanner

/** This class contains utilities to manage paths. A path is a string
 * of characters representing directions. The provided utilities are:<UL>
 * <LI>read a path from a file</LI>
 * <LI>check if a path is correct</LI>
 * <LI>reverse a path</LI></UL>
 */
public class PathUtils
{
  /** String describing the problem arised while checking the path. */
  public static String problem = "";
  /** Index where the problem occurred. */
  public static int problemIndex = -1;
    
  /** Read a path from file. On the file, the letters forming the path
   * may or may not be separated by black spaces.
   * @param filename the name of the file
   * @return the string representing the path
   * @throws IOException if the file does not exist or
   * does not contain a path */
  public static String pathFromFile(String filename) throws IOException
  {
    Scanner sc = new Scanner(new File(filename));
    String s;
    String path = "";
    while (sc.hasNext())
    {
      s = sc.next().replaceAll(" ","");
      for (int i=0; i<s.length(); i++)
         path += s.charAt(i);
    }
    if (!isGood(path)) throw new IOException("Error: " + problem);
    return path;
  }
  
  /** Check if a path contains only letters that prepresent directions,
   * i.e., characters equal to 'E', 'N', 'W', or 'S'.
   * @param path the string to be checked
   * @return true iff the string represents a path */
  public static boolean isMadeOfDirections(String path)
  {
    for (int i=0; i<path.length(); i++)
    {
      if (!Direction.isDirection(path.charAt(i)))
      {
        problem = "Character "+path.charAt(i)+" is not a valid direction";
        problemIndex = i;
//        System.out.println("i="+i+": "+path.charAt(i)+" not a direction");
        return false; 
      }
    }
    problem = "";
    problemIndex = -1;
    return true;
  }
  
  /** Check if a path does not contain U-turns.
   * @param path the string representing the path to be checked
   * @return true iff the path does not contain U-turns */
  public static boolean hasNoUTurns(String path)
  {
    for (int i=1; i<path.length(); i++)
    {
      int a = Direction.decodDir(path.charAt(i));
      int b = Direction.decodDir(path.charAt(i-1));
      if ((a==b+2) || (b==a+2))
      {
        problem = "U-turn "+path.charAt(i-1)+path.charAt(i)+" not allowed";
        problemIndex = i;
//        System.out.println("i="+i+": "+c+" opposite to its prec"+path.charAt(i-1));
        return false;
      }
    }
    problem = "";
    problemIndex = -1;
    return true;
  }
  
  /** Check if a path does not pass twice from its starting point
   * (but a closed path is allowed, i.e., the last point
   * and the first point may coincide).
   * @param path the string representing the path to be checked
   * @return true iff the path does not pass twice through its start */
  public static boolean hasAdmissibleStart(String path)
  {
    // horizontal and vertical displacement from starting point
    int countHoriz = 0; 
    int countVertic = 0;
    for (int i=0; i<path.length()-1; i++)
    {
      switch (path.charAt(i))
      {
        case Direction.EAST: countHoriz++; break;
        case Direction.WEST: countHoriz--; break;
        case Direction.NORTH: countVertic++; break;
        case Direction.SOUTH: countVertic--; break;
      }
      if ((countHoriz==0) && (countVertic==0))
      {
        problem = "After "+i+" moves, the path returns to its start";
        problemIndex = i;
        return false;
      }
    }
    problem = "";
    problemIndex = -1;
    return true;
  }
  
  /** Check if a path string is correct. The string must contain only 
   * direction characters, and cannot contain "U turn" such as
   * SN, NS, EW, WE.
   * @param path the string to be checked
   * @return true iff the string represents a correct path 
   * (i.e., it contains only directions, it has no U-turns, it
   * does not pass twice through its starting point) */
  public static boolean isGood(String path)
  {
    problem = "";
    problemIndex = -1;
    // an empty path is not accepted
    if ((path==null) || (path.length()==0))
    {
      problem = "Empty path";
      return false;
    }
    return (isMadeOfDirections(path) && hasNoUTurns(path)
         && hasAdmissibleStart(path));
  }
  
  /** Return the reversed path: reversed order and reverse directions.
   * @param path a string representing a path
   * @return the string representing the reversed path */
  public static String reversePath(String path)
  {
    String rev = "";
    for (int i=0; i<path.length(); i++)
    {
      char c = path.charAt(i);
      int d =  Direction.decodDir(c);
      if (d<2) d += 2; else d -= 2;
      rev = Direction.letterDir[d] + rev;
    }
    return rev;
  }


}
