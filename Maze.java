package lark.fun.maze;

import java.util.Random;


/**
* This class represents a maze as a grid of MazeSquares
* @version 1.1 2012.9.2 Bug that left some squares unconnected to
*	any path corrected. Various methods of MazeGrid overloaded to
*	permit Maze class to deal mainly in MazeSquares, not in raw 
*	coordinates.
* @version 1.0 2012.8.30
* @author U.C. Bergstrom
*/

public class Maze {

private static final int MIN_WIDTH = 8;
private static final int MIN_HEIGHT = 8;
private static final int MAX_WIDTH = 50;
private static final int MAX_HEIGHT = 100;

public static final int DEFAULT_WIDTH = 30;
public static final int DEFAULT_HEIGHT = 40;

private Random rand;

private MazeGrid mg;

/**
* Constructor that initializes a MazeGrid of the desired size and a
* Random object for maze construction.
* @param Desired width of maze.
* @param Desired height of maze.
*/
Maze(int width, int height) {
	// sanity check on inputs
	if (width < MIN_WIDTH) {
		width = MIN_WIDTH;
		System.out.println("Desired width too small.  Reset to " +
			width + ".");
	}
	if (width > MAX_WIDTH) {
		width = MAX_WIDTH;
		System.out.println("Desired width too great.  Reset to " +
			width + ".");
	}
	if (height < MIN_HEIGHT) {
		height = MIN_HEIGHT;
		System.out.println("Desired height too small.  Reset to " +
			height + ".");
	}
	if (height > MAX_HEIGHT) {
		height = MAX_HEIGHT;
		System.out.println("Desired height too great.  Reset to " +
			height + ".");
	}

	mg = new MazeGrid(width, height);
	rand = new Random();
}

/**
* Finds a solution path through the maze.  It starts at coordinates (0,0)
* and runs to the end square at the max x-coordinate and max y-coordinate.  It
* does not cross itself, but--for enjoyment of the user--is quite twisty
* and turny
*/
void findSolPath() {
	int[] possdirs = new int[3];
	int dir, numpossdirs;
	MazeSquare sqr = mg.square(0, 0);
	boolean backout = false;

	// The opening square always starts from the NORTH.
	sqr.pathfrom = MazeGrid.NORTH;

	// Loop while we haven't reached the end square (width - 1, height - 1).
	while ( !mg.isEndSquare(sqr) ) {

		// Compose a set of the directions that are possible moves from
		// the current square.
		numpossdirs = 0;
		for (int DIR = MazeGrid.NORTH, checked = 0; checked < 4; DIR *= 2, checked++) {
			if (solCanGo(sqr, DIR)) {
				possdirs[numpossdirs] = DIR;
				numpossdirs++;
			}
		}
				

		// Select one direction at random from the set of possible directions,
		// unless no possible directions were found.
		if (numpossdirs == 0) {
			// The algorithm in use does still sometimes cause the solution
			// path to go down a one-square wide blind alley.  These could be
			// detected and avoided if the algorithm looked further ahead than
			// its immediate neighborhood, but for now I am going to use a
			// backing-out kludge that, although not elegant, ought to be
			// faster.
			dir = sqr.pathfrom;
			mg.wallCheck(sqr, dir);
			backout = true;			
		} else {
			dir = possdirs[rand.nextInt(numpossdirs)];
		}

		// Set the pathto of the current square, and check
		// if it opens any walls.
		sqr.pathto = dir;
		mg.wallCheck(sqr);
		sqr = mg.squareTo(sqr, dir);

		// Set the pathfrom of the next square, because the next square will 
		// need to know where it came from to evaluate the next set of direction
		// choices.  Exception: if we are backing out, retain the old path
		// information, so we do not keep stepping back and forth into
		// the stuck square.
		if (!backout) {
			sqr.pathfrom = MazeGrid.inverseDir(dir);
		} else {
			backout = false; // Done handling the backout; reset this switch.
		}
	} // while not at end square
} // findSolPath();

/**
* Checks for an available passage to the given direction, for 
* the solution path.  The solution path has strict rules which do 
* not allow it to cross itself, and which help prevent it from 
* cutting itself off from access to the end square.
* @param MazeSquare from which to examine a direction for
*	the solution path.
* @param One of the direction values, MazeGrid.NORTH, SOUTH, EAST,
*	WEST, from the given MazeSquare.
* @return True if the solution path can go in the indicated
*	direction.
*/
private boolean solCanGo(MazeSquare sqr, int direction) {
	// Can't go that direction if there is a wall or part of the
	// solution path in the way.
	if (mg.whatIsTo(sqr, direction) != MazeGrid.Contents.NOTHING) {
		return false;
	}
	
	if ((direction & MazeGrid.NORTHSOUTH) == 0) { // testing east or west directions
		// We can go east but not west if alongside the northern or southern wall.
		if ((sqr.aty == 0) || (sqr.aty == mg.height - 1)) {
			return (direction == MazeGrid.EAST);
		}
	} else { // testing north or south directions
		// We can go south but not north if alongside the eastern or western wall.
		if ((sqr.atx == 0) || (sqr.atx == mg.width - 1)) {
			return (direction == MazeGrid.SOUTH);
		}
	}

	// Determine if we are coming from a direction at right angles to
	// the direction being tested.
	if ( ((direction | MazeGrid.inverseDir(direction)) & sqr.pathfrom) == 0) {
		// If so, examine the square that is "in front of us", if we 
		// keep going the same direction we have been going (i.e., the
		// opposite from the direction we came from).
		MazeSquare nextInLine = mg.squareTo(sqr, MazeGrid.inverseDir(sqr.pathfrom));

		// We cannot go the same way that that path is going--
		// that is, if we are going east-west and we run into a 
		// path that is going south (or coming from the north), we
		// cannot do the same, or we will trap ourselves.
		if ((nextInLine.pathto == direction) || 
				(nextInLine.pathfrom == MazeGrid.inverseDir(direction))) {
			return false;
		}
	}

	// Otherwise, the direction being tested is all good!
	return true;
} // solCanGo(sqr, int)

/**
* Fills the rest of the maze with random filler paths, after the
* solution path has been established.
*/
void fillRestOfMaze() {
	int[] possdirs = new int[4];
	int dir, numpossdirs;
	MazeSquare sqr;

	// Loop over the whole maze, looking for squares that are not yet connected to
	// the rest of the maze.  Upon finding one, open a wall at random between it
	// and one of the adjoining squares that IS part of the maze.  Then, make a filler
	// path starting at that square.
	for (int x = 0; x < mg.width; x++) {
		for (int y = 0; y < mg.height; y++) {
			sqr = mg.square(x, y);
			if (sqr.pathfrom == MazeGrid.NONE) {
				numpossdirs = 0;
				for (int DIR = MazeGrid.NORTH, checked = 0; checked < 4; DIR *= 2, checked++)
				{
					if (mg.whatIsTo(sqr, DIR) == MazeGrid.Contents.PATH) {
						possdirs[numpossdirs] = DIR;
						numpossdirs++;
					}
				}
				dir = possdirs[rand.nextInt(numpossdirs)];
				sqr.pathfrom = dir;
				mg.wallCheck(sqr, dir);
				makeFillerPath(sqr);
			}
		}
	}
} // fillRestOfMaze()

/**
* Makes a "filler" path--that is, a random path that need not
* reach a given endpoint--starting at the given coordinates.
* Stops when blocked on all sides by walls or other paths.
* @param The MazeSquare at which to begin.
*/
void makeFillerPath(MazeSquare sqr) {
	int[] possdirs = new int[3];
	int numpossdirs, dir;
	
	// Keep growing the path until there are no available
	// directions it can go.
	do {
		// Check which directions are open; break from the loop
		// if none, otherwise pick one at random in which to go. 
		numpossdirs = 0;
		for (int DIR = MazeGrid.NORTH, checked = 0; checked < 4; DIR *= 2, checked++) {
			if (mg.whatIsTo(sqr, DIR) == MazeGrid.Contents.NOTHING) {
				possdirs[numpossdirs] = DIR;
				numpossdirs++;
			}
		}
		if (numpossdirs == 0) {
			// be sure to open any walls needed!
			mg.wallCheck(sqr);
			return;
		}
		
		dir = possdirs[rand.nextInt(numpossdirs)];
		sqr.pathto = dir;
		mg.wallCheck(sqr);
		sqr = mg.squareTo(sqr, dir);
		sqr.pathfrom = MazeGrid.inverseDir(dir);
	} while (numpossdirs != 0);
} // makeFillerPath(MazeSquare)

/**
* Draws the maze in a simple ASCII form to standard out.
*/
void drawMaze() {
	int i, j;

	System.out.print("  ");
	for (i = 1; i < mg.width; i++) {
		System.out.print("__"); // north wall
	}
	System.out.println("");

	for (j = 0; j < mg.height - 1; j++) {
		System.out.print("|"); // west wall
		for (i = 0; i < mg.width; i++){
			System.out.print((mg.square(i, j).southwall) ?
					"_" : " ");
			System.out.print((mg.square(i, j).eastwall) ?
					"|" : " ");

		}
		System.out.println("");
	}
	System.out.print("|");
	for (i = 0; i < mg.width - 1; i++){
		System.out.print((mg.square(i, j).southwall) ?
				"_" : " ");
		System.out.print((mg.square(i, j).eastwall) ?
				"|" : " ");

	}
	System.out.println(" |");
} // drawMaze()

/**
* Constructs a random maze that is completed filled with paths, and
* which is guaranteed to have a single solution.  Maze is drawn in 
* simple ASCII form to standard out.  Use may enter values for width
* and height.
*/
public static void main(String[] args) {
	// default width and height of maze.
	int x = DEFAULT_WIDTH;
	int y = DEFAULT_HEIGHT;

	if (args.length > 0) {
		try {
			x = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println(args[0] + " is not an acceptable width.  Using default: "
					+ x);
		}
	}
	if (args.length > 1) {
		try {
			y = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println(args[0] + " is not an acceptable height.  Using default: "
					+ y);
		}
	}

	Maze maze = new Maze(x, y);
	maze.findSolPath();
	maze.fillRestOfMaze();
	maze.drawMaze();
} // main

} // class MazeGrid