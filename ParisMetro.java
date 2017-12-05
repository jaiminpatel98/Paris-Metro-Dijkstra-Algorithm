import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;

import java.util.*;
import net.datastructures.*;

/**
 * All code used to construct data structures is from the Net folder in this directory.
 * The Net folder is used from CSI 2110 Lab 9 module, which sources the code as coming from 
 * Goodrich et al.
*/

public class ParisMetro {

	static Vertex[] vertexList = new Vertex[376]; // Will store all Metro Station vertices in order from 000 to 375; 
	static Graph<Integer, Integer> parisMetro; // Stores the Graph with Stations Numbers as vertices and time between vertices as edge weightings

	public ParisMetro(String fileName) throws Exception, IOException{
		parisMetro = new AdjacencyMapGraph<Integer, Integer>(true);
		readMetro(fileName); // Converts fileName from ParisMetro parameter into an Array of Station vertices and a Graph of the Metro system
	}

	public static Graph<Integer,Integer> getGraph() {return parisMetro;} // Returns Graph containing Metro system

	public static Vertex[] getVertexList() {return vertexList;} // Returns array of Station vertices in order from 000 to 375

	public static void readMetro(String fileName) throws Exception, IOException {
		BufferedReader metroFile = new BufferedReader(new FileReader(fileName)); // New BufferReader that reads Class fileName parameter
		String stationData = metroFile.readLine(); // Skips first line that states total number of vertices and edges in the Metro system
		stationData = metroFile.readLine();
		int i = 0; // Used to iterate through the array of vertices
		while (!stationData.equals("$")) { // Stops when file is done listing the Station Numbers
			String[] indivStationData = stationData.split(" "); // Seperates the line into the Station Number and Station Name
			int stationNum = Integer.parseInt(indivStationData[0]);
			Vertex newVertex = parisMetro.insertVertex(stationNum); // Creates vertex out of Station Number on current line
			vertexList[i] = newVertex; // Adds vertex to the vertexList
			stationData = metroFile.readLine(); // Goes to next line
			i++;
		}

		stationData = metroFile.readLine(); // Skips line with '$'

		// Inputs Vertices into a Graph and connects them by their common weighted edge
		while (stationData != null) { // Stops at the end of the file
			String[] pathData = stationData.split(" "); // Splits line into source Station, destination Station, and time (weighted edge)

			int sourceStation = Integer.parseInt(pathData[0]); 
			int destStation = Integer.parseInt(pathData[1]);
			int pathTime = Integer.parseInt(pathData[2]);

			if (pathTime == -1) { // If the weighted edge is -1 it indicated it is a walking edge and we give it a walking constant of 90
				pathTime = 90;
			}

			parisMetro.insertEdge(vertexList[sourceStation], vertexList[destStation], pathTime); // Inserts weighted edge connected by two vertices into Graph

			stationData = metroFile.readLine(); // Goes to the next line in file
		}

	}

