import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class SensorList
{
    public Node head;
    public Node tail;

    public SensorList(int min, int max)
    {
        head = new Node(min);
        tail = new Node(max);
        head.next = new AtomicMarkableReference<SensorList.Node>(tail, false);
    }

    class Node
    {
        public int key;
        public AtomicInteger freq = new AtomicInteger(0);
        public AtomicReference<Node> next;


        Node(int id)
        {
            this.key = id;
            this.next = new AtomicReference<Node>(null);
        }
    }

    public boolean add(int key)
    {
        Node pred = head;
        Node curr = head;

        // Item will for sure be in the list because of the range.
        while (true)
        {
            if (curr.key == key)
            {
                curr.freq.incrementAndGet();
                return true;
            }

            curr = curr.next;
        }
    }

    public void cleanup(int key)
    {
        boolean snip;

        while (curr.next.getReference() != tail)
        {
           curr.freq.set(0);
           curr = curr.next;
        }
    }
}