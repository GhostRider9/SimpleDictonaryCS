/**
 * Distributed System Project 1
 * Liping Zhang, ID:1016954
 */
package server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class serverWindow extends serverLogic{


    private JPanel mainWindow;
    private JTextArea logArea;
    private JButton startServerButton;
    private JTextField pathField;
    private JButton stopServerButton;
    private JTextField portField;
    private JTextField threadLimitField;
    private JLabel portLable;
    private JLabel dictPathLable;
    private JLabel threadLimitLable;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Dictionary Server");
                frame.setContentPane(new serverWindow().mainWindow);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public serverWindow() {

        pathField.setText(dictPath);
        portField.setText(""+port);
        threadLimitField.setText(""+threadLimit);

        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryStartServer(portField.getText(),threadLimitField.getText(),pathField.getText());
            }
        });
        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
    }

    public void printLog(String m){
        logArea.append(m+"\n");
    }
}
