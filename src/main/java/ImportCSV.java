import com.google.gson.Gson;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ImportCSV {
    public static void main(String[] args) {
        String[] redisHost = args[0].split(":");
        String csvFile = args[1];
        String redis_hash_map_name = args[2];
        Scanner scanner = new Scanner(csvFile);
        String[] header = null;
        JedisPooled jedis = new JedisPooled(redisHost[0], Integer.parseInt(redisHost[1]));
        Gson gson = new Gson();
        int count = 0;
        while(scanner.hasNext()) {
            String [] row = scanner.nextLine().split(",");
            if(header == null) {
                header = row;
            } else {
                Map<String, String> data = new HashMap<>();

                for(int i = 0; i < header.length;i++) {
                    data.put(header[i], row[i]);
                }
                jedis.hset(redis_hash_map_name + ":" + row[0], data);
                count++;
                if(count%1000 == 0) {
                    System.out.println("Inserted " + count + " records");
                }
            }
        }
        System.out.println("Finished, total " + count + " records");
    }
}
