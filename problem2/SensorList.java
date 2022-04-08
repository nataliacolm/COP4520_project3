import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SensorList
{
    public Node head;
    public Node tail;

    private int min_temp;
    private int max_temp;

    public SensorList(int min, int max)
    {
        min_temp = min;
        max_temp = max;
        head = new Node(min - 1);
        tail = new Node(max + 1);
        head.next.set(tail);
    }

    class Node
    {
        public int key;
        public AtomicInteger freq = new AtomicInteger(0);
        public AtomicReference<Node> next;
        public AtomicReference<Node> prev;



        Node(int id)
        {
            this.key = id;
            this.next = new AtomicReference<Node>(null);
            this.prev = new AtomicReference<Node>(null);
        }
    }

    public void create_list()
    {
        int start = min_temp;
        int end = max_temp;

        Node curr = head;
        Node prev = curr;

        while (start <= end)
        {
            Node newNode = new Node(start);
            curr.next.set(newNode);
            curr = curr.next.get();
            curr.prev.set(prev);
            prev = curr;
            start++;
        }

        curr.next.set(tail);
        tail.prev.set(curr);
    }

    public boolean add(int key)
    {
        Node curr = head.next.get();

        // Item will for sure be in the list because of the range.
        while (true)
        {
            if (curr.key == key)
            {
                curr.freq.incrementAndGet();
                return true;
            }

            curr = curr.next.get();
        }
    }

    public int find_range()
    {
        Node curr = head.next.get();
        int max = 0;
        int min = 0;
        
        while (curr.freq.get() <= 0)
        {
            curr = curr.next.get();
        }

        max = curr.key;

        curr = tail.prev.get();
        while (curr.freq.get() <= 0)
        {
            curr = curr.prev.get();
        }

        min = curr.key;

        return max - min;
    }

    public void get_low_five()
    {
        int count = 0;
        Node curr = head.next.get();

        while (count < 5 && curr != tail)
        {
            int temp = 0;
            while (temp < curr.freq.get())
            {
                temp++;
                if (temp + count > 5)
                    return;
                
                System.out.println(curr.key);
            }

            count = temp + count;

            if (curr != tail)
                curr = curr.next.get();
        }
    }

    public void get_top_five()
    {
        int count = 0;
        Node curr = tail.prev.get();

        while (count < 5 && curr != head)
        {
            int temp = 0;
            while (temp < curr.freq.get())
            {
                temp++;

                if (count + temp > 5)
                    return;
                
                System.out.println(curr.key);
            }

            count = temp + count;

            if (curr != head)
                curr = curr.prev.get();
        }
    }

    public void traverse()
    {
        Node curr = tail.prev.get();

        while (curr != head)
        {
            System.out.println("Temp: " + curr.key + " Freq: " + curr.freq.get());
            curr = curr.prev.get();
        }
    }

    public void cleanup()
    {
        Node curr = head.next.get();

        while (curr != tail)
        {
           curr.freq.set(0);
           curr = curr.next.get();
        }
    }
}