	public static LinkedQueue<Vertex<Integer>> sameLine(Vertex<Integer> v) { // Returns a LinkedQueue containing all vertices on the same line as parameter vertex
		LinkedStack<Vertex<Integer>> stack = new LinkedStack<Vertex<Integer>>(); // Will contain a vertex at a time to traverse along a line
		HashSet visited = new HashSet(); // Will contain visted vertices as to be sure not to visit them again
		Edge[] originalEdges = new Edge[2]; // Contains the one or two edges of parameter vertex that are not weighted as 90
		LinkedQueue<Vertex<Integer>> onLine = new LinkedQueue<Vertex<Integer>>(); // Will contain all vertices on the same line as parameter vertex
		onLine.enqueue(v);
		visited.add(v);
		Iterable<Edge<Integer>> edgeList = parisMetro.outgoingEdges(v); // Gets outgoing edges of parameter vertex as a Iterable
		int index = 0; 

		for (Edge<Integer> e : edgeList) { // Looks for outgoing edges of parameter vertex that are not walking edges
			if (e.getElement() != 90) {
				originalEdges[index] = e;
				index++;
			}
		}

		Vertex<Integer> nextStation = parisMetro.opposite(v, originalEdges[0]); // Sets next station to visit
		visited.add(nextStation); // Becomes 'visited' vertex
		stack.push(nextStation); // Pushes on to stack

		while (!stack.isEmpty()) { // Traverses line in one direction and adds each vertex visited to visited and onLine, then stops when it reaches an endpoint of the line
			nextStation = stack.pop();
			Iterable<Edge<Integer>> outgoingEdges = parisMetro.outgoingEdges(nextStation);
			for (Edge<Integer> e : outgoingEdges) { // Iterates through nextStation's outgoing edges 
				if (e.getElement() != 90 && !visited.contains(parisMetro.opposite(nextStation, e))) { // Looks for edges that are not walking and that HashSet visited doesn't contain
					nextStation = parisMetro.opposite(nextStation, e);
					visited.add(nextStation);
					stack.push(nextStation);
					onLine.enqueue(nextStation);
					break;
				}
			}
		} 

		if (originalEdges[1] != null) { // Checks if parameter vertex is a line endpoint, if not it continues to check the other direction from the initial vertex
			nextStation = parisMetro.opposite(v, originalEdges[1]);
			visited.add(nextStation); // Becomes 'visited' vertex
			stack.push(nextStation); // Pushes on to stack
			while (!stack.isEmpty()) { // Traverses line in one direction and adds each vertex visited to visited and onLine, then stops when it reaches an endpoint of the line
				nextStation = stack.pop();
				Iterable<Edge<Integer>> outgoingEdges = parisMetro.outgoingEdges(nextStation);
				for (Edge<Integer> e : outgoingEdges) { // Iterates through nextStation's outgoing edges
					if (e.getElement() != 90 && !visited.contains(parisMetro.opposite(nextStation, e))) {
						nextStation = parisMetro.opposite(nextStation, e); // Looks for edges that are not walking and that HashSet visited doesn't contain
						visited.add(nextStation);
						stack.push(nextStation);
						onLine.enqueue(nextStation);
						break;
					}
				}
			}
			return onLine;
		} else { // If parameter vertex is an endpoint, then return LinkedQueue onLine
			return onLine;
		}

		
	}
	// Code taken from GraphAlgoritm class in Net folde from Lab 9
	public static LinkedStack<Integer> shortestTimeToDestination(Vertex<Integer> src, Vertex<Integer> dest) {
		ProbeHashMap<Vertex<Integer>, Vertex<Integer>> previousVisit = new ProbeHashMap<>(); // Key is the current vertex and value is its previous vertex on the shortest route from source
		LinkedStack<Integer> shortestPath = new LinkedStack<Integer>(); // will contain the shortest path from src to dest with src being on top and dest being at the bottom
		// d.get(v) is upper bound on distance from src to v
		ProbeHashMap<Vertex<Integer>, Integer> d = new ProbeHashMap<>();
		// map reachable v to its d value
		ProbeHashMap<Vertex<Integer>, Integer> cloud = new ProbeHashMap<>();
		// pq will have vertices as elements, with d.get(v) as key
		AdaptablePriorityQueue<Integer, Vertex<Integer>> pq;
		pq = new HeapAdaptablePriorityQueue<>();
		// maps from vertex to its pq locator
		ProbeHashMap<Vertex<Integer>, Entry<Integer,Vertex<Integer>>> pqTokens;
		pqTokens = new ProbeHashMap<>();

		// for each vertex v of the graph, add an entry to the priority queue, with
		// the source having distance 0 and all others having infinite distance
		for (Vertex<Integer> v : parisMetro.vertices()) {
			if (v == src)
				d.put(v,0);
			else
				d.put(v, Integer.MAX_VALUE);
			pqTokens.put(v, pq.insert(d.get(v), v));       // save entry for future updates
		}
		// now begin adding reachable vertices to the cloud 
		while (!pq.isEmpty()) {
			Entry<Integer, Vertex<Integer>> entry = pq.removeMin();
			int key = entry.getKey();
			Vertex<Integer> u = entry.getValue();
			cloud.put(u, key);                             // this is actual distance to u
			pqTokens.remove(u);    			               // u is no longer in pq
			for (Edge<Integer> e : parisMetro.outgoingEdges(u)) {
				Vertex<Integer> v = parisMetro.opposite(u,e);
				if (cloud.get(v) == null) {
					// perform relaxation step on edge (u,v)
					int wgt = e.getElement();
					if (d.get(u) + wgt < d.get(v)) {              // better path to v?
						previousVisit.put(v, u);
						d.put(v, d.get(u) + wgt);                   // update the distance
						if (v == dest) { // if current vertex is dest halt process to find its shortest path
							int time = d.get(u) + wgt; // store time for ease of access later
							shortestPath.push(v.getElement()); // pushes current/dest vertex element into the stack so that it will be popped out last
							while (v != src) { // Following through a previous vertices until it gets to the src vertex
								shortestPath.push(previousVisit.get(v).getElement());
								v = previousVisit.get(v);
							} // End result is a stack with in order of shortest path from src to dest
							shortestPath.push(time); // Push time onto the top of stack to easily get from main
							return shortestPath; // 
						}
						pq.replaceKey(pqTokens.get(v), d.get(v));   // update the pq entry
					}
				}
			}
		}


		return shortestPath;         // Safety return
	}

