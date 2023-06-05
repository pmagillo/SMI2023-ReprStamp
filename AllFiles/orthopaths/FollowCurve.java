package orthopaths;

import java.io.*;

/** This class allows checking whether a path is self intersecting.
 * It may be necessary to divide the path into two parts and check
 * each part separately, because the intersection  test must 
 * start from the point of lowest y among the points with lowest x
 * coordinate.
 */
public class FollowCurve
{
  
  /** Return a string from a string of directions representing a path.
   * Insert blank spaces between the letters. Optionally, reverse the
   * path (i.e., consider the reversed string of reversed directions).
   * @param s a string of directions
   * @param rev if true, consider the reversed path 
   * @return a string with blank spaces between the letters */
  public static String stringForPath(String s, boolean rev)
  {
    int i;
    String st = "";
    if (rev)
     for (i=s.length()-1; i>=0; i--)
     {
       char d = Direction.oppositeDir(s.charAt(i));
       st = st +" "+d;
     }
    else
     for (i=0; i<s.length(); i++)
     {
       st = st +" "+s.charAt(i); 
     }
    return st;
  }
  
  /** Return a string from a modified path. The result is a string of 
   * directions and perturbations, separated by blank spaces. 
   * Optionally, reverse the path.
   * @param pp a modified path with perturbations
   * @param rev if true, consider the reversed path 
   * @return a string with perturbations and blank spaces
   * between the letters */
  public static String stringForPath(PathChecker.CheckedPath pp, boolean rev)
  {
    int i;
    String st = "";
    if (rev)
     for (i=pp.length()-1; i>=0; i--)
     {
       char d = Direction.oppositeDir(pp.getDirName(i));
       float pert = pp.getPerturbation(i);
       int rounded = (int)pert;
       if (pert==(float)rounded) st += " "+d+" "+rounded;
       else st += " "+d+" "+pert;
     }
    else
     for (i=0; i<pp.length(); i++)
     {
       char d = pp.getDirName(i);
       float pert = pp.getPerturbation(i);
       int rounded = (int)pert;
       if (pert==(float)rounded) st += " "+d+" "+rounded;
       else  st += " "+d+" "+pert;
     }
    return st;
  }
  
  /** MAIN PROGRAM. 
   * Accept a file name on the command line,
   * read a path string from the file and check it.
   * On a file having the same name as the input one, with prefix
   * "out_", write the original path and the checked version.
   * <BR>
   * If the path is self intersecting, the checked version will
   * stop at the detected intersection.
   * @param arg command line, it must contain the input file name
   * @throws java.io.IOException if the file does not
   * exist or does not have the correct syntax. */
  public static void main(String[] arg) throws java.io.IOException
  {
    if (arg.length<1)
    {
      System.out.println("Need file name");
      return;
    }
    System.out.println("Leggo da "+arg[0]);
    String input_path = PathUtils.pathFromFile(arg[0]);

    if (!PathUtils.isGood(input_path))
    {
      System.out.println("path "+input_path+" is not valid");
      return;
    }
    System.out.println("From (0,0) path: "+input_path);
  
    PathChecker checker = new PathChecker();
    
    PathChecker.CheckedPath output = checker.test(0,0, input_path, true);
    System.out.println(output.hasIntersection());
    System.out.println(output);
    PrintStream output_file = new PrintStream("out_"+arg[0]);
    // 1) write original path
    output_file.println(stringForPath(input_path,false));
    // 2) write perturbed path
    output_file.println(stringForPath(output,false));
  }
            
}
