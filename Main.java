public class Main {
	
	public static void main ( String[] args ) {
		
		Integer aa = 0;
		Integer bb = 4;
		Node start = new Node(aa, aa, true);
		Node end = new Node(bb, bb, true);
		AStar a = new AStar( start, end );
		
		a.findShortestPath(start, end);
		
	}

}
