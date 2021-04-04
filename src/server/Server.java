package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Server{
    public static final Logger LOGGER = Logger.getLogger("");
    List<ClientHandler> clients;
    private AuthService authService;

    private static int PORT = 8189;
    ServerSocket server = null;
    Socket socket = null;

    public Server() {
        GregorianCalendar calendar = new GregorianCalendar();
        LOGGER.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record)
            {
                return "" + calendar.getTime() +" "+ record.getLevel() + "\t" + record.getMessage() + "\n";
            }
        });
        clients = new Vector<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            //System.out.println("Сервер запущен");
            LOGGER.log(Level.INFO, "сервер запущен");
            while (true) {
                socket = server.accept();
                //System.out.println("Клиент подключился");

//                clients.add(new ClientHandler(this, socket));
//                subscribe(new ClientHandler(this, socket));
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, " ошибка " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                server.close();
                LOGGER.log(Level.INFO, " сервер закончил работу.");
            } catch (IOException e) {
                Server.LOGGER.log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String str, boolean isForMultipleUsers) {
        if(!isForMultipleUsers){
            String user1 = str.split(" ")[0];
            String user2 = str.split(" ")[1];
            String msg = str.split(" ")[2];
            for (ClientHandler client : clients) {
                if(client.getNickName().equals(user1) || client.getNickName().equals(user2)) {
                    String message = String.format("%s : %s", client.getNickName(), msg);
                    client.sendMsg(message);
                    Server.LOGGER.log(Level.INFO, sender.getNickName()+ " отправил сообщение " +client +": "+  msg);
                }
            }
        }else {
            String message = String.format("%s : %s", sender.getNickName(), str);
            for (ClientHandler client : clients) {
                client.sendMsg(message);
            }
            Server.LOGGER.log(Level.INFO, sender.getNickName()+ " отправил всем сообщение: " +  message);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService(){
        return authService;
    }
}
