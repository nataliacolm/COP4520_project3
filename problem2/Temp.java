// Natalia Colmenares
// COP 4520

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Temp {
    private static final int min = -100;
    private static final int max = 70;
    private static final int hours = 4;
    private static final int iterations = hours * 60; // 60 iterations for 1 hour. Consider 1 iteration a minute.
    private static final int sensors = 8;

    private static SensorList list = new SensorList(min, max); // include temperature range here.
    private static SensorList interval_list = new SensorList(min, max); // include temperature range here.
    private static AtomicIntegerArray intervals = new AtomicIntegerArray(480);

    private static AtomicInteger num_iterations = new AtomicInteger(0);
    private static CountDownLatch cLatch = new CountDownLatch(8);
    private static AtomicInteger index = new AtomicInteger(0);

    private static Random rand_temp = new Random();
    private static volatile boolean ready = false;

    static class Sensor implements Runnable
    {
        // Each sensor has a state to "synchronize" them when a minute is reached.
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

                int curr_temp = rand_temp.nextInt((max + 1) - (min)) + min;

                list.add(curr_temp); // Look at the add method in SensorList to see why a thread will not be delayed here!
                interval_list.add(curr_temp);
                // Keep track of numbers added to interval list
                intervals.set(index.getAndIncrement(), curr_temp);
                cLatch.countDown();
            }
        }
    }

    public static void main(String [] args)
    {
        ArrayList<Sensor> arr_list = new ArrayList<>();
        list.create_list(); // list stores info for the hour
        interval_list.create_list(); // interval list stores info for 10 minutes.
    
        for (int i = 0; i < sensors; i++)
        {
            Sensor sensor = new Sensor();
            arr_list.add(sensor);
            Thread thread = new Thread(sensor, Integer.toString(i)); // give the name of the thread as index
            thread.start();
        }

        int hour = 1;
        int max = -1000; // dummy value
        int max_interval = -1000; // dummy value 
        int num_iteration_in_hour = 0;
        int interval_index_list = 0;

        while (num_iterations.get() < iterations)
        {
            // attempt to synchronize threads
            for (int i = 0; i < sensors; i++)
            {
                Sensor temp = arr_list.get(i);
                temp.state = true;
            }

            if (num_iteration_in_hour == 9)
            {
                int min_temp = interval_list.find_min();
                int max_temp = interval_list.find_max();

                if ((max_temp - min_temp) > max)
                {
                    max = max_temp - min_temp;
                    max_interval = num_iteration_in_hour + 1;
                }
            }

            else if (num_iteration_in_hour >= 10)
            {
                int count = 0;
                // start removing intervals that do not work for the next iteration.
                while (count < 8)
                {
                    interval_list.remove(intervals.get(interval_index_list));
                    interval_index_list++;
                    count++;
                }
                int min_temp = interval_list.find_min();
                int max_temp = interval_list.find_max();

                if ((max_temp - min_temp) > max)
                {
                    max = max_temp - min_temp;
                    max_interval = num_iteration_in_hour + 1;
                }
            }
  
            num_iterations.getAndIncrement();
            num_iteration_in_hour++;


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

            if (num_iterations.get() % 60 == 0)
            {
                 // REPORT SECTION:
                System.out.println("==========TOP 5 HIGHEST TEMPS: HOUR " + hour +"============");
                list.get_top_five();
                System.out.println("==========TOP 5 LOWEST TEMPS: HOUR " + hour +"=============");
                list.get_low_five();

                System.out.println("+++++ MAX 10 MINUTE INTERVAL: " + max_interval + "-" + (max_interval - 10) + " Difference: " + max +" +++++");
                System.out.println();
                System.out.println();

                list.cleanup();
                hour++;
                max = -1000;
                num_iteration_in_hour = 0;
                interval_index_list = 0;
                index.set(0);
                interval_list.cleanup();
            }
           
        }

        ready = true;
    }
}
