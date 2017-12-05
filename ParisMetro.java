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

	static Vertex[] vertexList = new Vertex[376];
	static Graph<Integer, Integer> parisMetro;

	public ParisMetro(String fileName) throws Exception, IOException{
		parisMetro = new AdjacencyMapGraph<Integer, Integer>(true);
		readMetro(fileName);
	}

	public static Graph<Integer,Integer> getGraph() {return parisMetro;}

	public static Vertex[] getVertexList() {return vertexList;}

	public static void readMetro(String fileName) throws Exception, IOException {
		BufferedReader metroFile = new BufferedReader(new FileReader(fileName));
		String stationData = metroFile.readLine();
		stationData = metroFile.readLine();
		int i = 0;
		while (!stationData.equals("$")) {
			String[] indivStationData = stationData.split(" ");
			int stationNum = Integer.parseInt(indivStationData[0]);
			Vertex newVertex = parisMetro.insertVertex(stationNum);
			vertexList[i] = newVertex;
			stationData = metroFile.readLine();
			i++;
		}

		stationData = metroFile.readLine();

		while (stationData != null) {
			String[] pathData = stationData.split(" ");

			int sourceStation = Integer.parseInt(pathData[0]);
			int destStation = Integer.parseInt(pathData[1]);
			int pathTime = Integer.parseInt(pathData[2]);

			if (pathTime == -1) {
				pathTime = 90;
			}

			parisMetro.insertEdge(vertexList[sourceStation], vertexList[destStation], pathTime);

			stationData = metroFile.readLine();
		}

	}

	public static LinkedQueue<Vertex<Integer>> sameLine(Vertex<Integer> v) {
		LinkedStack<Vertex<Integer>> stack = new LinkedStack<Vertex<Integer>>();
		HashSet visited = new HashSet();
		Edge[] originalEdges = new Edge[2];
		LinkedQueue<Vertex<Integer>> onLine = new LinkedQueue<Vertex<Integer>>();
		onLine.enqueue(v);
		visited.add(v);
		Iterable<Edge<Integer>> edgeList = parisMetro.outgoingEdges(v);
		int index = 0; 

		for (Edge<Integer> e : edgeList) {
			if (e.getElement() != 90) {
				originalEdges[index] = e;
				index++;
			}
		}

		Vertex<Integer> nextStation = parisMetro.opposite(v, originalEdges[0]);
		visited.add(nextStation);
		stack.push(nextStation);

		while (!stack.isEmpty()) {
			nextStation = stack.pop();
			Iterable<Edge<Integer>> outgoingEdges = parisMetro.outgoingEdges(nextStation);
			for (Edge<Integer> e : outgoingEdges) {
				if (e.getElement() != 90 && !visited.contains(parisMetro.opposite(nextStation, e))) {
					nextStation = parisMetro.opposite(nextStation, e);
					visited.add(nextStation);
					stack.push(nextStation);
					onLine.enqueue(nextStation);
					break;
				}
			}
		} 

		if (originalEdges[1] != null) {
			nextStation = parisMetro.opposite(v, originalEdges[1]);
			visited.add(nextStation);
			stack.push(nextStation);
			while (!stack.isEmpty()) {
				nextStation = stack.pop();
				Iterable<Edge<Integer>> outgoingEdges = parisMetro.outgoingEdges(nextStation);
				for (Edge<Integer> e : outgoingEdges) {
					if (e.getElement() != 90 && !visited.contains(parisMetro.opposite(nextStation, e))) {
						nextStation = parisMetro.opposite(nextStation, e);
						visited.add(nextStation);
						stack.push(nextStation);
						onLine.enqueue(nextStation);
						break;
					}
				}
			}
			return onLine;
		} else {
			return onLine;
		}

		
	}

	public static LinkedStack<Integer> shortestTimeToDestination(Vertex<Integer> src, Vertex<Integer> dest) {
		Map<Vertex<Integer>, Vertex<Integer>> previousVisit = new ProbeHashMap<>();
		LinkedStack<Integer> shortestPath = new LinkedStack<Integer>();
		// d.get(v) is upper bound on distance from src to v
		Map<Vertex<Integer>, Integer> d = new ProbeHashMap<>();
		// map reachable v to its d value
		Map<Vertex<Integer>, Integer> cloud = new ProbeHashMap<>();
		// pq will have vertices as elements, with d.get(v) as key
		AdaptablePriorityQueue<Integer, Vertex<Integer>> pq;
		pq = new HeapAdaptablePriorityQueue<>();
		// maps from vertex to its pq locator
		Map<Vertex<Integer>, Entry<Integer,Vertex<Integer>>> pqTokens;
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
						if (v == dest) {
							int time = d.get(u) + wgt;
							shortestPath.push(v.getElement());
							while (v != src) {
								shortestPath.push(previousVisit.get(v).getElement());
								v = previousVisit.get(v);
							}
							shortestPath.push(time);
							return shortestPath;
						}
						pq.replaceKey(pqTokens.get(v), d.get(v));   // update the pq entry
					}
				}
			}
		}


		return shortestPath;         // this only includes reachable vertices
	}

	public static void closeLine (Vertex<Integer> v) {
		LinkedQueue<Vertex<Integer>> line = sameLine(v);
		while(!line.isEmpty()) {
			Vertex<Integer> vertex = line.dequeue();
			parisMetro.removeVertex(vertex);
		}
	}

	public static void printLine(Vertex<Integer> v) {
		LinkedQueue<Vertex<Integer>> queue = sameLine(v);
		while (!queue.isEmpty()) {
			System.out.print(queue.dequeue().getElement() + " ");
		}
		System.out.println();
	}

	public static void printStack(LinkedStack<Integer> stack) {
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