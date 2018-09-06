/**
 * Distributed System Project 1
 * Liping Zhang, ID:1016954
 */
package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import javax.net.ServerSocketFactory;


public class serverLogic {

    public int port=3000;
    public int threadLimit=3;
    public String dictPath="/Users/baby/IdeaProjects/DSproject1/src/server/dictionary.json";

    ServerSocketFactory factory;
    ServerSocket server;
    Boolean isRunning=false;
    ExecutorService pool;

    private Map<String,String> dictionary;
    ObjectMapper mapper;

    public serverLogic() {
        init();
    }

    private void init() {
        mapper = new ObjectMapper();
        /**
         * Read JSON from a file into a Map
         */
        try {
            dictionary = mapper.readValue(new File(
                    dictPath), new TypeReference<Map<String, String>>() {});

        } catch (JsonMappingException e) {
            e.printStackTrace();
            printLog(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            printLog(e.getMessage());
        }
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }


    public void tryStartServer(String p,String l,String d){

        if(!p.isEmpty() & isNumeric(p) & !l.isEmpty() & isNumeric(l)) {
            port = Integer.parseInt(p);
            threadLimit=Integer.parseInt(l);
            dictPath=d;

            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    startServer();
                }
            });
            t.start();
        }
        else
            printLog("Please input valid port or thread limit numbers");
    }

    public void startServer(){

        pool=newFixedThreadPool(threadLimit);

        factory = ServerSocketFactory.getDefault();
        try{
            server = factory.createServerSocket(port);
            isRunning=true;
            printLog("Starting server at port "+port+", "+threadLimit+" threads");
            printLog("Waiting for client connection..");

            // Wait for connections.
            while(true){
                Socket client = server.accept();
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        serveClient(client);
                    }
                });

                pool.execute(t);
            }

        } catch (IOException e) {
            printLog(e.getMessage());
            e.printStackTrace();
        }
    }



    public void stopServer(){
        if(isRunning){
            try {
                server.close();
                printLog("Stop server at port "+port);
                pool.shutdown();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else{
            printLog("Server is not running ");
        }
    }

    private void serveClient(Socket client){
        try{

            printLog(Thread.currentThread().getName()+" is running");

            DataInputStream input=new DataInputStream(client.getInputStream());
            DataOutputStream output=new DataOutputStream(client.getOutputStream());
            JsonMessage data=parseJsonString(input.readUTF());
            switch (data.flag){
                case "0":
                    output.writeUTF(query(data.username,data.word));
                    break;
                case "1":
                    output.writeUTF(add(data.username,data.word,data.meaning));
                    break;
                case "2":
                    output.writeUTF(delete(data.username,data.word));
                    break;
            }
            input.close();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String query(String u,String w){
        printLog("User "+u+" searches for word "+w);
        String result;
        String q=w.toUpperCase();
        if(dictionary.containsKey(q)){
            result=dictionary.get(q);
        }
        else{
            result="";
        }
        return result;

    }

    public synchronized String add(String u,String w,String m){
        printLog("User "+u+" adds word "+w);
        String q=w.toUpperCase();
        if(dictionary.containsKey(q)){
            return "1";
        }
        else{
            dictionary.put(q,m);
            updateDict();
        }
        return "0";
    }

    public synchronized String delete(String u,String w){
        printLog("User "+u+" deletes word "+w);
        String q=w.toUpperCase();
        if(!dictionary.containsKey(q)){
            printLog("fail");
            return "1";
        }
        else{
            dictionary.remove(q);
            printLog("success");
            updateDict();
        }
        return "0";
    }

    public synchronized void updateDict(){

        /**
         * Convert Map to JSON and write to a file
         */
        try {
            mapper.writeValue(new File(dictPath), dictionary);
            printLog("Dictionary updated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric (String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public class JsonMessage{
        public String username;
        public String word;
        public String flag;
        public String meaning;
    }

    public JsonMessage parseJsonString(String s){
        Gson gson=new Gson();
        JsonMessage m=gson.fromJson(s,JsonMessage.class);
//        printLog(m.username+m.word+m.flag+m.meaning);
        return m;
    }

    public void printLog(String m) {}

}
