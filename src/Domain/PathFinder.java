package Domain;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class PathFinder {
    private static class Node implements Comparable<Node> {
        Point pos;
        Node parent;
        double g; // Cost from start to this node
        double h; // Heuristic (estimated cost to goal)

        Node(Point pos, Node parent, double g, double h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        double getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getF(), other.getF());
        }
    }

    public static List<Point> findPath(Point start, Point goal, boolean[][] walkable) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.pos.equals(goal)) {
                return reconstructPath(current);
            }

            closedSet.add(current.pos);

            // Check all neighbors
            for (Point neighbor : getNeighbors(current.pos, walkable)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double newG = current.g + 1; // Cost to move to neighbor

                Node neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, newG, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (newG < neighborNode.g) {
                    neighborNode.parent = current;
                    neighborNode.g = newG;
                    // Reorder in priority queue
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }

    private static double heuristic(Point a, Point b) {
        // Manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static List<Point> getNeighbors(Point p, boolean[][] walkable) {
        List<Point> neighbors = new ArrayList<>();
        int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}}; // Four directions

        for (int[] dir : dirs) {
            int newX = p.x + dir[0];
            int newY = p.y + dir[1];

            if (newX >= 0 && newX < walkable.length &&
                    newY >= 0 && newY < walkable[0].length &&
                    walkable[newX][newY]) {
                neighbors.add(new Point(newX, newY));
            }
        }

        return neighbors;
    }

    private static List<Point> reconstructPath(Node endNode) {
        List<Point> path = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            path.add(0, current.pos);
            current = current.parent;
        }

        return path;
    }
}