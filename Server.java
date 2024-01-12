//服务器端
import java.io.*;
import java.net.*;
import java.util.Map;

public class Server {
    public static Map<String, String> users;//保存所有用户的用户名和密码
    public static int totalUsers;//保存用户总数，在生成配置文件时指定
    public static String rootDirectory;//上传、下载文件的根目录
    public static void main(String args[]) throws IOException{
        //读入一个配置文件
        ConfigReader configReader = new ConfigReader();
        configReader.readConfig("config.txt");
        rootDirectory = configReader.getRootDirectory();//获取根目录
        users = configReader.getUsers();//获取用户信息
        System.out.println("配置文件读取成功。");

        try (//在端口8000上监听
        ServerSocket serverSocket = new ServerSocket(8000)) {
            while(true){
                Socket socket = serverSocket.accept();
            	System.out.println("Server accept");
            	new Thread(new ClientThread(socket)).start();//使用线程池来支持多个客户端同时访问
            }
        }
    }
}