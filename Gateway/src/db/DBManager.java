package db;

import Core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.regions.Regions;


public class DBManager {
    private static DBManager                instance = null;
    final private Gson                      gson         = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create();

    public static DBManager getInstance() {
        if(null == instance) {
            instance = new DBManager();
        }
        return instance;
    }

    private DBManager() {
        // Initialte the DynamoClient
    }

}