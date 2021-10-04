package by.dkozyrev.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//DB configuration class
public enum Config {
    INSTANCE;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    Config(){
        loadProperties();
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    //Method loads properties from dbProperties.properties file
    private void loadProperties(){
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("dbProperties.properties")){
            Properties properties = new Properties();
            properties.load(in);
            dbUrl = properties.getProperty("dbUrl");
            dbUser = properties.getProperty("dbUser");
            dbPassword = properties.getProperty("dbPassword");
        } catch (IOException ignored){ }
    }
}
