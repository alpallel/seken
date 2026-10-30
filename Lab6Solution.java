import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Lab 6 - MotoGP Tier List
 *
 * Struktur data: AVL Tree (self-balancing BST).
 * Urutan "lebih kuat" dibuat seolah-olah menjadi nilai lebih kecil,
 * sehingga rider terkuat selalu berada di subtree kiri.
 *
 * Perbandingan:
 *   - Tier S < A < B < C < D < E < F (S paling kuat).
 *   - Tier sama: battle point lebih besar = lebih kuat.
 *   - Rider yang lebih kuat diletakkan di kiri.
 */
public class Lab6Solution {

    private static final char[] TIER_CHAR = {'S', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final class Node {
        int tier;
        int bp;
        int height;
        Node left;
        Node right;
        Node parent;

        Node(int tier, int bp) {
            this.tier = tier;
            this.bp = bp;
            this.height = 1;
        }
    }

    private static int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private static void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * Negatif jika (t1, b1) lebih kuat dari (t2, b2).
     * Itulah sebabnya rider terkuat masuk ke subtree kiri.
     */
    private static int compare(int t1, int b1, int t2, int b2) {
        if (t1 != t2) {
            return t1 - t2;
        }
        return b2 - b1; // battle point lebih besar = lebih kuat = lebih kecil
    }

    private static Node rotateRight(Node y) {
        Node x = y.left;
        Node middle = x.right;
        Node parent = y.parent;

        x.right = y;
        y.parent = x;

        y.left = middle;
        if (middle != null) {
            middle.parent = y;
        }

        x.parent = parent;

        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private static Node rotateLeft(Node x) {
        Node y = x.right;
        Node middle = y.left;
        Node parent = x.parent;

        y.left = x;
        x.parent = y;

        x.right = middle;
        if (middle != null) {
            middle.parent = x;
        }

        y.parent = parent;

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private static Node rebalance(Node node) {
        updateHeight(node);
        int balance = height(node.right) - height(node.left);

        if (balance > 1) {
            if (height(node.right.left) > height(node.right.right)) {
                node.right = rotateRight(node.right);
                node.right.parent = node;
            }
            return rotateLeft(node);
        }

        if (balance < -1) {
            if (height(node.left.right) > height(node.left.left)) {
                node.left = rotateLeft(node.left);
                node.left.parent = node;
            }
            return rotateRight(node);
        }

        return node;
    }

    private static Node insert(Node node, Node newNode) {
        if (node == null) {
            return newNode;
        }

        int cmp = compare(newNode.tier, newNode.bp, node.tier, node.bp);
        if (cmp < 0) {
            node.left = insert(node.left, newNode);
            node.left.parent = node;
        } else if (cmp > 0) {
            node.right = insert(node.right, newNode);
            node.right.parent = node;
        }

        return rebalance(node);
    }

    private static Node leftmost(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private static Node rightmost(Node node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    /** Rider berikutnya yang lebih lemah (in-order successor). */
    private static Node successor(Node node) {
        if (node.right != null) {
            return leftmost(node.right);
        }

        Node parent = node.parent;
        while (parent != null && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    /** Rider berikutnya yang lebih kuat (in-order predecessor). */
    private static Node predecessor(Node node) {
        if (node.left != null) {
            return rightmost(node.left);
        }

        Node parent = node.parent;
        while (parent != null && node == parent.left) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private static Node find(Node node, int tier, int bp) {
        while (node != null) {
            int cmp = compare(tier, bp, node.tier, node.bp);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return node;
            }
        }
        return null;
    }

    /**
     * Hapus rider. Jika punya dua anak, posisinya digantikan oleh
     * rider terkuat yang masih lebih lemah (successor = leftmost right subtree).
     */
    private static Node delete(Node node, int tier, int bp) {
        if (node == null) {
            return null;
        }

        int cmp = compare(tier, bp, node.tier, node.bp);
        if (cmp < 0) {
            node.left = delete(node.left, tier, bp);
            if (node.left != null) {
                node.left.parent = node;
            }
        } else if (cmp > 0) {
            node.right = delete(node.right, tier, bp);
            if (node.right != null) {
                node.right.parent = node;
            }
        } else {
            if (node.left == null) {
                Node child = node.right;
                if (child != null) {
                    child.parent = node.parent;
                }
                return child;
            }

            if (node.right == null) {
                Node child = node.left;
                if (child != null) {
                    child.parent = node.parent;
                }
                return child;
            }

            Node replacement = leftmost(node.right);
            node.tier = replacement.tier;
            node.bp = replacement.bp;

            node.right = delete(node.right, replacement.tier, replacement.bp);
            if (node.right != null) {
                node.right.parent = node;
            }
        }

        return rebalance(node);
    }

    private static int tierOf(char c) {
        return c == 'S' ? 0 : c - 'A' + 1;
    }

    private static void printRider(PrintWriter out, Node node) {
        out.print(TIER_CHAR[node.tier]);
        out.print(' ');
        out.println(node.bp);
    }

    private static void appendRider(StringBuilder sb, Node node) {
        sb.append(TIER_CHAR[node.tier]).append(' ').append(node.bp);
    }

    public static void main(String[] args) {
        FastScanner in = new FastScanner(System.in);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        int n = in.nextInt();
        Node root = null;

        for (int i = 0; i < n; i++) {
            char tierChar = in.nextChar();
            int bp = in.nextInt();
            root = insert(root, new Node(tierOf(tierChar), bp));
        }
        if (root != null) {
            root.parent = null;
        }

        int q = in.nextInt();
        StringBuilder sb = new StringBuilder(64);

        for (int i = 0; i < q; i++) {
            char operation = in.nextChar();

            if (operation == 'G') {
                char tierChar = in.nextChar();
                int bp = in.nextInt();

                Node newNode = new Node(tierOf(tierChar), bp);
                root = insert(root, newNode);
                root.parent = null;

                if (newNode.parent == null) {
                    printRider(out, newNode);
                } else {
                    printRider(out, newNode.parent);
                }
            } else if (operation == 'R') {
                char tierChar = in.nextChar();
                int bp = in.nextInt();

                Node target = find(root, tierOf(tierChar), bp);
                if (target == null) {
                    out.println("-1");
                } else {
                    printRider(out, target);
                    root = delete(root, target.tier, target.bp);
                    if (root != null) {
                        root.parent = null;
                    }
                }
            } else if (operation == 'T') {
                if (root == null) {
                    out.println("-1");
                    continue;
                }

                sb.setLength(0);

                Node current = leftmost(root);
                int count = 0;
                while (current != null && count < 3) {
                    if (count > 0) {
                        sb.append(' ');
                    }
                    appendRider(sb, current);
                    current = successor(current);
                    count++;
                }

                sb.append(" | ");

                current = rightmost(root);
                count = 0;
                while (current != null && count < 3) {
                    if (count > 0) {
                        sb.append(' ');
                    }
                    appendRider(sb, current);
                    current = predecessor(current);
                    count++;
                }

                out.println(sb);
            }
        }

        out.flush();
    }

    private static final class FastScanner {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int pointer = 0;
        private int length = 0;

        FastScanner(InputStream in) {
            this.in = in;
        }

        private int read() {
            if (pointer >= length) {
                try {
                    length = in.read(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                pointer = 0;
                if (length <= 0) {
                    return -1;
                }
            }
            return buffer[pointer++];
        }

        char nextChar() {
            int c;
            do {
                c = read();
            } while (c != -1 && c <= ' ');
            return (char) c;
        }

        int nextInt() {
            int c;
            do {
                c = read();
            } while (c != -1 && c <= ' ');

            boolean negative = false;
            if (c == '-') {
                negative = true;
                c = read();
            }

            int value = 0;
            while (c > ' ') {
                value = value * 10 + (c - '0');
                c = read();
            }

            return negative ? -value : value;
        }
    }
}
