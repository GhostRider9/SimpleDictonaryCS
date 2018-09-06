/**
 * Distributed System Project 1
 * Liping Zhang, ID:1016954
 */

package client;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;


public class clientWindow {

    // IP and port
    private static String ip = "localhost";
    private static int port = 3000;
    private String userName="User";

    private JTextField textField;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton addButton;
    private JPanel mainWindow;
    private JScrollPane JScroll;
    private JTextArea textArea;
    private JTextField portField;
    private JTextField usernameField;
    private JButton startButton;

    private String inputWord;
    private Boolean isEdit=false;

    private String[] flag={"0","1","2"}; // flag for search, add, delete



    public static void main(String[] args) {
        JFrame frame = new JFrame("Dictionary Client");
        frame.setContentPane(new clientWindow().mainWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public clientWindow() {

        usernameField.setText(userName);
        portField.setText(""+port);
        initStatus();

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputWord=textField.getText();
                if(!inputWord.isEmpty()){
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getMeaning();
                        }
                    });
                    t.start();
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!isEdit) {
                    String s = (String) JOptionPane.showInputDialog(
                            JOptionPane.getRootFrame(),
                            "Please input the word you want to add",
                            "Add word",
                            JOptionPane.PLAIN_MESSAGE);
                    if ((s != null) && (s.length() < 1)) {
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Input something!", "Come on"
                                , JOptionPane.PLAIN_MESSAGE);
                    } else {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isInDictionary(s)) {
                                    setAddMode(s);
                                } else
                                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "This word exists!", "Come on"
                                            , JOptionPane.PLAIN_MESSAGE);
                            }
                        });
                        t.start();
                    }
                }
                else{
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            addWord(textField.getText(),textArea.getText());
                            setSearchMode();
                        }
                    });
                    t.start();
                    setSearchMode();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!isEdit && !textField.getText().isEmpty()){
                    int n = JOptionPane.showConfirmDialog(
                            JOptionPane.getRootFrame(),
                            "Are you sure you want to delete this words and its content?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION);
                    if(n==0){
                        deleteWord(inputWord);
                        setSearchMode();
                    }
                }
                else{
                    setSearchMode();
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String p=portField.getText();
                String u=usernameField.getText();
                if(!u.isEmpty()){
                    userName=u;
                    if(!p.isEmpty() & isNumeric(p)) {
                        port = Integer.parseInt(p);
                        startStatus();
                    }
                    else
                        printText("Please input valid port numbers");
                }
                else{
                    printText("Please input username");
                }

            }
        });
    }

    public String commServer(String s){
        try(Socket socket = new Socket(ip, port)){
            // Output and Input Stream
            DataInputStream input = new DataInputStream(socket.
                    getInputStream());
            DataOutputStream output = new DataOutputStream(socket.
                    getOutputStream());
            output.writeUTF(s);
            output.flush();

            String result = input.readUTF();
            input.close();
            output.close();
            return result;
        } catch (ConnectException e) {
            printText("Can't reach server, check network");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public boolean isInDictionary(String s){

        String result=commServer(toJsonString(userName,s,flag[0],""));
        if(!result.isEmpty() && result!="0")
            return true;
        return false;
    }

    public void getMeaning(){

        String result=commServer(toJsonString(userName,inputWord,flag[0],""));
        if(result.isEmpty()){
            printText("Oops,can't find it. You can add one.");
        }
        else{
            printText(result);
        }
    }


    public void addWord(String w,String m){
        String result=commServer(toJsonString(userName,w,flag[1],m));
        System.out.println(result);
        if(result.equals("1")){
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Somebody just added it", "Oops"
                    , JOptionPane.PLAIN_MESSAGE);
        }
        if(result.equals("0")){
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Added", "Success"
                    , JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void deleteWord(String w){
        String result=commServer(toJsonString(userName,w,flag[2],""));
        System.out.println("delete:"+result);
        if(result.equals("1")){
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Somebody just deleted it", "Oops"
                    , JOptionPane.PLAIN_MESSAGE);
        }
        if(result.equals("0")){
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Deleted", "Success"
                    , JOptionPane.PLAIN_MESSAGE);
        }
    }

    public String toJsonString(String u,String w,String f,String m){
        JSONObject obj=new JSONObject();
        obj.put("username",u);
        obj.put("word",w);
        obj.put("flag",f);
        obj.put("meaning",m);
        return obj.toString();
    }

    public void setSearchMode(){
        searchButton.setEnabled(true);
        addButton.setText("Add");
        deleteButton.setText("Delete");
        isEdit=false;
        textField.setText("");
        textField.setEditable(true);
        textArea.setText("");

    }

    public void setAddMode(String w){
        textField.setEditable(false);
        textField.setText(w);
        textArea.setText("");
        searchButton.setEnabled(false);
        addButton.setText("Confirm");
        deleteButton.setText("Cancel");
        isEdit=true;
    }

    public void initStatus(){
        textField.setEditable(false);
        textArea.setEnabled(false);
        searchButton.setEnabled(false);
        addButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    public void startStatus(){
        textField.setEditable(true);
        textArea.setEnabled(true);
        searchButton.setEnabled(true);
        addButton.setEnabled(true);
        deleteButton.setEnabled(true);
        usernameField.setEditable(false);
        portField.setEditable(false);
        startButton.setEnabled(false);
    }

    public static boolean isNumeric (String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private void printText(String s){
        textArea.setText(s);
    }

}
