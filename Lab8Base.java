public class Lab8Base {

    class MaxHeap {
        private final int[] heap;
        private int size;
        
        MaxHeap(int capacity) {
            heap = new int[capacity];
            size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        void insert(int value) {
            int i = size;
            heap[size++] = value;
            percolateUp(i);
        }

        private void percolateUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (heap[parent] >= heap[i]) break;

                swap(parent, i);
                i = parent;
            }
        }

        int peek() {
            if (isEmpty()) throw new
                IllegalStateException("Heap kosong");
            return heap[0];
        }

        int removeRoot() {
            if (isEmpty()) throw new
                IllegalStateException("Heap kosong");

            int result = heap[0];
            size--;
            if (size > 0) {
                heap[0] = heap[size];
                percolateDown(0);
            }

            return result;
        }

        private void percolateDown(int i) {
            while (true) {
                int largest = i;
                int left = 2 * i + 1;
                int right = 2 * i + 2;
                if (left < size && heap[left] > heap[largest])
                    largest = left;
                if (right < size && heap[right] >
                    heap[largest]) largest = right;
                if (largest == i) break;
                    swap(i, largest);
                    i = largest;
            }
        }

        private void swap(int i, int j) {
            int temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }
    }

    class MinHeap {
        private final int[] heap;
        private int size;

        MinHeap(int capacity) {
        heap = new int[capacity];
        size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        void insert(int value) {
            int i = size;
            heap[size++] = value;
            percolateUp(i);
        }
        private void percolateUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (heap[parent] <= heap[i]) break;
                swap(parent, i);
                i = parent;
            }
        }

        int peek() {
            if (isEmpty()) throw new
                IllegalStateException("Heap kosong");
            return heap[0];
        }

        int removeRoot() {
            if (isEmpty()) throw new
                IllegalStateException("Heap kosong");

            int result = heap[0];
            size--;

            if (size > 0) {
                heap[0] = heap[size];
                percolateDown(0);
            }

            return result;
        }
        private void percolateDown(int i) {
            while (true) {
                int smallest = i;
                int left = 2 * i + 1;
                int right = 2 * i + 2;

                if (left < size && heap[left] <
                    heap[smallest]) smallest = left;
                if (right < size && heap[right] <
                    heap[smallest]) smallest = right;
                if (smallest == i) break;
                    swap(i, smallest);

                i = smallest;
            }
        }

        private void swap(int i, int j) {
            int temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }
    }
}
