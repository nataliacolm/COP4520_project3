import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Temp {

    private static SensorList list = new SensorList(-100, 70); // include temperature range here.
    private static SensorList interval_list = new SensorList(-100, 70); // include temperature range here.

    private static final int iterations = 240; // 60 iterations for 1 hour. Consider 1 iteration a minute.
    private static AtomicInteger num_iterations = new AtomicInteger(0);
    private static CountDownLatch cLatch = new CountDownLatch(8);

    private static Random rand_temp = new Random();
    private static volatile boolean ready = false;

    static class Sensor implements Runnable
    {
        public volatile boolean state = false;

        @Override
        public void run()
        {
            while (!ready)
            {
                // wait until it marks a minute
                while(!state)
                {
                    if (ready)
                        return;
                }

                // If a thread reaches here, a minute is up and it is time to add it to the shared memory space.
                state = false;

                int curr_temp = rand_temp.nextInt(71 - (-100)) - 100;

                list.add(curr_temp); // Look at the add method in SensorList to see why a thread will not be delayed here!
                interval_list.add(curr_temp);

                cLatch.countDown();
            }
        }
    }

    public static void main(String [] args)
    {
        ArrayList<Sensor> arr_list = new ArrayList<>();
        list.create_list();
        interval_list.create_list();
        int max = -1000;
        int max_interval = 0;

        for (int i = 0; i < 8; i++)
        {
            Sensor sensor = new Sensor();
            arr_list.add(sensor);
            Thread thread = new Thread(sensor, Integer.toString(i)); // give the name of the thread as index
            thread.start();
        }

        int hour = 1;

        while (num_iterations.get() < iterations)
        {
            for (int i = 0; i < 8; i++)
            {
                Sensor temp = arr_list.get(i);
                temp.state = true;
            }
  
            num_iterations.getAndIncrement();

            // Make sure sensors give time at the correct minute by synchronizing them with a count down!
            try
            {
                cLatch.await();
            }

            catch(Exception e)
            {
                ;
            }

            cLatch = new CountDownLatch(8);

            if (num_iterations.get() % 10 == 0)
            {
                int range = interval_list.find_range();
                if (max < range)
                {
                    max = range;
                    max_interval = num_iterations.get() / 10;
                }
                interval_list.cleanup();
            }

            if (num_iterations.get() % 60 == 0)
            {
                 // REPORT SECTION:
                System.out.println("==========TOP 5 HIGHEST TEMPS: HOUR " + hour +"============");
                list.get_top_five();
                System.out.println("==========TOP 5 LOWEST TEMPS: HOUR " + hour +"=============");
                list.get_low_five();
                System.out.println("+++++ MAX INTERVAL: " + max_interval + "++++++++");
                list.cleanup();
                hour++;

                max = -1000;
            }
        }

        ready = true;
    }
}
