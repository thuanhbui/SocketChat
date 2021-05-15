import client.Client;
import server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Login {

    private Client client;

    private JTextField username;
    private JPanel panel1;
    private JPasswordField password;
    private JButton loginButton;
    private JLabel loginResponse;

    public static JFrame frame = new JFrame("Login");

    public Login() {

        loginButton.setBorder(new RoundedBorder(5));

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        password.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    public void doLogin() {
        client = new Client(username.getText(), password.getPassword().toString());
        client.connect();
        try {
            Chat chat = new Chat(client);
            if (client.login()) {

                //client.login();
                JFrame frame_ = new JFrame("Chatting");
                frame_.getContentPane().add(chat.panel1);
                frame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame_.pack();
                frame_.setVisible(true);
                frame.setVisible(false);

                frame_.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.out.println("ow kia");
                        super.windowClosing(e);
                        client.logout();
                    }
                });
            } else {
                loginResponse.setText("You're already logged in!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        Login login = new Login();

        //login.panel1.setPreferredSize(new Dimension(300, 450));

        frame.setContentPane(login.panel1);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();

        frame.setVisible(true);
    }

}
