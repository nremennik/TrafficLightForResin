package work.crossing;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;


public class BaseDao
{
    private Connection connection = null;
    private static Logger logger=Logger.getLogger("BaseDao");

    public Connection getConnection()
    {
        try
        {
            if (connection == null)
            {
                InitialContext initialContext = new InitialContext();
                Context context = (Context) initialContext.lookup("java:comp/env");
                DataSource dataSource = (DataSource) context.lookup("jdbc/domdb");
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
            }
        } catch (NamingException | SQLException e)
        {
            getLogger().log(Level.SEVERE, "Cannot get db connection: " + e.getMessage(), e);
        }

        return (connection);
    }

    public void closeConnection()
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException e)
            {
                getLogger().log(Level.SEVERE, "Cannot close db connection: " + e.getMessage(), e);
            }
            connection = null;
        }
    }

    public static Logger getLogger()
    {
        return (logger);
    }
}
