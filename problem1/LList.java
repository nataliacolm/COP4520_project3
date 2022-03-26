import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

// order to delete gifts in will be 1,2,3,4...

public class LList
{
    private static final int num_presents = 500000;
    // private static ConcurrentLinkedQueue<Gift> list = new ConcurrentLinkedQueue<>();
    private static AtomicInteger adder = new AtomicInteger(1);
    private static AtomicInteger to_delete = new AtomicInteger(1);
    private static LockFreeLL list = new LockFreeLL(0, num_presents + 1);

    static class Servant implements Runnable
    {
        @Override
        public void run()
        {
            while (adder.get() <= 500000 && to_delete.get() <= 500000)
            {
                // keep trying to delete the gift until possible.
                list.add(adder.getAndIncrement());
                // System.out.println("Test");
                boolean test = false;
                int del = to_delete.getAndIncrement();
                while (!test)
                {
                    test = list.remove(del);
                }

                // System.out.println(t.get_id());
            }

            System.out.println("Done");
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
        for (int i = 0; i < 4; i++)
        {
            Servant servant = new Servant();
            Thread thread = new Thread(servant);
            thread.start();
        }
    }
}