// Natalia Colmenares
// COP 4520

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Temp {
    private static final int min = -100;
    private static final int max = 70;
    private static final int hours = 20;
    private static final int sensors = 8;
    private static final int num_iterations_in_hour = 480;
    private static final int num_iterations_in_10_min = 80;
    private static final int num_iterations_per_thread = 60;


    private static SensorList list = new SensorList(min, max); // include temperature range here.
    private static SensorList interval_list = new SensorList(min, max); // include temperature range here.
    private static AtomicIntegerArray intervals = new AtomicIntegerArray(480);

    private static CountDownLatch cLatch = new CountDownLatch(8);
    private static AtomicInteger index = new AtomicInteger(0);
    private static AtomicInteger ten_min = new AtomicInteger(0);

    private static Random rand_temp = new Random();
    private static AtomicBoolean ready = new AtomicBoolean();

    static class Sensor implements Runnable
    {
        private int name;
        private ThreadLocal<Integer> num_iterations = new ThreadLocal<Integer>();

        public Sensor(int name)
        {
            this.name = name;
        }

        @Override
        public void run()
        {
            num_iterations.set(0);

            while (num_iterations.get() < num_iterations_per_thread)
            {
                int curr_temp = rand_temp.nextInt((max + 1) - (min)) + min;

                // Add 80 items to represent adding to the first 10 minutes. Therefore, each thread only
                // input 10 times.
                if (num_iterations.get() < 10)
                {
                    // get first ten minutes added to linked list
                    interval_list.add(curr_temp);

                    // ensure that every thread has inputted their own readings for
                    // the 10 minutes is counted.
                    ten_min.getAndIncrement();
                }

                // Add to the other lists, but not the 10 minute window anymore, main will track that when new
                // temperature readings are added to the intervals arraylist.
                if (num_iterations.get() < num_iterations_per_thread)
                {
                    // make sure sensors add in correct position to avoid missing a minute reading
                    intervals.set(name, curr_temp);
                    this.name = name + sensors;

                    list.add(curr_temp);
                    // used to demonstrate a new temperature reading has been added.
                    index.getAndIncrement();

                }

                num_iterations.set(num_iterations.get() + 1);
            }

            ready.set(true);
            cLatch.countDown();
        }
    }

    public static void main(String [] args)
    {
        list.create_list(); // list stores info for the hour
        interval_list.create_list(); // interval list stores info for 10 minutes.
        ExecutorService pool = Executors.newFixedThreadPool(sensors);

        long startTime = System.nanoTime();
    
        for (int i = 0; i < sensors; i++)
        {
            pool.execute(new Sensor(i));
        }

        int hour = 1;
        int max = -1000; // dummy value
        int max_interval = -1000; // dummy value 
        int interval_index_list = 0;
        int time = 0;

        while (hour <= hours)
        {
            // Do productive work when the threads get a new temperature reading.
            // 80 iterations marks 10 minutes! Start looking for the largest 10 minute interval window.
            if (ten_min.get() >= num_iterations_in_10_min)
            {
                // compare first ten minutes
                if (time == 0)
                {
                    int min_temp = interval_list.find_min();
                    int max_temp = interval_list.find_max();

                    time = num_iterations_in_10_min;

                    if ((max_temp - min_temp) > max)
                    {
                        max = max_temp - min_temp;
                        max_interval = time / sensors;
                    }
                }

                // Keep removing 8 indices and adding 8 indices to update the 10 minute interval window.
                else if (time + sensors <= index.get())
                {
                    int count = 0;
                    // start removing intervals that do not work for the next iteration.
                    while (count < sensors)
                    {
                        interval_list.remove(intervals.get(interval_index_list));
                        interval_list.add(intervals.get(time));
                        interval_index_list++;
                        count++;
                        time++;
                    }

                    // Find the largest range within the window.
                    int min_temp = interval_list.find_min();
                    int max_temp = interval_list.find_max();
    
                    if ((max_temp - min_temp) > max)
                    {
                        max = max_temp - min_temp;
                        max_interval = time / sensors;
                    }
                }
            }

            if (ready.get() == true && time >= num_iterations_in_hour)
            {
                try
                {
                    cLatch.await();
                }

                catch(Exception e)
                {
                    ;
                }

                cLatch = new CountDownLatch(8);

                 // REPORT SECTION:
                System.out.println("==========TOP 5 HIGHEST TEMPS: HOUR " + hour +"============");
                list.get_top_five();
                System.out.println("==========TOP 5 LOWEST TEMPS: HOUR " + hour +"=============");
                list.get_low_five();

                System.out.println("+++++ MAX 10 MINUTE INTERVAL: " + max_interval + " - " + (max_interval - 10) + " Difference: " + max +" +++++");
                System.out.println();
                System.out.println();

                hour++;

                if (hour <= hours)
                {
                    list.cleanup();
                    max = -1000;
                    interval_index_list = 0;
                    index.set(0);
                    interval_list.cleanup();
                    ready.set(false);
                    time = 0;
                    ten_min.set(0);

                    for (int i = 0; i < sensors; i++)
                    {
                        pool.execute(new Sensor(i));
                    }
                }
            }
        }

        pool.shutdown();
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Execution time (milliseconds): " + elapsedTime / 1000000);
    }
}
