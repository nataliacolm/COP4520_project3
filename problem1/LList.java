import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.*;

public class LList
{
    private static final int num_presents = 500000;
    private static AtomicInteger adder = new AtomicInteger(0);
    private static AtomicInteger to_delete = new AtomicInteger(0);
    private static LockFreeLL list = new LockFreeLL(-1, num_presents + 1);
    // private static Random rand = new Random();

    static class Servant implements Runnable
    {
        private static ArrayList<Integer> bag = new ArrayList<>();

        Servant(ArrayList<Integer> bag)
        {
            this.bag = bag;
        }

        @Override
        public void run()
        {
            while (adder.get() < num_presents + 1 && to_delete.get() < num_presents + 1)
            {
                int to_add = adder.getAndIncrement();
    
                if (to_add < num_presents)
                {
                    list.add(bag.get(to_add));
                    if (to_add == num_presents - 1)
                        System.out.println("Last item is: " + bag.get(to_add));
                }

                int val = to_delete.getAndIncrement();
                if (val < num_presents)
                {
                    int to_del = bag.get(val);
                    boolean test = false;

                    if (val == num_presents - 1)
                    System.out.println("Del " + to_del);
               
                    while (!test)
                    {
                        test = list.remove(to_del);
                    }
                }
            }

            System.out.println("Done");

            if (list.is_empty())
                System.out.println("Empty");

            else
            {
                list.traverse();
            }
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
        
        for (int i = 0; i < 4; i++)
        {
            Servant servant = new Servant(bag);
            Thread thread = new Thread(servant);
            thread.start();
        }

        System.out.println("List item: " + bag.get(499999));
    }
}