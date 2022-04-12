// Natalia Colmenares
// COP 4520

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// SensorList is a modified list that follows a hashmap <key, value>, but uses nodes.
// Therefore, inserting (updating the value) in the sensor list will never result in waiting since
// the keys are already predefined by the temperature.

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

    // Called by main thread to create the list with keys.
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

    // Helper function called by main to find the largest temperature range.
    public int find_range()
    {
        Node curr = head.next.get();
        int max = 0;
        int min = 0;
        
        while (curr.freq.get() <= 0)
        {
            curr = curr.next.get();
        }

        min = curr.key;

        curr = tail.prev.get();

        while (curr.freq.get() <= 0)
        {
            curr = curr.prev.get();
        }

        max = curr.key;

        return max - min;
    }

    public int find_max()
    {
        Node curr = tail.prev.get();
        while (curr.freq.get() <= 0)
        {
            curr = curr.prev.get();
        }

        int max = curr.key;
        return max;
    }

    public int find_min()
    {
        Node curr = head.next.get();
        while (curr.freq.get() <= 0)
        {
            curr = curr.next.get();
        }

        int min = curr.key;
        return min;
    }

    public boolean remove(int key)
    {
        // start at tail
        if (key > 0)
        {
            Node curr = tail.prev.get();

            // Item will for sure be in the list because of the range.
            while (true)
            {
                if (curr.key == key)
                {
                    curr.freq.decrementAndGet();
                    return true;
                }
    
                curr = curr.prev.get();
            }
        }

        // start at head
        else
        {
            Node curr = head.next.get();

            // Item will for sure be in the list because of the range.
            while (true)
            {
                if (curr.key == key)
                {
                    curr.freq.decrementAndGet();
                    return true;
                }
    
                curr = curr.next.get();
            }
        }
    }

    // Function called by main to get the lowest 5 temperatures in an hour.
    public void get_low_five()
    {
        /* For non-unique values uncomment the following and comment the other code portion
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
        */

        int count = 0;
        Node curr = head.next.get();

        while (count < 5 && curr != tail)
        {
            if (curr.freq.get() > 0)
            {
                System.out.println(curr.key);
                count++;
            }

            if (curr != tail)
                curr = curr.next.get();
        }

    }

    // Function called by main to get the top five temperatures in an hour. Values are unique.
    public void get_top_five()
    {
        /* For non-unique values uncomment the following and comment the other code portion
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
        */

        int count = 0;
        Node curr = tail.prev.get();

        while (count < 5 && curr != head)
        {
            if (curr.freq.get() > 0)
            {
                System.out.println(curr.key);
                count++;
            }

            if (curr != head)
                curr = curr.prev.get();
        }
    }

    // Tester function to make sure list is constructed correctly.
    public void traverse()
    {
        Node curr = tail.prev.get();

        while (curr != head)
        {
            System.out.println("Temp: " + curr.key + " Freq: " + curr.freq.get());
            curr = curr.prev.get();
        }
    }

    // Clean the id values so that the list is reusable again!
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