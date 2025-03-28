import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        InputStream s = ClassLoader.getSystemClassLoader().getResourceAsStream("file.txt");
        if (s == null) {
            System.out.println("nothing");
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (IOException _) {}
    }
}
