import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class server {
    private  static  ServerSocket serverSocket;//Tạo server và port
    private static Socket socket;//Lắng nghe client
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    public static boolean loop = true;
    private static HashMap<String, String > dictionaries  = new HashMap<>();
    //constructor
    public server(){}

    //Xóa dấu tiếng Ziệt
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("đ", "d");
    }

    //Lấy đường dẫn tương đối của file Dictionary.txt
    private static String getPathDictionaryTxt(){
        String absolutePath = Paths.get("").toAbsolutePath().toString().split(":")[1]+"";
        String localPath = Paths.get("\\src\\dictionary.txt").toAbsolutePath().toString().split(":")[1]+"";
        return absolutePath+localPath;
    }

    //Đọc File Dictionary.txt
    private static  void readFile(String pathFile ){
        try {
            Scanner file = new Scanner(new File(pathFile));
            while (file.hasNextLine()){
                String[] line = file.nextLine().split(";");
                String keyHash = line[0];
                String valueHash = line[1];
                dictionaries.putIfAbsent(keyHash,valueHash);

            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private  static  void handle(){
        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server is running on port 5000....");
            socket = serverSocket.accept();
            System.out.println("Client connecting........");

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8) );

            while (loop){
                String[] data = bufferedReader.readLine().split(";");
                String message = data[0];

                switch (message.toUpperCase(Locale.ROOT)){
                   case "BYE":
                       bufferedWriter.write("Bái bai");
                       bufferedWriter.newLine();
                       bufferedWriter.flush();
                       loop=false;
                    break;

                   case "ADD":
                        String x = data[1]; //Lấy từ Tiếng Anh
                        String y = data[2];//Lấy từ Tiếng Ziệt
                        boolean flagAdd = false;

                        //Kiểm tra từ tiếng anh có tồn tại hay chưa
                        for (String key : dictionaries.keySet()){
                            if (key.toLowerCase().equals(x.toLowerCase())){
                                flagAdd=true;
                                bufferedWriter.write("This word already exists");
                                bufferedWriter.newLine();
                                bufferedWriter.flush();
                                break;
                            }
                        }

                                ///Trường hợp chưa
                        if (!flagAdd){
                            //Thêm vào HashMap dictionaries
                            dictionaries.putIfAbsent(x,y);

                            //Ghi vào file
                            PrintWriter printWriter = new PrintWriter(new FileWriter(getPathDictionaryTxt(),true));
                            printWriter.write(x+";"+y +"\n");
                            printWriter.flush();
                            bufferedWriter.write("Add successful");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                        break;

                   case "DEL":
                        String del = data[1].trim();
                        boolean flagDel = false;
                        for (String key:dictionaries.keySet()){
                            if (key.equals(del)){
                                dictionaries.remove(key);
                                String line;
                                String tmp = "";

                                File inputFile = new File(getPathDictionaryTxt());
                                File tmpFile = new File("src/tmp.txt");

                                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                                BufferedWriter writer = new BufferedWriter((new FileWriter(tmpFile)));

                                String currentLine;

                                while ((currentLine=reader.readLine())!=null){
                                    if (currentLine.trim().split(";")[0].equals(del)){
                                        continue;
                                    }
                                    writer.write(currentLine+System.getProperty("line.separator"));
                                }
                                writer.close();
                                reader.close();
                                inputFile.delete();
                                tmpFile.renameTo(inputFile);

                                bufferedWriter.write("Delete Successful");
                                bufferedWriter.newLine();
                                bufferedWriter.flush();

                                flagDel=true;
                                break;
                            }
                        }

                        if (!flagDel){
                            bufferedWriter.write("Can't find the word to delete");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }

                   default:
                       ///Tra cứu Tiếng Anh
                       boolean flagCheckEng = false;
                       for (String key: dictionaries.keySet()){
                           if(message.equalsIgnoreCase(key)){
                               String value = dictionaries.get(key);
                               bufferedWriter.write("Translate: " + value);
                               bufferedWriter.newLine();
                               bufferedWriter.flush();
                               flagCheckEng=true;
                               break;
                           }
                       }

                       ///Tra cứu Tiếng Ziệt
                       if (!flagCheckEng){
                           for (String key: dictionaries.keySet()){
                               String value = dictionaries.get(key);
                               if(message.equalsIgnoreCase(removeAccent(value))){
                                   bufferedWriter.write("=======> Translate: " + key);
                                   bufferedWriter.newLine();
                                   bufferedWriter.flush();
                                   flagCheckEng=true;
                                   break;
                               }
                           }
                       }

                       //Không tìm thấy
                       if (!flagCheckEng){
                           bufferedWriter.write(" ^_____^ Not Found");
                           bufferedWriter.newLine();
                           bufferedWriter.flush();
                       }
                }

        System.out.println("Client request lookup:        "+message);


            }
            bufferedReader.close();
            bufferedWriter.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public  static  void main(String[] args){
        server.readFile(server.getPathDictionaryTxt());
        server.handle();
    }
}