	public static void closeLine (Vertex<Integer> v) { // Takes paramter vertex and removes every vertex on the same line
		LinkedQueue<Vertex<Integer>> line = sameLine(v); // Gets all vertices on the same line as parameter vertex v
		while(!line.isEmpty()) { // Iterates through LinkedQueue line and removes each vertex from the Graph
			Vertex<Integer> vertex = line.dequeue();
			parisMetro.removeVertex(vertex);
		}
	}

	public static void printLine(Vertex<Integer> v) { // Prints the line that parameter vertex is on, in a legible manner
		LinkedQueue<Vertex<Integer>> queue = sameLine(v);
		while (!queue.isEmpty()) {
			System.out.print(queue.dequeue().getElement() + " ");
		}
		System.out.println();
	}

	public static void printStack(LinkedStack<Integer> stack) { // Prints a inputted stack of Station Numerbs in a legible order 
		while (!stack.isEmpty()) {
			System.out.print(stack.pop() + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) throws Exception, IOException {
		int argsLength = args.length;

		ParisMetro metro = new ParisMetro("metro.txt");
		Vertex[] list = metro.getVertexList();
		
		if (argsLength == 1) {
			int N1 = Integer.parseInt(args[0]);

			System.out.println("Test --------------------");

			System.out.println("	Input: ");
			System.out.println("	N1 = " + N1);
			System.out.println("	Output: ");
			System.out.print("	Path: ");
			printLine(list[N1]);

			System.out.println("End of Test -------------");
		} else if (argsLength == 2) {
			int N1 = Integer.parseInt(args[0]);
			int N2 = Integer.parseInt(args[1]);

			System.out.println("Test --------------------");

			System.out.println("	Input: ");
			System.out.println("	N1 = " + N1 + " N2 = " + N2);
			System.out.println("	Output: ");
			LinkedStack<Integer> stack = shortestTimeToDestination(list[N1], list[N2]);
			System.out.println("	Time: " + stack.pop());
			System.out.print("	Path: ");
			printStack(stack);

			System.out.println("End of Test -------------");
		} else if (argsLength == 3) {
			int N1 = Integer.parseInt(args[0]);
			int N2 = Integer.parseInt(args[1]);
			int N3 = Integer.parseInt(args[2]);

			System.out.println("Test --------------------");

			System.out.println("	Input: ");
			System.out.println("	N1 = " + N1 + " N2 = " + N2);
			System.out.println("	Output: ");
			LinkedStack<Integer> stack = shortestTimeToDestination(list[N1], list[N2]);
			System.out.println("	Time: " + stack.pop());
			System.out.print("	Path: ");
			printStack(stack);

			System.out.println("	Input: ");
			System.out.println("	N1 = " + N1 + " N2 = " + N2 + " N3 = " + N3);
			System.out.println("	Output: ");
			closeLine(list[N3]);
			LinkedStack<Integer> stack2 = shortestTimeToDestination(list[N1], list[N2]);
			System.out.println("	Time: " + stack2.pop());
			System.out.print("	Path: ");
			printStack(stack2);

			System.out.println("End of Test -------------");
		}
	}
}
