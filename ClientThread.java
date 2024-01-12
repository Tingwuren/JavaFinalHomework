//服务器启动的客户端线程
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ClientThread implements Runnable {
    private Socket toClientSocket = null;//会话套接字
    private String logFileName = "server.log"; // 日志文件名

    ClientThread(){
    }

    ClientThread(Socket toClientSocket){
    	this.toClientSocket = toClientSocket;	
    }

    public void run() {
        try {
            DataOutputStream writer = new DataOutputStream(toClientSocket.getOutputStream());
            DataInputStream reader = new DataInputStream(toClientSocket.getInputStream());
            OutputStreamWriter logWriter = new OutputStreamWriter(new FileOutputStream(logFileName), StandardCharsets.UTF_8);
            //FileWriter logWriter = new FileWriter(logFileName, true); // 打开日志文件
            String clientIP = toClientSocket.getInetAddress().getHostAddress(); // 获取客户端IP地址
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // 获取当前时间戳

            String username;
            String password;

            // 验证用户名和密码，若用户验证失败，可以重复验证
            while (true) {
                username = reader.readUTF();//读取客户端发送的用户名
                timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // 获取当前时间戳
                if ("anonymous".equals(username)) {
                    writer.writeUTF("验证成功！");//向客户端发送登录成功响应
                    logWriter.write(timestamp + " - 登录成功 - 用户名: anonymous - IP地址: " + clientIP + "\n"); // 记录登录成功信息
                    break;
                }

                password = reader.readUTF();//读取客户端发送的用户密码
                if (password.equals(Server.users.get(username))) {//匹配成功
                    writer.writeUTF("验证成功！");//向客户端发送登录成功响应
                    logWriter.write(timestamp + " - 登录成功 - 用户名: " + username + " - IP地址: " + clientIP + "\n"); // 记录登录成功信息
                    break;
                } else {
                    writer.writeUTF("用户名或密码错误！");
                    logWriter.write(timestamp + " - 登录失败 - 用户名: " + username + " - IP地址: " + clientIP + "\n"); // 记录登录失败信息
                }
            }

            // 处理客户端命令
            String command;//声明客户端请求
            String currentDirectory = Server.rootDirectory;//当前目录初始为根目录
            while (!(command = reader.readUTF()).equals("exit")) {//接收到非终止命令
                System.out.println("接收到命令: " + command); // 打印接收到的命令
                timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // 获取当前时间戳
                logWriter.write(timestamp + " - 登录失败 - 用户名: " + username + " - IP地址: " + clientIP + "\n"); // 记录登录失败信息
                File dir = new File(currentDirectory);//打开当前目录
                String[] parts = command.split(" ");//将命令用空格分隔
                switch (parts[0]) {//选择命令类型
                    case "dir":
                        // 列出当前目录下所有文件（包括子目录）
                        if (parts.length == 1) {
                            File[] files = dir.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    writer.writeUTF(file.getName());//向客户端发出响应
                                }
                                writer.writeUTF("dir响应结束");//向客户端发出结束响应
                            }
                        } else {
                            writer.writeUTF("格式错误!dir命令不需要参数");
                            writer.writeUTF("dir响应结束");
                        }
                        
                        break;
                    case "cd":
                        // 进入名字为“目录名”的子目录
                        if (parts.length > 1) {
                            String dirName = parts[1];//获取目录名
                            if ("..".equals(dirName)) {//返回上级目录
                                File parentDir = dir.getParentFile();
                                if (parentDir!= null) {
                                    currentDirectory = parentDir.getPath();
                                    writer.writeUTF("已跳转到上级目录！");
                                    System.out.println("当前目录: " + parentDir.getAbsolutePath());
                                } else {
                                    currentDirectory = Server.rootDirectory;
                                    writer.writeUTF("已跳转到根目录！");
                                    System.out.println("当前目录: " + dir.getAbsolutePath());
                                }
                            }  else {//转到指定目录
                                File cdDir = new File(currentDirectory, dirName);
                                if (cdDir.exists() && cdDir.isDirectory()) {
                                    currentDirectory = cdDir.getPath();
                                    writer.writeUTF("目录跳转成功！");
                                    System.out.println("当前目录: " + cdDir.getAbsolutePath());
                                } else {
                                    writer.writeUTF("目录不存在！");
                                    System.out.println("当前目录: " + dir.getAbsolutePath());
                                }
                            }
                        } else {//命令没有指定参数
                            writer.writeUTF("未输入目录名，请输入规范命令！");
                            System.out.println("当前目录: " + dir.getAbsolutePath());
                        }
                        break;
                    case "put":
                        // 把本地文件上传到服务器端当前目录
                        if ("anonymous".equals(username)) {
                            writer.writeUTF("匿名用户不允许上传文件！");
                        } else {
                            // 处理上传文件
                            if (parts.length > 1) {
                                String fileName = parts[1];//获取文件名字
                                writer.writeUTF("开始上传文件"); // 发送服务器响应
                                long fileSize = reader.readLong();//获取文件大小
                                System.out.println("接收文件大小为："+ fileSize);
                                File file = new File(currentDirectory, fileName);
                                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {//在服务器当前目录新建要上传的文件
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while (fileSize > 0 && 
                                    (bytesRead = reader.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                                        fileOutputStream.write(buffer, 0, bytesRead);
                                        fileSize -= bytesRead;
                                    }
                                }
                            }
                        }
                        break;
                    case "get":
                        // 将服务器上的文件下载到本地当前目录
                        if (parts.length > 1) {
                            String fileName = parts[1];//获取文件名
                            File file = new File(currentDirectory, fileName);//打开服务器端的文件
                            long fileSize = file.length();
                            writer.writeLong(fileSize); // 发送文件大小
                            System.out.println("发送文件: " + fileName + ", 大小: " + fileSize); // 打印发送的文件名和文件大小
                            if (file.exists() && file.isFile()) {
                                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while (fileSize > 0 && 
                                    (bytesRead = fileInputStream.read(buffer)) != -1) {
                                        writer.write(buffer, 0, bytesRead);//服务器向客户端发送文件
                                        fileSize -= bytesRead;
                                    }
                                }
                            } else {
                                writer.writeLong(0);
                            }
                        }
                        break;
                    case "exit":
                        // 结束当前服务器线程
                        System.out.println("结束当前线程！");
                        break;
                }
            }

            logWriter.close(); // 关闭日志文件
        }
        catch(IOException ex) {
            //ex.printStackTrace();
        }
    }
}