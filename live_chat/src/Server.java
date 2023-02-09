import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        Vector<UserThread> vector = new Vector<>();
        ExecutorService es = Executors.newFixedThreadPool(5);
        try {
            ServerSocket server = new ServerSocket(8888);
            while(true){
                Socket socket = server.accept();
                UserThread user = new UserThread(socket,vector);
                es.execute(user);


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


class UserThread implements Runnable{
    private String name;
    private Socket socket;
    Vector<UserThread> vector;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean flag = true;
    public UserThread(Socket socket,Vector<UserThread> vector){
        this.socket = socket;
        this.vector = vector;
        vector.add(this);
    }

    @Override
    public void run() {
        try{
            System.out.println("server-end: "+socket.getInetAddress().getHostAddress()+"is connected");
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            while(flag){
                Message msg = (Message)ois.readObject();
                //System.out.println(msg);
                int type = msg.getType();
                if(type == MessageType.TYPE_LOGIN){
                    name = msg.getFrom();
                    msg.setInfo("welcome!" + name);
                    oos.writeObject(msg);
                }else{
                    String to = msg.getTo();
                    int size = vector.size();
                    for(int i = 0; i < size; i++){
                        if(to.equals(vector.get(i).name)){
                            vector.get(i).oos.writeObject(msg);
                            break;
                        }
                    }
                }
            }
            ois.close();
            oos.close();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }
}
