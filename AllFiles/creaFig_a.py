# STEP A FOR CREATING FIGURE 15:
# GENERATE THE PROGRAM FOR STEP B

# Generate a random path with no U-turns of
# length from 100 to 54000 with step=100.
# For each path, compute the degrees of all 
# traversed vertices, their sum and their max.
# Write a MatLab source code which makes the
# two plots in Figure 15 of the paper, having 
# x = path length
# y1 = sum of vertex degrees
# y2 = max vertex degree

from random import randrange

NumToDir = ('E','S','W','N')

def randomDir(forbidden=None):
   D = forbidden
   while (D==forbidden):
     D = NumToDir[randrange(4)]
   return D

def extendPath(path):
  if len(path)==0:
     path = [randomDir()]
  else:
     D = randomDir(path[-1])
     path.append(D)
  return path

def countDegrees(path):
  P = [0,0]
  vertexDeg = {tuple(P) : 1}
  sumDeg = 1
  for move in path:
     if move=='E': P[0]+=1
     elif move=='S': P[1]-=1
     elif move=='W': P[0]-=1
     else: P[1]+=1
     if tuple(P) in vertexDeg:
       vertexDeg[tuple(P)] += 1
     else:
       vertexDeg.update({tuple(P) : 1})
     sumDeg += vertexDeg[tuple(P)]
  return vertexDeg, sumDeg

# MAIN
print("--start--");
out = open("creaFig_b.m","w")

#Write a MatLab matrix with three columns:
#1) path length
#2) max sum of vertex degrees
#3) max vertex degree
out.write("MAT = [\n");
N_TIMES = 10
LENGTH = 100
MAX_LENGTH = 54000
while (LENGTH <= MAX_LENGTH):
  maxDeg, maxSum = 0, 0
  for k in range(N_TIMES):
     PATH = []
     for i in range(LENGTH):
        PATH = extendPath(PATH)
     DEG, SUMDEG = countDegrees(PATH)
     s = max(DEG.values())
     if s>maxDeg: maxDeg = s
     if SUMDEG>maxSum: maxSum = SUMDEG
  out.write(str(LENGTH)+", "+str(maxSum)+", "+str(maxDeg)+"\n");
  LENGTH += 100
  if (LENGTH%1000)==0:
    print("Processing path length "+str(LENGTH)+" out of "+str(MAX_LENGTH));
out.write("];\n");

# Write the MatLab code for making figure 15
out.write("close all\n");
out.write("subplot(2,1,1)\n");
out.write("X = MAT(:,1);\n");
out.write("Y = MAT(:,3);\n");
out.write("plot(X,Y)\n");
out.write("grid on\n");
out.write("\n");
out.write("subplot(2,1,2)\n");
out.write("Y = MAT(:,2);\n");
out.write("plot(X,Y)\n");
out.write("grid on\n");
out.close();
print("--done--");
print("Run file creaFig_b.m in MatLab to create the figure");
print("--end--");
