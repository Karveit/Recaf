package me.coley.recaf;

import me.coley.recaf.graph.*;
import me.coley.recaf.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for basic graph logic.
 *
 * @author Matt
 */
public class GraphTest {
	private Graph<Integer, IVert> undirectedGraph;
	private Graph<Integer, IVert> directedGraph;

	@BeforeEach
	public void setup() {
		//   1   4
		//  / \ /
		// 2   3
		//  \ / \
		//   5   6
		IVert u1 = new IVert(1);
		IVert u2 = new IVert(2);
		IVert u3 = new IVert(3);
		IVert u4 = new IVert(4);
		IVert u5 = new IVert(5);
		IVert u6 = new IVert(6);
		u1.addEdge(u2, false);
		u1.addEdge(u3, false);
		u2.addEdge(u5, false);
		u3.addEdge(u5, false);
		u3.addEdge(u6, false);
		u4.addEdge(u3, false);
		// Specify all vertices as roots
		undirectedGraph = () -> new HashSet<>(Arrays.asList(u1, u2, u3, u4, u5, u6));
		//
		//
		//     1
		//    v v
		//   2   3   6
		//  v     v v
		// 4       5
		IVert d1 = new IVert(1);
		IVert d2 = new IVert(2);
		IVert d3 = new IVert(3);
		IVert d4 = new IVert(4);
		IVert d5 = new IVert(5);
		IVert d6 = new IVert(6);
		d1.addEdge(d2, true);
		d1.addEdge(d3, true);
		d2.addEdge(d4, true);
		d3.addEdge(d5, true);
		d6.addEdge(d5, true);
		// Specify all vertices as roots
		directedGraph = () -> new HashSet<>(Arrays.asList(d1, d2, d3, d4, d5, d6));
	}

	@Test
	public void testRootContainment() {
		// values
		for(int i = 1; i < 6; i++)
			assertTrue(undirectedGraph.containsRoot(i));
		assertFalse(undirectedGraph.containsRoot(-1));
		// vertices
		for(int i = 1; i < 6; i++)
			assertTrue(undirectedGraph.containsRoot(undirectedGraph.getRoot(i)));
	}

	@Test
	public void testUndirectedPathSearch() {
		IVert v1 = undirectedGraph.getRoot(2);
		IVert v4 = undirectedGraph.getRoot(4);
		// Use DFS to find the path between v1 and v4
		Search<Integer> search = new DepthFirstSearch<>();
		SearchResult result = search.find(v1, v4);
		assertNotNull(result);
		// Path must follow one of the given paths
		String[] paths = new String[]{"2 1 3 4", "2 5 3 4"};
		String path = result.getPath().stream()
				.map(String::valueOf)
				.collect(Collectors.joining(" ")).toString();
		assertTrue(Arrays.binarySearch(paths, path) >= 0);
	}

	@Test
	public void testDirectedPathSearch() {
		IVert v1 = directedGraph.getRoot(1);
		for(int i = 2; i < 5; i++) {
			IVert vOther = directedGraph.getRoot(i);
			// Use DFS to find the path between v1 and vOther
			Search<Integer> search = new DepthFirstSearch<>();
			SearchResult result = search.find(v1, vOther);
			assertNotNull(result);
		}
		IVert v5 = directedGraph.getRoot(5);
		for(int i = 1; i < 4; i++) {
			IVert vOther = directedGraph.getRoot(i);
			// Use DFS to ensure no path exists between v5 and vOther
			Search<Integer> search = new DepthFirstSearch<>();
			SearchResult result = search.find(v5, vOther);
			assertNull(result);
		}
	}

	@Test
	public void testDirectedChildren() {
		// Create a set of vertex names that are children of "1"
		Set<String> children = directedGraph.getRoot(1).getAllDirectedChildren(true)
				.map(String::valueOf)
				.collect(Collectors.toSet());
		// Ensure all expected vertices are in the results, all others are not in results
		for (int i = 1; i < 5; i++) {
			String num = String.valueOf(i);
			assertTrue(children.contains(num));
		}
		// [6 -> 5] thus is not accessible from branching off of "1".
		assertFalse(children.contains("6"));
	}

	@Test
	public void testDirectedParents() {
		// Create a set of vertex names that are parents of "5"
		Set<String> children = directedGraph.getRoot(5).getAllDirectedParents(true)
				.map(String::valueOf)
				.collect(Collectors.toSet());
		// Ensure all expected vertices are in the results, all others are not in results
		String[] expected = new String[]{"5", "6", "3", "1"};
		String[] unexpected = new String[]{"4", "2"};
		for(String s : expected) {
			assertTrue(children.contains(s));
		}
		for(String s : unexpected) {
			assertFalse(children.contains(s));
		}
	}

	/**
	 * By the intended design of Vertex, there is not supposed to be a "edges" field,
	 * rather it should be dynamically generated off of whatever the data is.
	 *
	 * But since we're just testing we can work with this dumb implementation.
	 */
	public static class IVert extends Vertex<Integer> {
		private final Set<Edge<Integer>> edges = new HashSet<>();
		private int data;

		public IVert(int data) {
			this.data = data;
		}

		public void addEdge(IVert other, boolean directed) {
			// Create the edge
			Edge<Integer> edge = null;
			if(directed) {
				// Directed edge
				edge = new DirectedEdge<>(IVert.this, other);
			} else {
				// Undirected edge
				edge = () -> new Pair<>(this, other);
			}
			// Add the edge
			edges.add(edge);
			other.edges.add(edge);
		}

		@Override
		public Integer getData() {
			return data;
		}

		@Override
		public void setData(Integer data) {
			this.data = data.intValue();
		}

		@Override
		public int hashCode() {
			return data;
		}

		@Override
		public boolean equals(Object other) {
			if(this == other)
				return true;
			if(getData().equals(other))
				return true;
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(data);
		}

		@Override
		public Set<Edge<Integer>> getEdges() {
			return edges;
		}
	}
}
