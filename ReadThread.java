//客户端启动的读取服务器响应线程
import java.io.*;
public class ReadThread implements Runnable {
    private DataInputStream in ;
    ReadThread(DataInputStream in) {
        this.in = in ;
    }

    public void run() {
        String response;
        try {
            while ((response=in.readUTF() )!= null)
                System.out.println("服务器响应: " + response);//打印服务器响应
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}