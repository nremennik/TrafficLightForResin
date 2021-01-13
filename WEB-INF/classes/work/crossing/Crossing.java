package work.crossing;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Crossing
{

    Map<String, Road> roads = new HashMap<>();

    // Possible states - array of roads with green light
    // Each state is an array of Roads that have green light currently set
    List<List<Road>> greenLights = new ArrayList<>();
    static final int GREEN_DURATION = 20000; // Green light duration (millis)
    static final int GREEN_DURATION_QUANTUM_DELAY = 100; // Green light duration granularity in millis
    private AtomicLong crossedGreenCars= new AtomicLong(0);

    public Crossing()
    {
    }

    public void addRoad(Road road) // Add road to the crossing
    {
        roads.put(road.getName(), road);
    }

    public long getCrossedGreenCars()
    {
        return crossedGreenCars.get();
    }

    // Set possible states by road names. Road shall be already added to the crossing
    public void setAllowedStates(String[][] roadsStates)
    {
        greenLights.clear();
        for (int i = 0; i < roadsStates.length; i++)
        {
            List<Road> state = new ArrayList<>();

            for (int j = 0; j < roadsStates[i].length; j++)
            {
                Road road = roads.get(roadsStates[i][j]);
                if (road == null)
                {
                    throw new RuntimeException("Crossing::setAllowedStates: no such road: " + roadsStates[i][j]);
                }
                state.add(road);
            }
            greenLights.add(state);
        }
    }

    Thread crossingRunner = null;

    public void startTraffic() throws InterruptedException
    {
        if (crossingRunner != null)
        {
            stopTraffic();
        }
        crossingRunner = new Thread(this::crossingProcessor);
        crossingRunner.start();
    }

    public void stopTraffic() throws InterruptedException
    {
        if (crossingRunner == null) return;
        crossingRunner.interrupt();
        crossingRunner.join(10000);
        if (crossingRunner.isAlive())
        {
            throw new RuntimeException("Fatal: crossingProcessor cannot be stopped");
        }
        crossingRunner = null;
    }

    // Traffic processor
    void crossingProcessor()
    {
        boolean stopFlag = false;

        while (!Thread.interrupted() && !stopFlag)
        {
            // Iterate through green light states
            // for (int stateNumber = 0; stateNumber < greenLights.size() && !stopFlag; stateNumber++)
            for (List<Road> currentlyGreen : greenLights)
            {
                if (stopFlag) break;

                crossedGreenCars.set(0);

                long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < GREEN_DURATION)
                {
                    // Process waiting cars
                    for (Road currentRoad : currentlyGreen)
                    {
                        Car car = currentRoad.getWaitingCar();
                        if (car != null)
                        {
                            Road destination = roads.get(car.getDestination());
                            if (destination == null)
                            {
                                // No such destination, nowhere to route
                                continue;
                            }
                            car.setCrossed(LocalDateTime.now());
                            destination.acceptArrivedCar(car);
                            crossedGreenCars.incrementAndGet();
                        }
                        else
                        {
                            // No cars in queue
                        }
                    }

                    // Process violators
                    for (Road violatorsRoad : roads.values())
                    {
                        Car car = violatorsRoad.getWaitingViolatorCar();
                        if (car != null)
                        {
                            // Process violators here. Currently it is the same as for usual cars but might be changed.
                            Road destination = roads.get(car.getDestination());
                            if (destination == null)
                            {
                                // No such destination, nowhere to route
                                continue;
                            }
                            if (currentlyGreen.contains(violatorsRoad))
                            {
                                // Violator runs on the green light
                                car.setViolator(false);
                            }
                            car.setCrossed(LocalDateTime.now());
                            destination.acceptArrivedCar(car);
                            crossedGreenCars.incrementAndGet();

                        }
                    }

                    // Define waiting strategy here:
                    long remainingTime = GREEN_DURATION - (System.currentTimeMillis() - start);
                    if (remainingTime > GREEN_DURATION_QUANTUM_DELAY)
                    {
                        remainingTime = GREEN_DURATION_QUANTUM_DELAY;
                    }
                    try
                    {
                        Thread.sleep(remainingTime);
                    } catch (InterruptedException e)
                    {
                        stopFlag = true; // To break outer loop
                        break;
                    }
                    // End waiting
                }
            }
        }
        System.out.println("Crossing processor exited");
    }

}
