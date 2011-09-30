import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;

public class AStar {	
	
	private final int MAX_SPAWN_THREADS = 10;
	private final int MAX_NEIGHBOUR_SEARCH_THREADS = 2;
	private final int MAX_THREADS = 20;
	private final int MAX_READER_THREADS = MAX_SPAWN_THREADS;
		
	private Stack <Node> path;
	private HashSet <Node> closed_set;
	private TreeSet <Node> open_set;
	private HashMap <Node, Node> came_from;
	private Node start, goal;
	
	// The reader and the writer locks for the open_set and closed_set.
	private ReadWriteLock closed_set_lock = new ReentrantReadWriteLock();
	private Lock closed_set_read_lock = closed_set_lock.readLock();
	private Lock closed_set_write_lock = closed_set_lock.writeLock();
	
	private ReadWriteLock open_set_lock = new ReentrantReadWriteLock();
	private Lock open_set_read_lock = open_set_lock.readLock();
	private Lock open_set_write_lock = open_set_lock.writeLock();
	
	
	/* Dimensions of the map.
	 * This implementation currently only supports 2D maps. 
	 */
	private final int MAP_HEIGHT = 5, MAP_WIDTH = 5;
	
	/* The map on which the path is searched.
	 * If true, it means the node is passable, if false it is unpassable.
	 */
	private final boolean [][] map_array = new boolean[][] {
			{true, 	true, 	true, 	true, 	true},
			{true, 	false, 	false, 	false, 	true},
			{true, 	true, 	true, 	false, 	true},
			{true, 	true, 	true, 	true, 	true},
			{true, 	true, 	true, 	true, 	true},
	};
	
	
	private final AStarMap map;
		
	
	//Constructor function, providing an initial value for start and end nodes
	public AStar(Node start, Node end) {
		
		this.start = start;
		this.goal = end;

		closed_set = new HashSet <Node> ();
		open_set = new TreeSet <Node> ( new NodePriorityComparator() ) ;
		came_from = new HashMap <Node, Node> ();
		
		//Define the map in which the path is searched
		map = new AStarMap(map_array, MAP_HEIGHT, MAP_WIDTH);
		
		path = new Stack <Node> ();
		
	}

	
	public List <Node> findShortestPath( Node s, Node g ) {
		
		start = s;
		goal = g;
		
		start.g_score = 0;
		start.h_score = start.get_heuristic_score( goal );
		start.f_score = start.g_score + start.h_score;
		open_set.add(start);
		
		
		
		/* Now, we start the workers for the SpawnThread threads,
		 * as governed by MAX_SPAWN_THREADS
		 */		
		
		
		/* Keep repeating the following code section, as long as there 
		 * are nodes in the open_set. This will keep repeating, till the 
		 * goal is reached, or none of the nodes lead to the goal. 
		 */ 
		while( !open_set.isEmpty() ) {
			
			int i = 0;
			
			/* Get the threads to spawn in this iteration.
			 * This is will be less than or equal to MAX_SPAWN_THREADS
			 */ 
			//int n_spawn_threads = getNThreadsToSpawn();
			ArrayList spawn_threads = new ArrayList( getThreadsToSpawn() );
			System.out.println(" NThreadsToSpawn " + n_spawn_threads);
			
			
			// The iterator for the open_set node needs to be synchronized.
			//open_set_read_lock.lock();
			Iterator<Node> it = spawn_threads.iterator();
			List < SpawnBranchThread > workerList = new ArrayList<SpawnBranchThread>();
			
			/* Iterate through the open set to spawn the threads for 
			 * the specified number of nodes in the open_set.
			 */ 
			while( it.hasNext() && i++ < n_spawn_threads  ) {
				
				Node spawn = new Node( (Node) it.next() );
				
				
				/* If the goal has been reached, reconstruct the path,
				 * and terminate the function.
				 */ 
				if( spawn.equals( goal )) {
					reconstruct_path( came_from.get((Node) spawn)  );
					
					System.out.println("Goal reached!");
					System.out.println("Coordinates of goal: " + goal.getVector().elementAt(0) + " " + goal.getVector().elementAt(1));
					System.out.println("Coordinates of goal: " + spawn.getVector().elementAt(0) + " " + spawn.getVector().elementAt(1));
					
					return  path;
				}
					
				
				Thread worker = new SpawnBranchThread( spawn );
				worker.setName( spawn.getVector().toString() );
				worker.start();
				
				System.out.println(" Executed thread ");
				
				System.out.println(" Adding worker to list ");
				workerList.add( (SpawnBranchThread)worker );
			
			}
			
			//open_set_read_lock.unlock();
			
			// Wait for the spawned threads to terminate.
			for( int j = 0; j < workerList.size(); j++ ) {
				
				System.out.println( "Waiting"  );
				while( workerList.get(j).isAlive() ) {}
				
				System.out.println("Thread terminated ");
				
			}
			
		}
		
		System.out.println( " Done!");
		
		return null;

		
	}
	
	
	// Recursive function to reconstruct path, once the last node is obtained.
	private void reconstruct_path(Node node) {
		
		path.push(node);
		
		if( came_from.containsKey( node ))
			reconstruct_path( came_from.get( node ) );		
		
	}

