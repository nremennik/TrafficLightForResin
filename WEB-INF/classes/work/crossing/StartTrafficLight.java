package work.crossing;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
@LocalBean
@Startup
public class StartTrafficLight
{
    private static final Logger logger = Logger.getLogger("StartTrafficLight");

    private static StartTrafficLight instance = null;

    private volatile static boolean stopTraffic = true;
    private TrafficDao trafficDao = new TrafficDao();

    public StartTrafficLight()
    {
    }

    public static StartTrafficLight getInstance()
    {
        if (instance == null)
        {
            getLogger().log(Level.INFO, "StartTrafficLight getInstance: creating new instance ");
            instance = new StartTrafficLight();
        }
        return (instance);
    }

    public void setStopTraffic(boolean stop)
    {
        this.stopTraffic = stop;
    }

    public void storePassedCar(Car car)
    {
        try
        {
            trafficDao.insertPassedCar(car);
        } catch (Exception e)
        {
            getLogger().log(Level.SEVERE, "Cannot store passed car: " + e.getMessage(), e);
        }
    }

    public List<Car> getPassedCars(LocalDateTime from, LocalDateTime to)
    {
        try
        {
            List<Car> list = trafficDao.selectPassedCars(from, to);
            return list;

        } catch (Exception e)
        {
            getLogger().log(Level.SEVERE, "Cannot store passed car: " + e.getMessage(), e);
            return null;
        }
    }


    @PostConstruct
    void initTraffic()
    {
        getLogger().log(Level.INFO, "Starting traffic");
        getInstance();

        AtomicLong carNumber = new AtomicLong(0);

        Road r1 = new Road("Road 1", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 3");
//            System.out.println("New car on road 1: " + car);
            return (car);
        });
        Road r2 = new Road("Road 2", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 4");
//            System.out.println("New car on road 2: " + car);
            return (car);
        });
        Road r3 = new Road("Road 3", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 1");
//            System.out.println("New car on road 3: " + car);
            return (car);
        });
        Road r4 = new Road("Road 4", () ->
        {
            Car car = new Car("Car-" + carNumber.incrementAndGet(), "Road 2");
//            System.out.println("New car on road 4: " + car);
            return (car);
        });

        Vector<Road> roads = new Vector<>();
        roads.add(r1);
        roads.add(r2);
        roads.add(r3);
        roads.add(r4);

        Crossing crossing = new Crossing();

        for (int i = 0; i < roads.size(); i++)
            crossing.addRoad(roads.get(i));

        crossing.setAllowedStates(new String[][]{
                {"Road 1", "Road 3"},
                {"Road 2", "Road 4"}
        });
        try
        {
            crossing.startTraffic();
            for (int i = 0; i < roads.size(); i++)
                roads.get(i).startTraffic();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        while (stopTraffic)
        {

            for (int i = 0; i < roads.size(); i++)
            {
                final Road road = roads.get(i);
                road.processArrived((arrivedCar) ->
                {
                    // Process arrived cars (insert to database)
                    trafficDao.insertPassedCar(arrivedCar);
//                    getLogger().log(Level.INFO, "Road " + road.getName() + ": " + arrivedCar);
                    return (true);
                });
            }

            if (stopTraffic == false)
            {
                try
                {
                    for (int i = 0; i < roads.size(); i++)
                        roads.get(i).stopTraffic();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            crossing.stopTraffic();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            trafficDao.closeConnection();
        }
    }

    public static Logger getLogger()
    {
        return (logger);
    }
}
