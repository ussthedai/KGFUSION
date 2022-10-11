package com.usst.kgfusion.util;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.Set;
import java.util.UUID;

/**
 * @program: kgfusion
 * @description: 图包测试
 * @author: JH_D
 * @create: 2022-01-08 17:12
 **/

public class GraphAlTest {
    public static void main(String[] args) {

        SparseGraph g = new SparseGraph();
        int num_edge = 0;
        for (int i = 1; i < 10; i++) {
            g.addVertex(i);
            if(num_edge <= (Integer.MAX_VALUE - 1))
            g.addEdge(num_edge++, 1, i + 1);
            if (i > 1 && num_edge <= (Integer.MAX_VALUE - 1)) {
                g.addEdge(num_edge++, i, i + 1, EdgeType.DIRECTED);
            }
        }
        g.addVertex(11);
        g.addVertex(12);
        g.addVertex(13);
        if(num_edge <= (Integer.MAX_VALUE - 1))
        g.addEdge(num_edge++, 11, 12);
        System.out.println("The graph g = " + g.toString());

        WeakComponentClusterer weakComponentClusterer = new WeakComponentClusterer();
//        Graph graph = new DirectedSparseGraph();
        Set<Set<Integer>> res = weakComponentClusterer.apply(g);
        System.out.println(res);
    }
}
