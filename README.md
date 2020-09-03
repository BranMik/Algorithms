# Algorithms

## SeamCarver

This is my implementation of Seam Carver algorithm which performs content-aware image scaling.
It was made for Algorithms II course on Coursera as an assignment. 
It uses a combination of dynamic programming and topological sort variant of Dykstra algorithm for finding shortest
paths in DAGs. Seam in this context is just the sequence of neighbouring pixel indices to remove (one pixel for each
row or column, depending if you are removing horizontal or vertical seam).

This problem is similar to classic shortest path problem, just with weights on nodes not edges. For each removal, 
algorithm needs to find the least energy path from one end of the picture to the other. To understand it more clearly
one can draw a hypotethical start node that is connected to all the topmost nodes, and one hypotethical end 
node that all bottom one connect to. Then you have a standard shortest path from a node in a DAG (Directed Acyclic Graph). 
Functionaly it's the same thing, just slightly different implementation. You don't have to keep keep adjacency list
because each pixel/node is connected to three neighbouring pixels in previous and three in next row of the matrix 
(column for horizontal seam), except for bordering rows/columns.

When considering the code one should have in mind that standard picture/pixel representation is width*height which 
equals to columns*rows, and that is opposite to standard 2d array nomenclature ([row][column]). 
Furthermore to perform cache friendly iterations horEnergyMatrix have this order reversed. 

**Also a thing to pay attention to is that Picture object is created from the picture with width always larger than
height, so if original picture have height larger, it will be rotated and original height will in fact become
width. This is a thing of Picture class implementation from algs4.jar library, or maybe of standard Java BufferedImage 
 or ImageIO classes that are used by Picture class. Anyways this effect is not result of this class implementation.** 
	 
I optimized the program for speed, as much as I could, and it performed competently on the assignment tests.
Some optimizations I made are :
- Picture object (pictureData variable) is not updated(recreated) until it's fetched by picture() method. 
Instead intermediary and current data about picture are held in helper matrices.
- Matrices are iterated in a matrixRow*matrixColumn fashion as much as possible to make the code cache friendly.
They are also designed with this in mind (verEnergyMatrix is pixelWidth*pixelHeight, while horEnergyMatrix is 
pixelHeight*pixelWidth).
- Conditionals are as much as possible removed from inner loops, and edge cases (border rows/columns) calculated separately
outside of them.
- Pixels energy is kept in class fields(2d arrays) so once calculated, only a part of values have to be recalculated after 
removals.

The optimizations are mostly memory vs speed where memory usage growth is in acceptable range, and speed increase is quite
significant. First I've done a version without helper matrices, which recreates picture object after each seam removal and 
recalculates all the energy for each find call. This one improves on speed by orders of magnitude.

edu.princeton.cs.algs4. Picture library is contained in algs4.jar which is downloadable on this link : 
https://algs4.cs.princeton.edu/code/
