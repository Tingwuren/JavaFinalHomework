//客户端
import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client {
    private static String username;//客户端登录用户名
    private static String password;//客户端登录密码
	public static void main(String[] args) throws IOException {
	    Socket clientSocket = null; //声明客户机套接字
	    Scanner sc = new Scanner(System.in);//用于客户端输入
        String response;//保存服务器对客户端的响应
	    
	    clientSocket = new Socket(InetAddress.getByName("127.0.0.1"), 8000);//客户端连接服务器的8000端口
        DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());

        // 验证用户名和密码，若用户验证失败，可以重复验证
        System.out.println("请输入用户名：");
        username = sc.nextLine();//输入用户名
        writer.writeUTF(username);//将用户名发送给服务器
        System.out.println("请输入密码：");
        password = sc.nextLine();//输入密码
        writer.writeUTF(password);//将密码发送给服务器
        response = reader.readUTF();//获取服务器响应
        while (!response.startsWith("验证成功！")) {//未登录成功则重新登录
            System.out.println("服务器响应：" + response);//打印登录错误响应
            System.out.println("请输入用户名：");
            username = sc.nextLine();//重新输入用户名
            writer.writeUTF(username);//重新发送用户名
            System.out.println("请输入密码：");
            password = sc.nextLine();//重新输入用户密码
            writer.writeUTF(password);//重新发送用户密码
            response = reader.readUTF();//再次获取服务器响应
        }
        System.out.println("服务器响应：" + response);//打印登录成功响应

        // 发送命令给服务器
        String command;
        String currentDirectory = "user";//客户端目录为当前目录
        System.out.print("$ ");//提示用户可以输入命令
        while (!(command = sc.nextLine()).equals("exit")) {//输入命令
            String[] parts = command.split(" ");//将命令用空格分隔
            switch (parts[0]) {//选择命令种类
                case "dir":
                    // 列出当前目录下所有文件（包括子目录）
                    writer.writeUTF(command);//向服务器发送命令
                    while (!(response = reader.readUTF()).equals("dir响应结束")) { // 循环读取服务器响应
                        System.out.println(response); // 打印服务器响应
                    }
                    break;
                case "cd":
                    // 进入名字为“目录名”的子目录
                    writer.writeUTF(command);//向服务器发送命令
                    response = reader.readUTF();//获取服务器响应
                    System.out.println("服务器响应：" + response);//打印服务器响应
                    break;
                case "put":
                    // 把本地文件上传到服务器端当前目录
                    if (parts.length > 1) {
                        String fileName = parts[1];//获取文件名
                        writer.writeUTF(command);//向服务器发送命令
                        //File file = new File(fileName);
                        File file = new File(currentDirectory, fileName);//打开本地目录下的文件
                        long fileSize = file.length();
                        System.out.println("发送文件: " + fileName + ", 大小: " + fileSize);
                        if (file.exists() && file.isFile()) {
                            writer.writeLong(file.length());
                            try (FileInputStream fileInputStream = new FileInputStream(file)) {//从客户端当前目录读取文件
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                    writer.write(buffer, 0, bytesRead);//将文件向服务器发出
                                }
                            }
                        }
                    }
                    break;
                case "get":
                    // 将服务器上的文件下载到本地当前目录
                    if (parts.length > 1) {
                        String fileName = parts[1];
                        writer.writeUTF(command); // 向服务器发送下载命令
                        long fileSize = reader.readLong(); //从服务器读取文件长度
                        System.out.println("接收文件大小为："+ fileSize);
                        System.out.println("开始下载文件");
                        if (fileSize > 0) {
                            //File file = new File(fileName);//新建文件
                            File file = new File(currentDirectory, fileName);//在客户端本地新建文件
                            System.out.println("文件地址: " + file.getAbsolutePath()); // 打印文件的绝对路径
                            //File file = new File(currentDirectory, fileName);
                            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {//将文件输出流对应到新建文件
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while (fileSize > 0 && 
                                (bytesRead = reader.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);// 将从服务器接收到的数据写入本地文件
                                    fileSize -= bytesRead;
                                }
                            }
                        } else {
                            System.out.println("文件不存在！");
                        }
                    }
                    break;
                case "exit":
                    //结束客户端进程
                    break;
                default:
                    System.out.println("请输入正确的命令，以下是使用规范：");
                    System.out.println("dir: 列出当前目录下的所有文件；");
                    System.out.println("cd 目录名: 进入名字为“目录名”的子目录,其中cd ..表示回到上一级目录；");
                    System.out.println("get 文件名: 将服务器上的文件下载到本地当前目录；");
                    System.out.println("put 文件名: 把本地文件上传到服务器当前目录；");
                    System.out.println("exit: 结束客户端程序。");
                    break;
            }
            System.out.print("$ ");//提示用户可以输入命令
        }
        writer.writeUTF("exit");//向服务器发送关闭连接请求
    }
}
