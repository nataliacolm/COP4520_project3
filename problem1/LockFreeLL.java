// Natalia Colmenares
// COP 4520

// This Lock free linked list implementation is from the book The Art of Multiprocessor Programming.
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeLL
{
    public Node head;
    public Node tail;

    public LockFreeLL(int min, int max)
    {
        head = new Node(min);
        tail = new Node(max);
        head.next = new AtomicMarkableReference<LockFreeLL.Node>(tail, false);
    }

    class Window 
    {
        public Node pred, curr;

         Window(Node myPred, Node myCurr) 
         {
            pred = myPred; curr = myCurr;
        } 
    }

    class Node
    {
        public int key;
        public AtomicMarkableReference<Node> next;

        Node(int id)
        {
            this.key = id;
            this.next = new AtomicMarkableReference<Node>(null, false);
        }
    }

    public Window find(Node head, int key)
    {
        Node pred = null, curr = null, succ = null;
        boolean [] marked = {false};
        boolean snip;

        if (head.next.getReference() == tail)
            return new Window(head, tail);

        retry: while (true)
        {
            pred = head;
            curr = pred.next.getReference();
            while (true)
            {
                succ = curr.next.get(marked);
                while (marked[0])
                {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry;
                    curr = succ;
                    succ = curr.next.get(marked);
                }

                if (curr == tail || curr.key >= key)
                    return new Window(pred, curr);
                
                    pred = curr;
                    curr = succ;
            }
        }
    }

    public boolean contains(int key)
    {
        boolean [] marked = {false};
        Node curr = head;

        while (curr.key < key)
        {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }

        return (curr.key == key && !marked[0]);
    }

    public boolean add(int key)
    {
        Node search = head;
        while (true)
        {
            Window window = find(search, key);
            Node pred = window.pred, curr = window.curr;
            
            if (curr.key == key)
            {
                return false;
            }

            else
            {
                Node node = new Node(key);
                node.next = new AtomicMarkableReference<>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false))
                    return true;
            }
        }
    }

    public boolean remove(int key)
    {
        boolean snip;

        while (true)
        {
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;
            if (curr.key != key)
            {
                return false;
            }

            else
            {
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if (!snip)
                    continue;
                pred.next.compareAndSet(curr, succ, false, false);
                //System.out.println(key);
                return true;
            }
        }
    }

    public void traverse()
    {
        Node curr = head;

        while (curr != tail)
        {
            curr = curr.next.getReference();
            System.out.println("curr: " + curr.key);
        }
    }

    public boolean is_empty()
    {
        return head.next.getReference() == tail;
    }



}