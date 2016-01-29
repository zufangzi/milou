package com.dingding.milou.dbunit;

import java.sql.SQLException;

import org.dbunit.database.IDatabaseConnection;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class DatabaseConnections {

    private final String[] names;

    private final IDatabaseConnection[] connections;

    public DatabaseConnections(String[] names, IDatabaseConnection[] connections) {
        Assert.notEmpty(names, "Names must not be empty");
        Assert.notEmpty(connections, "Connections must not be empty");
        Assert.isTrue(names.length == connections.length, "Names and Connections must have the same length");
        this.names = names;
        this.connections = connections;
    }

    public void closeAll() throws SQLException {
        for (IDatabaseConnection connection : this.connections) {
            connection.close();
        }
    }

    public IDatabaseConnection get(String name) {
        if (!StringUtils.hasLength(name)) {
            return this.connections[0];
        }
        for (int i = 0; i < this.names.length; i++) {
            if (this.names[i].equals(name)) {
                return this.connections[i];
            }
        }
        throw new IllegalStateException("Unable to find connection named " + name);
    }

}
