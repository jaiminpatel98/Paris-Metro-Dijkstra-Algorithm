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
		
		
	}

	public static Graph<Integer,Integer> getGraph() {return parisMetro;}

	public static void main(String[] args) throws Exception, IOException {
		ParisMetro metro = new ParisMetro("metro.txt");
		System.out.println(metro.getGraph());
		
	}



}