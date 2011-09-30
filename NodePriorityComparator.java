import java.util.Comparator;

// Node comparator class for sorting the nodes by their f score.
public class NodePriorityComparator implements Comparator <Node> {

	public int compare( Node n1, Node n2 ) {
		
		if( n1.f_score < n2.f_score )
			return -1;
		else
			return 1;		
	}
	
	public boolean equals( Node n1, Node n2 ){
		if( n1.f_score == n2.f_score )
			return true;
		else
			return false;
	}
	
}
