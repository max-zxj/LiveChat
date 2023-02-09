import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Scanner input = new Scanner(System.in);
        try {
            Socket socket = new Socket("localhost",8888);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("please input the name:");
            String name = input.nextLine();
            Message msg = new Message(name,"",MessageType.TYPE_LOGIN,"");
            oos.writeObject(msg);
            msg = (Message)ois.readObject();
            System.out.println(msg.getInfo());

            es.execute(new ReadInfoThread(ois));

            //使用主线程发送消息
            boolean flag = true;
            while(flag){
                msg.setFrom(name);
                System.out.println("To:");
                msg.setTo(input.nextLine());
                msg.setType(MessageType.TYPE_SEND);
                System.out.println("Info:");
                msg.setInfo(input.nextLine());
                //System.out.println(msg);
                oos.writeObject(msg);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
}


class ReadInfoThread implements Runnable{
    private ObjectInputStream in;
    private boolean flag = true;
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public ReadInfoThread(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try{
            while(flag){
                Message message = (Message) in.readObject();
                System.out.println("["+message.getFrom()+"] to me:"+message.getInfo());
            }
            if(in != null){
                in.close();
            }
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }


    }
}
