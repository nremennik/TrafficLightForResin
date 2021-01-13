package work.crossing;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TrafficDao extends BaseDao
{
    private final String sqlInsertPassedCar = "insert into traffic_lights\n" +
            "(car_id,arrived,crossed,source,destination,violator)\n" +
            "values (?,?,?,?,?,?)";

    private final String sqlSelectPassedCar = "select  car_id,     \n" +
            " arrived,    \n" +
            " crossed,    \n" +
            " source,     \n" +
            " destination,\n" +
            " violator\n" +
            "from   traffic_lights\n" +
            " where crossed between ? and ?";

    public List<Car> selectPassedCars(LocalDateTime from, LocalDateTime to)
    {
        List<Car> list = new ArrayList<>();
        Connection connection = getConnection();
        if (connection != null)
        {
            try
            {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectPassedCar);
                preparedStatement.setObject(1, from);
                preparedStatement.setObject(2, to);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next())
                {
                    Car car = new Car(resultSet.getString("car_id"),
                            resultSet.getString("destination"));
                    car.setArrived(resultSet.getObject("arrived", LocalDateTime.class));
                    car.setCrossed(resultSet.getObject("crossed", LocalDateTime.class));
                    car.setSource(resultSet.getString("source"));
                    car.setViolator(resultSet.getString("violator").equals("Y"));
                    list.add(car);
                }
                resultSet.close();
                preparedStatement.close();
                closeConnection();
            } catch (SQLException | RuntimeException e)
            {
                getLogger().log(Level.SEVERE, "Cannot insert car: " + e.getMessage(), e);
                return (null);

            }
        }
        return (list);
    }

    public void insertPassedCar(Car car)
    {
        Connection connection = getConnection();
        if (connection != null)
        {
            try
            {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertPassedCar);
                preparedStatement.setString(1, car.getName());
                preparedStatement.setObject(2, car.getArrived());
                preparedStatement.setObject(3, car.getCrossed());
                preparedStatement.setString(4, car.getSource());
                preparedStatement.setString(5, car.getDestination());
                preparedStatement.setString(6, car.isViolator() ? "Y" : "N");


                if (preparedStatement.executeUpdate() != 1)
                {
                    throw new RuntimeException("Cannot insert car");
                }
                connection.commit();

            } catch (SQLException | RuntimeException e)
            {
                getLogger().log(Level.SEVERE, "Cannot insert car: " + e.getMessage(), e);
            } finally
            {
                closeConnection();
            }
        }
    }
}
