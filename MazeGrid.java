package lark.fun.maze;

/**
* This is a class to represent a grid of MazeSquares.
* @version 1.1 2012.9.2 Bug that left some squares unconnected to
*	any path corrected. Various methods overloaded to permit Maze
*	class to deal mainly in MazeSquares, not in raw coordinates.
* @version 1.0 2012.8.30
* @author U.C. Bergstrom
*/

class MazeGrid {

//useful constants
public static final int NONE = 0;
public static final int NORTH = 1;
public static final int SOUTH = 4;
public static final int EAST = 2;
public static final int WEST = 8;
public static final int NORTHSOUTH = 5;
public static final int EASTWEST = 10;
public static final int ALL = 15;

public enum Contents {NOTHING, WALL, PATH};

private MazeSquare[][] grid;
int width, height;

MazeSquare currsqr;

/**
* Constructs the MazeGrid with the given dimensions.
* @param The width of the grid.
* @param The height of the grid.
*/
MazeGrid(int width, int height) {
	this.width = width;
	this.height = height;

	// initialize the grid
	grid = new MazeSquare[width][height];
	for (int i = 0; i < width; i++) {
		for (int j = 0; j < height; j++) {
			grid[i][j] = new MazeSquare(i, j);
		}
	}

	grid[0][0].pathfrom = NORTH;

} // MazeGrid(int, int) constructor

/**
* Determines if the MazeSquare is the end square (at 
* maximum coordinates.)
* @param A given MazeSquare in the maze.
* @return Whether or not this is the end square.
*/
boolean isEndSquare(MazeSquare sqr) {
	return isEndSquare(sqr.atx, sqr.aty);
} // isEndSquare(MazeSquare)

/**
* Determines if the coordinates given are of the end
* square (at maximum coordinates.)
* @param x-coordinate of a given MazeSquare in the maze.
* @param y-coordinate of a given MazeSquare in the maze.
* @return Whether or not this is the end square.
*/
boolean isEndSquare(int x, int y) {
	return ((x == width - 1) && (y == height - 1));
} // isEndSquare(int, int)

/**
* Fetches the indicated MazeSquare. WARNING: does not
* check for out-of-bounds coordinates.
* @param x-coordinate of a given MazeSquare in the maze.
* @param y-coordinate of a given MazeSquare in the maze.
* @return The MazeSquare at the given coordinates.
*/
MazeSquare square(int x, int y) {
	return grid[x][y];
}

/**
* Translates the movement of a path through a square into open walls,
* to facilitate the drawing of the maze.
* @param MazeSquare to check.
*/
void wallCheck(MazeSquare sqr) {
	if ((sqr.pathto == EAST) || (sqr.pathfrom == EAST))
		sqr.eastwall = false;
	if ((sqr.pathto == SOUTH) || (sqr.pathfrom == SOUTH))
		sqr.southwall = false;
}

/**
* Translates the movement of a path through a square--and from the next
* square over in the given direction--into open walls, to facilitate the 
* drawing of the maze.  WARNING: does not check for out-of-bounds
* conditions in the input direction; use whatIsTo() first.
* @param MazeSquare to check.
* @param direction from which the path came to the given MazeSquare,
*	designating the next-door MazeSquare whose walls should
*	also be checked.
*/
void wallCheck(MazeSquare sqr, int backdirection) {
	wallCheck(sqr);
	if (backdirection == NORTH) squareTo(sqr, backdirection).southwall = false;
	if (backdirection == WEST) squareTo(sqr, backdirection).eastwall = false;
}

/**
* Determines the contents of the indicated MazeSquare.
* @param A MazeSquare in the maze.
* @param One of the direction values, Maze.NORTH, SOUTH, EAST,
*	WEST, from the given MazeSquare.
* @return The contents of the MazeSquare one square away in
*	the indicated direction from the given MazeSquare, as
*	the values Contents.NOTHING, WALL, or PATH.
*/
Contents whatIsTo(MazeSquare sqr, int direction) {
	return whatIsTo(sqr.atx, sqr.aty, direction);
} // whatIsTo(MazeSquare, int)

/**
* Determines the contents of the indicated MazeSquare.
* @param x-coordinate of a given MazeSquare in the maze.
* @param y-coordinate of a given MazeSquare in the maze.
* @param One of the direction values, Maze.NORTH, SOUTH, EAST,
*	WEST, from the given MazeSquare.
* @return The contents of the MazeSquare one square away in
*	the given direction from the MazeSquare at the given
*	x- and y-coordinates as the values Contents.NOTHING,
*	WALL, or PATH.
*/
Contents whatIsTo(int x, int y, int direction) {
	switch (direction) {
		case NORTH:
			if (y == 0) return Contents.WALL;
			break;
		case SOUTH:
			if (y == height - 1) return Contents.WALL;
			break;
		case EAST:
			if (x == width - 1) return Contents.WALL;
			break;
		case WEST:
			if (x == 0) return Contents.WALL;
			break;
	} // switch

	if (squareTo(x, y, direction).pathfrom != NONE) return Contents.PATH;
	return Contents.NOTHING;
} // whatIsTo(int, int, int)

/**
* Fetches the indicated MazeSquare.  WARNING: does not check for
* out-of-bounds coordinates.  Be sure to use whatIsTo() first.
* @param A MazeSquare in the maze.
* @param One of the direction values, Maze.NORTH, SOUTH, EAST,
*	WEST, from the given MazeSquare.
* @return The MazeSquare one square in the direction from the
* 	given MazeSquare.
*/
MazeSquare squareTo(MazeSquare sqr, int direction) {
	return squareTo(sqr.atx, sqr.aty, direction);
} // squareTo(MazeSquare, int)

/**
* Fetches the indicated MazeSquare.  WARNING: does not check for
* out-of-bounds coordinates.  Be sure to use whatIsTo() first.
* @param x-coordinate of a given MazeSquare in the maze.
* @param y-coordinate of a given MazeSquare in the maze.
* @param One of the direction values, Maze.NORTH, SOUTH, EAST,
*	WEST, from the given MazeSquare.
* @return The MazeSquare one square in the direction indicated
* 	from the MazeSquare at the given x- and y-coordinates.
*/
MazeSquare squareTo(int x, int y, int direction) {
	switch (direction) {
		case NORTH:
			return grid[x][y-1];
		case SOUTH:
			return grid[x][y+1];
		case EAST:
			return grid[x+1][y];
		case WEST:
			return grid[x-1][y];
		default:
			return grid[x][y];
	} // switch
} // squareTo(int, int, int)

/**
* Swaps SOUTH and NORTH, or EAST and WEST (or remains NONE)
* by virtue of the particular values chosen to represent them.
* @param One of the direction values: Maze.NONE, NORTH, SOUTH, EAST,
*	WEST.
* @return The opposite direction, or NONE for NONE.
*/
static int inverseDir(int direction) {
	// SOUTH or WEST % 4 is zero. Divided by four, SOUTH and WEST
	// give their opposites. NORTH or EAST % 4 is non-zero, and their
	// opposites are obtained by multiplying by 4.
	return (direction % 4 == 0) ? (direction / 4) : (direction * 4);
} // inverseDir(int)

} // class MazeGrid