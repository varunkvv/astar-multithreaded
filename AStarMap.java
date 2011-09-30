import java.util.ArrayList;
import java.util.Vector;


/* The map class, basically a regular grid where each node is an object
 * of the node class.
 */ 
//Currently supports only 2D!!!
public class AStarMap {
	
	
	private Vector <Vector<Node>> map;
	public final int height, width;
	
	public AStarMap( boolean[][] m, int height, int width ) {
		
		map = new Vector < Vector <Node> > ();		
		for( int i = 0; i < height; i++) {			
			Vector <Node> row = new Vector <Node> ();
			for( int j = 0; j < width; j++) {
				
				Node n = new Node( i, j, m[i][j] );
				row.add( n );
				
			}
			map.add( row );
		}
		
		this.height = height;
		this.width = width;
	}
	
	// Obtain the neighbour nodes of a given node in the map.	
	public ArrayList<Node> getNeighbourNodes( Node n ) {
		
		System.out.println("  NEIGHBOUR NODES FUNCTION ");
		
		Vector <Integer> v = new Vector <Integer> (n.getVector());
		ArrayList <Node> l= new ArrayList <Node> ();
		
		System.out.println(" Element at 0: " + v.elementAt(0));
		System.out.println(" Element at 1: " + v.elementAt(1));
		
		
		for( int i = v.elementAt(0) - 1; i <= v.elementAt(0) + 1; i++ ) {
			for( int j = v.elementAt(1) - 1; j <= v.elementAt(1) + 1; j++ ) {
				
				if( i < 0 || j < 0 || i >= height || j >= width)
					continue;
				
				if( (i == v.elementAt(0)) && (j == v.elementAt(1)) )
					continue;
				
				
				Node temp = new Node( (map.elementAt(i)).elementAt(j) );
				
				if( !temp.is_passable ) 
					continue;
				
				l.add(temp);
				
			}
		} 
		
		return l;
		
	}
	
}
