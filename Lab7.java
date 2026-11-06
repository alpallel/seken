import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Lab7 {
    private static InputReader in;
    private static PrintWriter out;

    // priority[id] = current priority of server id.
    private static int[] priority;

    // Free servers are kept in a max-heap ordered by (priority desc, id asc).
    // freePos[id] is the index of server id inside freeHeap, or -1 when busy.
    private static int[] freeHeap;
    private static int[] freePos;
    private static int freeSize;

    // Busy servers are kept in a min-heap ordered by (finishTime asc, id asc).
    private static int[] busyId;
    private static long[] busyFinish;
    private static int busySize;

    private static int serverCount;

    public static void main(String[] args) {
        in = new InputReader(System.in);
        out = new PrintWriter(System.out);

        int n = in.nextInt();
        int[] initialPriority = new int[n];
        for (int i = 0; i < n; i++) {
            initialPriority[i] = in.nextInt();
        }
        int m = in.nextInt();

        int maxServers = n + m + 1; // at most one new server per query
        priority = new int[maxServers];
        freeHeap = new int[maxServers];
        freePos = new int[maxServers];
        Arrays.fill(freePos, -1);

        for (int i = 0; i < n; i++) {
            priority[i] = initialPriority[i];
            pushFree(i);
        }
        serverCount = n;

        // Each A query adds exactly one server to the busy heap.
        busyId = new int[m + 1];
        busyFinish = new long[m + 1];

        for (int q = 0; q < m; q++) {
            int time = in.nextInt();
            char type = in.next().charAt(0);

            releaseFinishedServers(time);

            if (type == 'A') {
                int processTime = in.nextInt();
                if (freeSize == 0) {
                    out.println(-1);
                } else {
                    int server = popFree();
                    pushBusy(server, (long) time + processTime);
                    out.println(server);
                }
            } else if (type == 'N') {
                int p = in.nextInt();
                int id = serverCount++;
                priority[id] = p;
                pushFree(id);
                out.println(id);
            } else {
                int server = in.nextInt();
                int p = in.nextInt();

                if (freePos[server] == -1) {
                    // Busy: only remember the new priority for when it finishes.
                    priority[server] = p;
                    out.println(1);
                } else {
                    updateFreePriority(server, p);
                    out.println(0);
                }
            }
        }

        out.flush();
    }

    private static void releaseFinishedServers(int time) {
        while (busySize > 0 && busyFinish[0] <= time) {
            int server = busyId[0];
            popBusy();
            pushFree(server);
        }
    }

    // ---------- Free-server max-heap (indexed) ----------

    // True when server a must be closer to the root than server b.
    private static boolean higher(int a, int b) {
        if (priority[a] != priority[b]) {
            return priority[a] > priority[b];
        }
        return a < b;
    }

    private static void pushFree(int id) {
        int i = freeSize++;
        freeHeap[i] = id;
        freePos[id] = i;
        siftUpFree(i);
    }

    private static int popFree() {
        int id = freeHeap[0];
        freePos[id] = -1;

        freeSize--;
        if (freeSize > 0) {
            int last = freeHeap[freeSize];
            freeHeap[0] = last;
            freePos[last] = 0;
            siftDownFree(0);
        }
        return id;
    }

    private static void updateFreePriority(int id, int newPriority) {
        int oldPriority = priority[id];
        priority[id] = newPriority;

        int i = freePos[id];
        if (newPriority > oldPriority) {
            siftUpFree(i);
        } else if (newPriority < oldPriority) {
            siftDownFree(i);
        }
    }

    private static void siftUpFree(int i) {
        while (i > 0) {
            int parent = (i - 1) >>> 1;
            if (!higher(freeHeap[i], freeHeap[parent])) {
                break;
            }
            swapFree(i, parent);
            i = parent;
        }
    }

    private static void siftDownFree(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int best = i;

            if (left < freeSize && higher(freeHeap[left], freeHeap[best])) {
                best = left;
            }
            if (right < freeSize && higher(freeHeap[right], freeHeap[best])) {
                best = right;
            }
            if (best == i) {
                break;
            }
            swapFree(i, best);
            i = best;
        }
    }

    private static void swapFree(int i, int j) {
        int a = freeHeap[i];
        int b = freeHeap[j];

        freeHeap[i] = b;
        freePos[b] = i;
        freeHeap[j] = a;
        freePos[a] = j;
    }

    // ---------- Busy-server min-heap ----------

    private static boolean earlierFinish(int a, int b) {
        if (busyFinish[a] != busyFinish[b]) {
            return busyFinish[a] < busyFinish[b];
        }
        return busyId[a] < busyId[b];
    }

    private static void pushBusy(int id, long finish) {
        int i = busySize++;
        busyId[i] = id;
        busyFinish[i] = finish;

        while (i > 0) {
            int parent = (i - 1) >>> 1;
            if (!earlierFinish(i, parent)) {
                break;
            }
            swapBusy(i, parent);
            i = parent;
        }
    }

    private static void popBusy() {
        busySize--;
        if (busySize > 0) {
            busyId[0] = busyId[busySize];
            busyFinish[0] = busyFinish[busySize];
            siftDownBusy(0);
        }
    }

    private static void siftDownBusy(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int best = i;

            if (left < busySize && earlierFinish(left, best)) {
                best = left;
            }
            if (right < busySize && earlierFinish(right, best)) {
                best = right;
            }
            if (best == i) {
                break;
            }
            swapBusy(i, best);
            i = best;
        }
    }

    private static void swapBusy(int i, int j) {
        int tmpId = busyId[i];
        busyId[i] = busyId[j];
        busyId[j] = tmpId;

        long tmpFinish = busyFinish[i];
        busyFinish[i] = busyFinish[j];
        busyFinish[j] = tmpFinish;
    }

    private static class InputReader {
        private final BufferedReader reader;
        private StringTokenizer tokenizer;

        InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 1 << 16);
        }

        String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
    }
}
