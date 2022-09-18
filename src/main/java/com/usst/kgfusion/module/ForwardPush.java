package com.usst.kgfusion.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ForwardPush<T> {
    private Map<T, Map<T, Double>> outMap;
    private Map<T, Double> residue;
    private Map<T, Double> reserve;
    private Double alpha;
    private Double rsum;

    public ForwardPush(Double alpha, Double rsum, Map<T, Map<T, Double>> outMap) {
        this.outMap = outMap;
        this.alpha = alpha;
        this.rsum = rsum;
        residue = new HashMap<>();
        reserve = new HashMap<>();
    }

    public Double getWeight(T from , T to){
        if(outMap.get(from).get(to)!=null){
            return outMap.get(from).get(to);
        }
        else {
            return 0.0;
        }
    }

    public Map<T, Double> getWholeGraphPPR() {
        return reserve;
    }

    public Double getRsum() {
        return rsum;
    }

    public HashMap<T, Double> getResidueCopy() { // 拿一份复制的
        return new HashMap<>(residue);
    }

    public HashMap<T, Double> getReserveCopy() { // 拿一份复制的
        return new HashMap<>(reserve);
    }

    public void computeWholeGraphPPR(T source, Double rmax) {
        residue.clear();
        reserve.clear();
        Double rsum_local = 1.0;
        int out_size_source = 0;
        if(outMap.get(source)!=null){
            out_size_source = outMap.get(source).size();
        }
        if (out_size_source == 0){
            reserve.put(source, 1.0);
            rsum = 0.0;
            return;
        }

        Set<T> nodesInQueue = new HashSet<>();  // 记录queue
        Queue<T> queue = new ConcurrentLinkedDeque<>(); // 还能forward的node
        
        queue.offer(source);
        nodesInQueue.add(source);
        residue.put(source, 1.0);

        while(!queue.isEmpty()){
            T cur = queue.poll();
            nodesInQueue.remove(cur);
            Double cur_residue = residue.get(cur);
            residue.replace(cur, 0.0);
            
            Double old_reserve_cur = reserve.get(cur);
            if (old_reserve_cur == null){
                old_reserve_cur = 0.0;
            }
            reserve.put(cur, old_reserve_cur + cur_residue * alpha);

            rsum_local -= cur_residue * alpha;
            
            int out_size_cur = 0;
            if (outMap.get(cur) != null){
                out_size_cur = outMap.get(cur).size();
            }
            if (out_size_cur == 0){
                //Double new_residue_source = residue.get(source) + cur_residue * (1.0 - alpha);
                Double new_residue_source = residue.get(source) + cur_residue * (1.0 - alpha) * getWeight(cur,source);
                residue.put(source, new_residue_source);
                if (out_size_source > 0 && new_residue_source / (double)out_size_source >= (Double)rmax && !nodesInQueue.contains(source)){
                    queue.offer(source);
                    nodesInQueue.add(source);
                }
                continue;
            }

            //Double avg_push_residue = ((1.0 - alpha) * cur_residue) / (double)out_size_cur;
            List<T> out_neighbours = outMap.get(cur).keySet().stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            for(T neighbour: out_neighbours){
                Double old_residue_next = residue.get(neighbour);
                if(old_residue_next == null){
                    old_residue_next = 0.0;
                }
                Double new_residue_next = old_residue_next + ((1.0 - alpha) * cur_residue) *  getWeight(cur,neighbour);
                //Double new_residue_next = old_residue_next + avg_push_residue;
                residue.put(neighbour, new_residue_next);
                
                int out_size_next = 0;
                if (outMap.get(neighbour) != null){
                    out_size_next = outMap.get(neighbour).size();
                }
                if (new_residue_next / (double)out_size_next >= (Double)rmax && !nodesInQueue.contains(neighbour)){
                    queue.offer(neighbour);
                    nodesInQueue.add(neighbour);
                }
                
            }
            
            rsum = rsum_local;

        }


    }

    // public static void main(String[] args) throws Exception {
    // List<Integer> quries = new ArrayList<>();
    // BufferedReader bw = new BufferedReader(new FileReader(new
    // File("D:\\DevelopmentProgress\\Project_VSCode\\EntityTypingExp\\entitytyping\\src\\main\\java\\com\\dai\\data\\duie\\query.txt")));
    // String line = "";
    // while((line = bw.readLine()) != null){
    // quries.add(Integer.parseInt(line.strip()));
    // }
    // bw.close();
    // Graph graph = new Graph("src\\main\\java\\com\\dai\\data\\duie\\graph.txt");
    // ForwardPush fp = new ForwardPush(0.2, 1.0, graph);

    // Long start_time = System.currentTimeMillis();

    // for(Integer query: quries){
    // fp.computeWholeGraphPPR(query, 0.0001);
    // }

    // Long end_time = System.currentTimeMillis();

    // System.out.println("fora running time:" + Long.toString(end_time -
    // start_time) + "ms");

    // }

}

// class MonteCarlo {
//     private Graph graph;
//     private Double alpha;
//     private Double delta;
//     private Double pfail;

//     public MonteCarlo(Graph graph, Double alpha, Double delta, Double pfail) {
//         this.graph = graph;
//         this.alpha = alpha;
//         this.delta = delta;
//         this.pfail = pfail;
//     }

//     public Integer random_walk(Integer source) {
//         // rwr
//         int out_degree_start = graph.getOutSize(source);
//         if (out_degree_start == 0)
//             // 输入检查
//             return source;
//         int nodeIdM_cur = source; // 当前node
//         while (true) {
//             if (ThreadLocalRandom.current().nextDouble(1.0) < alpha)
//                 // Stop at current node with a probability of alpha
//                 break;

//             int out_degree_cur = graph.getOutSize(nodeIdM_cur);
//             if (out_degree_cur > 0) {
//                 // 随机挑一个out
//                 int picked_rel_num = ThreadLocalRandom.current().nextInt(out_degree_cur);
//                 nodeIdM_cur = graph.getOutVert(nodeIdM_cur, picked_rel_num);
//             } else {
//                 // 没有out，转向source
//                 nodeIdM_cur = source;
//             }
//         }
//         return nodeIdM_cur;
//     }

//     public Integer random_walk_no_zero_hop(Integer nodeId_start) {
//         int out_degree_start = graph.getOutSize(nodeId_start);
//         if (out_degree_start == 0)
//             return nodeId_start;
//         int picked_rel_num_start = ThreadLocalRandom.current().nextInt(out_degree_start);
//         int nodeIdM_cur = graph.getOutVert(nodeId_start, picked_rel_num_start); // current node

//         while (true) {
//             if (ThreadLocalRandom.current().nextDouble(1.0) < alpha)
//                 break;

//             int out_degree_cur = graph.getOutSize(nodeIdM_cur);
//             if (out_degree_cur > 0) {
//                 int picked_rel_num = ThreadLocalRandom.current().nextInt(out_degree_cur);
//                 nodeIdM_cur = graph.getOutVert(nodeIdM_cur, picked_rel_num);
//             } else {
//                 nodeIdM_cur = nodeId_start;
//             }
//         }
//         return nodeIdM_cur;
//     }
// }
