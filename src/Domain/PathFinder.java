package Domain;

import java.awt.Point;
import java.util.*;

/**
 * A pathfinding utility that uses A* algorithm to find a path on a grid of walkable or blocked cells.
 */
public class PathFinder {
    /**
     * A private inner class that stores pathfinding node details (position, parent, costs).
     */
    private static class Node implements Comparable<Node> {
        Point pos;
        Node parent;
        double g; // Cost from start to this node
        double h; // Heuristic to goal

        Node(Point pos, Node parent, double g, double h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        /**
         * Calculates the sum of g and h for comparison.
         * @return the total cost so far plus the heuristic
         */
        double getF() {
            return g + h;
        }

        /**
         * Compares two nodes based on their total estimated cost F.
         * @param other another node
         * @return a negative, zero, or positive result of comparison
         */
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getF(), other.getF());
        }
    }

    /**
     * Finds a path from start to goal using A* in a boolean walkable grid.
     * @param start a Point representing the start position (row, col)
     * @param goal a Point representing the goal position (row, col)
     * @param walkable a 2D boolean array where true indicates a passable cell
     * @return a list of Points representing the path or null if none found
     */
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

            for (Point neighbor : getNeighbors(current.pos, walkable)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double newG = current.g + 1;

                Node neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, newG, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (newG < neighborNode.g) {
                    neighborNode.parent = current;
                    neighborNode.g = newG;
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }
        return null;
    }

    /**
     * A heuristic function measuring approximate distance between two points (Manhattan distance).
     * @param a the current point
     * @param b the target point
     * @return an estimated cost for traveling from a to b
     */
    private static double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Gathers the valid walkable neighboring cells for the given point.
     * @param p the point whose neighbors are evaluated
     * @param walkable a 2D boolean array
     * @return a list of Point objects representing walkable neighbors
     */
    private static List<Point> getNeighbors(Point p, boolean[][] walkable) {
        List<Point> neighbors = new ArrayList<>();
        int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};

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

    /**
     * Reconstructs the path from the goal node back to the start by following parent references.
     * @param endNode the final Node in the path
     * @return a list of Point objects from start to goal
     */
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
