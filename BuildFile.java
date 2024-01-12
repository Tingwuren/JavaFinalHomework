//用于生成配置文件的类
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class BuildFile {
    public static void main ( String[] args ) throws IOException {
        //用于输入用户总数
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入生成的用户总数：");
        Server.totalUsers = sc.nextInt();
        sc.close();

        //将输出流换为配置文件
        String fileName = "config.txt";
        PrintStream out = new PrintStream( 
            new BufferedOutputStream( new FileOutputStream( fileName )));
        System.setOut(out);
        System.setErr(out);

        //将根目录写入配置文件
        String rootdir = rootDir();
        System.out.println(rootdir);

        //将用户名和密码写入配置文件
        for(int i = 0; i < Server.totalUsers;i ++){
            StringBuilder username = buildName();
            StringBuilder password = buildPass();
            //System.out.println("Usernumber: "+ i);
            //System.out.println("Username: " + username);
            //System.out.println("Password: " + password);
            System.out.println(i);
            System.out.println(username);
            System.out.println(password);
        }

        out.close();
    }

    //用于生成一个由六个字母组成的用户名
    static StringBuilder buildName () {
        Random random = new Random();
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder username = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            username.append(chars[random.nextInt(chars.length)]);
        }
        return username;
    }

    //用于生成一个由八个字母或数字组成的密码
    static StringBuilder buildPass () {
        Random random = new Random();
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars[random.nextInt(chars.length)]);
        }
        return password;
    }

    //用于生成服务器根目录
    static String rootDir () {
        return "root";
    }
}
