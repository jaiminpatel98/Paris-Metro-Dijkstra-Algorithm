import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;

import java.util.*;
import net.datastructures.Graph;
import net.datastructures.Vertex;
import net.datastructures.Edge;
import net.datastructures.AdjacencyMapGraph;
import net.datastructures.Map;
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

	public static void sameLine(Vertex<Integer> v) {
		LinkedStack<Vertex<Integer>> stack = new LinkedStack<Vertex<Integer>>();
		Edge[] originalEdges = new Edge[2];
		Iterable<Edge<Integer>> edgeList = parisMetro.outgoingEdges(v);
		int index = 0;
		boolean flag = false;
		for (Edge<Integer> e : edgeList) {
			if (e.getElement() != (Integer)90) {
				originalEdges[index] = e;
				index++;
			}
		}
		
		Vertex<Integer> vTemp = parisMetro.opposite(v, originalEdges[0]);
		stack.push(vTemp);
		Object edgeValue = originalEdges[0].getElement();
		
		Integer nextValue = 0;
		
		while (!stack.isEmpty()) {
			int count = 0;
			edgeList = parisMetro.outgoingEdges(vTemp);
			for (Edge<Integer> e : edgeList) {
				Integer eValue = e.getElement();
				if (eValue != (Integer)90) {
					if (eValue != edgeValue) {
						nextValue = eValue;
						Integer vv = vTemp.getElement();
						// System.out.println(vTemp.getElement());
						vTemp = parisMetro.opposite(vTemp, e);
						Integer vw = vTemp.getElement();
						stack.push(vTemp);
						count = 1;
					}

				}
			}
			
			edgeValue = nextValue;
			
			if (count == 0) {
				// System.out.println(vTemp.getElement());
				while (!stack.isEmpty()) {
					Vertex vPrint = stack.pop();
					System.out.println(vPrint.getElement());
				}
			}
		}

		if (originalEdges[1] == null) {
			System.out.println(v.getElement());
		} else {
			vTemp = parisMetro.opposite(v, originalEdges[1]);
			stack.push(vTemp);
			edgeValue = originalEdges[1].getElement();

			nextValue = 0;
			while(!stack.isEmpty()) {
				int count = 0;
				edgeList = parisMetro.outgoingEdges(vTemp);
				for (Edge<Integer> e : edgeList) {
					Integer eValue = e.getElement();
					if (eValue != (Integer)90) {
						if (eValue != edgeValue) {
							nextValue = eValue;
							vTemp = parisMetro.opposite(vTemp, e);
							stack.push(vTemp);
							count = 1;
						}
					}
				}

				edgeValue = nextValue;

				if (count == 0) {
					// System.out.println(vTemp.getElement());
					while (!stack.isEmpty()) {
						Vertex vPrint = stack.pop();
						System.out.println(vPrint.getElement());
					}
				}
			}
			System.out.println(v.getElement());
		}
		
	}

	public static void main(String[] args) throws Exception, IOException {
		ParisMetro metro = new ParisMetro("metro.txt");
		Vertex[] list = metro.getVertexList();
		sameLine(list[114]);
		// System.out.println(metro.getGraph());
		
	}



}