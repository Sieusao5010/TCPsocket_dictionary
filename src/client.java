import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class client {
    private static Socket socket;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    public static BufferedReader std;
    public static boolean loop = true;

    public client(){}

    //Xóa dấu tiếng Ziệt
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("đ", "d");
    }
    private  static  void handle(){
        try {
            socket = new Socket("localhost",5000);
            System.out.println("Client connected...");

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));


            //Đẩy dữ liệu nhập từ bàn phím vào trong luồng
            std = new BufferedReader(new InputStreamReader(System.in));


//            bufferedWriter.write("Hello Server /////");
            while (loop){

                System.out.println("Enter the word to look up: ");
                String input = std.readLine();
//                input = removeAccent(input);

                bufferedWriter.write(input);
                bufferedWriter.newLine();
                bufferedWriter.flush();//ĐẨy

                String message = bufferedReader.readLine();
                System.out.println(message);

                if (message.equalsIgnoreCase("Bái bai")){
                    loop=false;
                }
            }


            bufferedReader.close();
            bufferedWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public  static  void main(String[] args){
        client.handle();

    }

}
