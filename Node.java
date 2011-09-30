import java.util.Vector;


// The node class 
public class Node {
	
	private Vector <Integer> node;
	private final int n_dim;
	
	// The current score, the from-start score, and the heuristic score of the node.
	public double f_score, g_score, h_score;
	
	// Whether the node is passable or not.
	public boolean is_passable;
	
	public Node(Vector <Integer> node, boolean is_passable) {
		
		this.node = node;
		n_dim = node.size();		
		this.is_passable = is_passable;
		
	}
	
	public Node( Node n ) {
		this.node = n.node;
		this.n_dim = n.n_dim;
		this.f_score = n.f_score;
		this.g_score = n.g_score;
		this.h_score = n.h_score;
		this.is_passable = n.is_passable;		
	}
	
	public Node(Integer node1, Integer node2, boolean is_passable) {
		
		node = new Vector <Integer> (); 
		node.add(node1);
		node.add(node2);
		n_dim = 2;		
		this.is_passable = is_passable;
		
	}
	
	
	//Override equals() method
	public boolean equals( Object obj ) {
		
		if( !(obj instanceof Node) )
			return false;
		
		Node compNode = (Node) obj;
		
		if( !( this.is_passable == compNode.is_passable ) )
			return false;
		
		if( !( this.node.equals(compNode.node) ))
			return false;
		
		if( !( this.n_dim == compNode.n_dim ) )
				return false;
		
		return true;
	}
	
	
	//Override hashcode() method
	public int hashcode() {
		
		int hash = 7;
		
		hash = (int) (31 * hash + Math.ceil( this.absolute_value() ));
		
		return hash;
		
	}
	
	public double distance(Node n){
		
		double distance = 0.0;
		if(n.n_dim != n_dim) {
			return -1;
		}
		else {
			for( int i = 0; i < n_dim; i++)
				distance += ( node.elementAt(i) - n.node.elementAt(i) ) * 
								( node.elementAt(i) - n.node.elementAt(i) );
			return distance;
		}
		
	}
	
	public double absolute_value() {
		
		double abs = 0.0;
		
		for( int i = 0; i < n_dim; i++)
			abs += ( node.elementAt(i) ) * ( node.elementAt(i) );
		
		return abs;
		
	}
	
	public double get_heuristic_score( Node n ) {
		
		return distance( n ); 
	
	}
	
	
	public Vector <Integer> getVector () {
		return node;
	}
 	
	
	public int changeValue(Vector <Integer> n) {
		
		if( n.size() != n_dim)
			return -1;
		else
			node = n;	
		
		return 0;
	
	}
	
}