	// Gets the neighbour nodes for a given node.
	public List <Node> getNeighbourNodes( Node n ) {
		return new ArrayList <Node> ( map.getNeighbourNodes( n ) );		
	}
	
	
	
	/* Finds the threads to spawn, making sure that the threads
	 * spawned are not neighbour threads.
	 */
	public List <Node> getThreadsToSpawn() {
		
		//System.out.println( " " );
		//System.out.println( " DEBUG NTHREADSTOSPAWN " );
		//System.out.println( " OPEN_SET.SIZE: " + open_set.size() );
		
		Iterator it = open_set.iterator();
		int n = 0, n2 = 0; 
		List < Vector < Integer > > list = new ArrayList();
		List <Node> node_list = new ArrayList();
	
	LOOPNSPAWN:
		while( it.hasNext() && n2 < MAX_SPAWN_THREADS ) {
			
			Node n_temp = (Node) it.next();
			Vector < Integer > v = n_temp.getVector();
			
			if( list.isEmpty() ) {
				node_list.add( n_temp );
				list.add(v);
				++n;

				//System.out.println( " Coninuining... " );
				continue;			
			}
			
			int p = 0;
			INNERFOR:
			for( Vector < Integer > v1 : list ) {
				
				//System.out.println( " Checking Vector... " );
				for( int i = 0; i < v1.size(); i++ ) {
					if( Math.abs( v.elementAt(i) -  v1.elementAt(i) ) < 2 ){
						++n2;
						++p;
						break INNERFOR;
					}
				}
			}
			if(p == 0 ) {

				++n;
				node_list.add(n_temp);
			}
		}
		

		//System.out.println( " RESULT: " + n );
		//System.out.println( "  " );
		
		return node_list;
		
	}
	
	
	
	/* Finds the number of threads to spawn, making sure that the threads
	 * spawned are not neighbour threads.
	 */
	public int getNThreadsToSpawn() {
		
		//System.out.println( " " );
		//System.out.println( " DEBUG NTHREADSTOSPAWN " );
		//System.out.println( " OPEN_SET.SIZE: " + open_set.size() );
		
		Iterator it = open_set.iterator();
		int n = 0, n2 = 0; 
		List < Vector < Integer > > list = new ArrayList();
	
	LOOPNSPAWN:
		while( it.hasNext() && n2 < MAX_SPAWN_THREADS ) {
			
			Vector < Integer > v = ((Node) it.next()).getVector();
			
			if( list.isEmpty() ) {
				list.add( v );
				++n;

				//System.out.println( " Coninuining... " );
				continue;			
			}
			
			int p = 0;
			INNERFOR:
			for( Vector < Integer > v1 : list ) {
				
				//System.out.println( " Checking Vector... " );
				for( int i = 0; i < v1.size(); i++ ) {
					if( Math.abs( v.elementAt(i) -  v1.elementAt(i) ) < 2 ){
						++n2;
						++p;
						break INNERFOR;
					}
				}
			}
			if(p == 0 )
				++n;
		}
		

		//System.out.println( " RESULT: " + n );
		//System.out.println( "  " );
		
		return n;
		
	}
	
	
	
	
	/* The thread subclass that spawns the thread, and searches the 
	 * neighbours for the current thread, estimates the scores.
	 */  
	public class SpawnBranchThread extends Thread {
		
		private final Node n;
		
		public SpawnBranchThread( Node n ) {
			this.n = n;
		}
		
		public void run() {
			
			//System.out.println("Hiiiiiiiiiiiiiiiiiiiii");
			
			//System.out.println("Lock Writer");
			open_set_write_lock.lock();
			//System.out.println("Writer locked");
				open_set.remove( n );
			//	System.out.println("Unlock Writer");
			open_set_write_lock.unlock();
			//System.out.println("Writer Unlocked");
			
			closed_set.add( n );
			
			List <Node> neighbour_nodes = getNeighbourNodes( n );
			//System.out.println( neighbour_nodes.size() );
			
			for( Node neighbour_node: neighbour_nodes ) {
				
				if( closed_set.contains( neighbour_node ))
					continue;
				
				double tentative_g_score = n.g_score + neighbour_node.distance( n );
				
				boolean tentative_is_better;
								
				if( !open_set.contains( neighbour_node ) ) {
					open_set_write_lock.lock();
						open_set.add( neighbour_node );
					open_set_write_lock.unlock();
					//System.out.println("Added neighbour node!!");
					tentative_is_better = true;
				}
				else if ( tentative_g_score < n.g_score )
					tentative_is_better = true;
				else
					tentative_is_better = false;
				
				if( tentative_is_better ) {
					came_from.put(neighbour_node, n);
					neighbour_node.g_score = tentative_g_score;
					neighbour_node.h_score = neighbour_node.get_heuristic_score( goal );
					neighbour_node.f_score = neighbour_node.g_score + neighbour_node.h_score;
				}
			}	
			
			System.out.println("Dnoe");
		
		}
		
	}

}
