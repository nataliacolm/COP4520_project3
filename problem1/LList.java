// Natalia Colmenares
// COP 4520

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.*;

public class LList
{
    private static final int num_presents = 500000;
    private static final int num_servants = 4;
    private static volatile boolean end = false;
    private static AtomicInteger adder = new AtomicInteger(0);
    private static AtomicInteger to_delete = new AtomicInteger(0);
    private static Random rand = new Random();
    public static CountDownLatch latch = new CountDownLatch(4);

    static class Servant implements Runnable
    {
        private ArrayList<Integer> bag = new ArrayList<>();
        private LockFreeLL list;

        Servant(ArrayList<Integer> bag, LockFreeLL list)
        {
            this.bag = bag;
            this.list = list;
        }

        @Override
        public void run()
        {
           while (true)
            {
                int to_add = adder.getAndIncrement();
    
                if (to_add < num_presents)
                {
                    list.add(bag.get(to_add));
                }
                
                int val = to_delete.getAndIncrement();

                if (val < num_presents)
                {
                    int to_del = bag.get(val);
                    boolean test = false;

                    // Make sure the delete is successful
                    while (!test)
                    {
                        test = list.remove(to_del);
                    }
                }

                // Check random contains
                int answer = rand.nextInt(num_servants - 0 + 1) + 0;

                if (answer == 0)
                {
                    answer = rand.nextInt(num_presents - 0 + 1) + 0;
                    list.contains(answer);
                }


                if (adder.get() >= num_presents && to_delete.get() >= num_presents)
                {
                    break;
                }
            }

            list.remove(bag.get(num_presents - 2));
            
            if (list.is_empty())
            {
                end = true;
            }
        
            /*
            // Test Portion to make sure all thank you notes were written.
            else
            {
                list.traverse();
                System.out.println("Delete: " + to_delete.get());
            }
            */
            
            
        }
    }

    static class Gift
    {
        Integer id;

        Gift(Integer n)
        {
            this.id = n;
        }

        public Integer get_id()
        {
            return this.id;
        }
    }
    public static void main(String [] args)
    {
        ArrayList<Integer> bag = new ArrayList<>();

        for (int i = 0; i < num_presents; i++)
        {
            bag.add(i);
        }

        Collections.shuffle(bag);
        LockFreeLL list = new LockFreeLL(-1, num_presents + 1);
        
        for (int i = 0; i < num_servants; i++)
        {
            Servant servant = new Servant(bag, list);
            Thread thread = new Thread(servant);
            thread.start();
        }

        
        boolean temp = false;
        while (!temp)
        {
            if (end)
            {
                System.out.println("The Chain of Presents is Now Empty!");
                temp = true;
            }
        }
        
        // Sanity check
        // System.out.println("List item: " + bag.get(499999));
    }
}