import boilerplate.utility.Logging;
import common.Game;

public class Main {
    public static void main(String[] args) {
        Logging.mystical("starting!");
        (new Game()).start();
    }
}
