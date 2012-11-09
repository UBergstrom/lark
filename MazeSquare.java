package lark.fun.maze;

/**
* This is a class to represent a square of a maze.
* @version 1.1 2012.9.2 Code refactoring requires the MazeSquare
*	to maintain information about its own coordinates in the
*	MazeGrid.
* @version 1.0 2012.8.30
* @author U.C. Bergstrom
*/

/**
* A class to store basic information about each square in the
* maze--much like a C struct.
*/
class MazeSquare {

int atx;
int aty;
int pathfrom;
int pathto;
boolean eastwall;
boolean southwall;

/**
* Simple constructor, which defaults to showing no path and
* to having its walls closed.
* @param The x-coordinate of the square in the grid
* @param The y-coordinate of the square in the grid
*/
public MazeSquare(int xcoord, int ycoord) {
	atx = xcoord;
	aty = ycoord;
	pathfrom = MazeGrid.NONE;
	pathto = MazeGrid.NONE;
	eastwall = true;
	southwall = true;
} // constructor

} // class MazeSquare