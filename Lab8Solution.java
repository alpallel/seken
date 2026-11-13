import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Solusi optimal Latihan Pra-Lab 8: Sistem Pelelangan Tiket Konser "Tabah".
 *
 * Menggunakan satu kelas Binary Heap berbasis array untuk antrean BID (Max Heap)
 * dan antrean OFFER (Min Heap) dengan tie-break ID terkecil.
 */
public class Lab8Solution {

    /**
     * Merepresentasikan satu pesanan beli (BID) atau jual (OFFER).
     */
    private static final class Order {
        final long id;
        final long price;
        long amount;

        Order(long id, long price, long amount) {
            this.id = id;
            this.price = price;
            this.amount = amount;
        }
    }

    /**
     * Binary Heap berbasis array untuk mengelola objek {@link Order}.
     */
    private static final class OrderHeap {
        private Order[] heap;
        private int size;
        private final boolean isMax;

        OrderHeap(boolean isMax, int initialCapacity) {
            this.isMax = isMax;
            this.heap = new Order[Math.max(1, initialCapacity)];
            this.size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        void insert(Order order) {
            if (size == heap.length) {
                grow();
            }
            int i = size;
            heap[size++] = order;
            percolateUp(i);
        }

        Order peek() {
            return size == 0 ? null : heap[0];
        }

        Order removeRoot() {
            if (isEmpty()) {
                throw new IllegalStateException("Heap kosong");
            }
            Order root = heap[0];
            size--;
            if (size > 0) {
                heap[0] = heap[size];
                heap[size] = null;
                percolateDown(0);
            } else {
                heap[0] = null;
            }
            return root;
        }

        /**
         * Menentukan apakah {@code a} harus diprioritaskan di atas {@code b}.
         * Prioritas utama pada harga (tertinggi untuk BID, terendah untuk OFFER),
         * dengan tie-break ID terkecil (pesanan yang datang lebih dahulu).
         */
        private boolean hasHigherPriority(Order a, Order b) {
            if (a.price != b.price) {
                return isMax ? a.price > b.price : a.price < b.price;
            }
            return a.id < b.id;
        }

        private void percolateUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (!hasHigherPriority(heap[i], heap[parent])) {
                    break;
                }
                swap(i, parent);
                i = parent;
            }
        }

        private void percolateDown(int i) {
            while (true) {
                int best = i;
                int left = 2 * i + 1;
                int right = 2 * i + 2;

                if (left < size && hasHigherPriority(heap[left], heap[best])) {
                    best = left;
                }
                if (right < size && hasHigherPriority(heap[right], heap[best])) {
                    best = right;
                }
                if (best == i) {
                    break;
                }
                swap(i, best);
                i = best;
            }
        }

        private void swap(int i, int j) {
            Order temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }

        private void grow() {
            Order[] next = new Order[heap.length * 2];
            System.arraycopy(heap, 0, next, 0, size);
            heap = next;
        }
    }

    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        String firstToken = in.next();
        if (firstToken == null) {
            return;
        }
        int q = Integer.parseInt(firstToken);

        OrderHeap bidHeap = new OrderHeap(true, q);
        OrderHeap offerHeap = new OrderHeap(false, q);
        long nextId = 1;

        for (int i = 0; i < q; i++) {
            String cmd = in.next();
            if ("BID".equals(cmd)) {
                long price = in.nextLong();
                long amount = in.nextLong();
                bidHeap.insert(new Order(nextId, price, amount));
                out.println(nextId++);
            } else if ("OFFER".equals(cmd)) {
                long price = in.nextLong();
                long amount = in.nextLong();
                offerHeap.insert(new Order(nextId, price, amount));
                out.println(nextId++);
            } else if ("MATCH".equals(cmd)) {
                long totalTickets = 0;
                long totalValue = 0;

                while (!bidHeap.isEmpty() && !offerHeap.isEmpty()) {
                    Order bestBid = bidHeap.peek();
                    Order bestOffer = offerHeap.peek();
                    if (bestBid.price < bestOffer.price) {
                        break;
                    }

                    long trxAmount = Math.min(bestBid.amount, bestOffer.amount);
                    totalTickets += trxAmount;
                    totalValue += trxAmount * bestOffer.price;

                    // Mengubah amount di tempat tidak merusak struktur heap karena urutan
                    // prioritas hanya bergantung pada price dan id.
                    bestBid.amount -= trxAmount;
                    bestOffer.amount -= trxAmount;

                    if (bestBid.amount == 0) {
                        bidHeap.removeRoot();
                    }
                    if (bestOffer.amount == 0) {
                        offerHeap.removeRoot();
                    }
                }

                out.print(totalTickets);
                out.print(' ');
                out.println(totalValue);
            } else if ("INFO".equals(cmd)) {
                Order bestBid = bidHeap.peek();
                Order bestOffer = offerHeap.peek();
                long bidPrice = bestBid == null ? -1 : bestBid.price;
                long offerPrice = bestOffer == null ? -1 : bestOffer.price;
                out.print(bidPrice);
                out.print(' ');
                out.println(offerPrice);
            }
        }

        out.flush();
    }

    private static final class FastReader {
        private final BufferedReader reader;
        private StringTokenizer tokenizer;

        FastReader(InputStream in) {
            this.reader = new BufferedReader(new InputStreamReader(in));
        }

        String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        return null;
                    }
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }
}
