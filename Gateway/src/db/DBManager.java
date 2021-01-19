package db;

import Core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

public class DBManager {
    private static DBManager                instance = null;
    final private Gson                      gson         = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create();
    final private MongoClient               mongoClient;
    final private MongoDatabase             database;
    final private MongoCollection<Document> usersCollection;
    final private MongoCollection<Document> workersCollection;
    final private MongoCollection<Document> mappingCollection;

    public static DBManager getInstance() {
        if(null == instance) {
            instance = new DBManager();
        }
        return instance;
    }

    private DBManager() {
        // Initialte the DynamoClient
        mongoClient =       new MongoClient("localhost", 27017);
        database =          mongoClient.getDatabase("storageDB");

        usersCollection =   database.getCollection("users");
        workersCollection = database.getCollection("workers");
        mappingCollection = database.getCollection("mappingFile");
    }

    public boolean addUser(User user) {
        Document doc = usersCollection.find(eq("username", user.getUsername())).first();
        boolean result;
        if (doc == null) {
            doc = Document.parse(gson.toJson(user));
            usersCollection.insertOne(doc);
            result = true;
        } else {
            result = false;
        }
        user.setId(doc.getObjectId("_id").toString());
        return result;
    }

    public User getUser(String username) {
        Document doc = usersCollection.find(eq("username", username)).first();
        if (doc == null) {
            return null;
        } else {
            return gson.fromJson(processIdJsonForGson(doc.toJson()), User.class);
        }
    }

    public WorkerMinimalConnData getWorker(String name) {
        Document doc = workersCollection.find(eq("name", name)).first();
        if (doc == null) {
            return null;
        } else {
            return gson.fromJson(processIdJsonForGson(doc.toJson()), WorkerMinimalConnData.class);
        }
    }

    private String processIdJsonForGson(String json) {
        return json.replaceAll("\\Q{ \"$oid\" : \"\\E([a-zA-Z0-9]+)\\Q\" }\\E", "\"$1\"");
    }

    public List<WorkerConnectionData> getWorkers() {
        return getList(null, workersCollection, WorkerConnectionData.class, "_id");
    }

    public List<WorkerMinimalConnData> getMinimalWorkers() {
        return getList(null, workersCollection, WorkerMinimalConnData.class, "_id");
    }

    private <T> List<T> getList(String workerId, MongoCollection<Document> collection, Class<T> clasz, String fieldName) {
        List<T> list = new ArrayList<T>();
        MongoCursor<Document> cursor;
        if (workerId == null) {
            cursor = collection.find().iterator();
        } else {
            cursor = collection.find(eq(fieldName, workerId)).iterator();
        }
        try {
            while (cursor.hasNext()) {
                String json = cursor.next().toJson();
                list.add(gson.fromJson(processIdJsonForGson(json), clasz));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private <T> List<T> getListStartsWith(String fieldPattern, MongoCollection<Document> collection, Class<T> clasz, String fieldName) {
        List<T> list = new ArrayList<T>();
        MongoCursor<Document> cursor;
        if (fieldPattern == null) {
            cursor = collection.find().iterator();
        } else {
            cursor = collection.find(regex(fieldName, fieldPattern, "i")).iterator();
        }
        try {
            while (cursor.hasNext()) {
                String json = cursor.next().toJson();
                list.add(gson.fromJson(processIdJsonForGson(json), clasz));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public void updateWorkerHeartbeat(String info, String workerName) {
        Gson gson = new GsonBuilder().create();
        HeartbeatInfo heartbeatInfo = gson.fromJson(info, HeartbeatInfo.class);

        Document query = new Document();
        query.append("name", workerName);
        Document setData = new Document();
        setData.append("CPU", heartbeatInfo.getCPU());
        setData.append("memory", heartbeatInfo.getMemory());
        setData.append("storage", heartbeatInfo.getOcupiedStorage());

        Document update = new Document();
        update.append("$set", setData);
        //To update single Document
        workersCollection.updateOne(query, update);
    }

    public void deleteWorker(String name){
        workersCollection.deleteOne(Filters.eq("name", name));
    }

    public List<String> getWorkerNamesForFile(String mappingId) {
        List<MappingInfo> mapping = getList(mappingId, mappingCollection, MappingInfo.class, "mappingId");
        List<String> workers = new ArrayList<>();
        for(MappingInfo map : mapping) {
            workers.add(map.getWorker());
        }
        return  workers;
    }

    public List<MappingInfo> getWorkersForFile(String mappingId) {
        return getList(mappingId, mappingCollection, MappingInfo.class, "mappingId");
    }

    public List<String> getUsedWorkerNames(String username) {
        List<MappingInfo> mapping = getListStartsWith(username + "_.*", mappingCollection, MappingInfo.class, "mappingId");
        List<String> workers = new ArrayList<>();
        for(MappingInfo map : mapping) {
            workers.add(map.getWorker());
            System.out.println("Used worker for " + username + ": " + map.getWorker());
        }
        return  workers;
    }

    public List<MappingInfo> getUsedWorkers(String username) {
        return getListStartsWith(username + "_.*", mappingCollection, MappingInfo.class, "mappingId");
    }
}