package db;

import Core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
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
    final private MongoCollection<Document> workersCollection;
    final private MongoCollection<Document> mappingCollection;

    public static DBManager getInstance() {
        if(null == instance) {
            instance = new DBManager();
        }
        return instance;
    }

    private DBManager() {
        mongoClient =       new MongoClient("localhost", 27017);
        database =          mongoClient.getDatabase("storageDB");
        workersCollection = database.getCollection("workers");
        mappingCollection = database.getCollection("mappingFile");
    }

    private String processIdJsonForGson(String json) {
        return json.replaceAll("\\Q{ \"$oid\" : \"\\E([a-zA-Z0-9]+)\\Q\" }\\E", "\"$1\"");
    }

    public List<WorkerConnectionData> getWorkers() {
        return getList(null, workersCollection, WorkerConnectionData.class);
    }

    private <T> List<T> getList(String workerId, MongoCollection<Document> collection, Class<T> clasz) {
        List<T> list = new ArrayList<T>();
        MongoCursor<Document> cursor;
        if (workerId == null) {
            cursor = collection.find().iterator();
        } else {
            cursor = collection.find(eq("_id", workerId)).iterator();
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

    public boolean addWorker(int port, int heartbeatPort, String name){
        WorkerConnectionData worker = new WorkerConnectionData("127.0.0.1", port, heartbeatPort, name);
        Document doc = workersCollection.find(eq("name", worker.getName())).first();
        boolean result;
        if (doc == null) {
            doc = Document.parse(gson.toJson(worker));
            workersCollection.insertOne(doc);
            result = true;
        } else {
            result = false;
        }
        worker.setId(doc.getObjectId("_id").toString());
        return result;
    }

    public void saveMapping(String finalFilename, String workerName, Integer version) {
        MappingInfo info = new MappingInfo();
        info.setMappingId(finalFilename);
        info.setWorker(workerName);
        info.setVersion(version);

        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        obj.add(new BasicDBObject("workerName", workerName));
        obj.add(new BasicDBObject("mappingId", finalFilename));
        andQuery.put("$and", obj);

        System.out.println(andQuery.toString());

        FindIterable<Document> cursor = mappingCollection.find(andQuery);
        if (cursor.iterator().hasNext()) {
            Document thisDoc = cursor.iterator().next();
            String json = thisDoc.toJson();
            MappingInfo map = gson.fromJson(processIdJsonForGson(json), MappingInfo.class);
            if(!map.getVersion().equals(version)){
                Document doc = Document.parse(gson.toJson(info));
                Document setData = new Document();
                setData.append("workerName", workerName);
                setData.append("mappingId", finalFilename);
                setData.append("version", version);
                Document update = new Document();
                update.append("$set", setData);
                mappingCollection.updateOne(andQuery, update);
            }
        } else {
            Document doc = Document.parse(gson.toJson(info));
            mappingCollection.insertOne(doc);
        }
    }
}