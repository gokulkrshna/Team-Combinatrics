import java.io.File;
import java.util.regex.Pattern;

public class DreamHack {

    private static final String dir_path = "E:\\DreamHackTeams\\";

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 1) {
            throw new Exception("Please mention the file name (E://Dream_hack/");
        }
        String file_name = args[0];
        String[] split = file_name.split(Pattern.quote(System.getProperty("file.separator")));
        if (split.length > 2) {
            throw new Exception("The Parent directory and file name must mentioned!!");
        }
        File file = new File(dir_path + file_name);
        if (!file.exists()) {
            throw new Exception("The File doesn't exist!!");
        }

        int i = file_name.lastIndexOf('.');
        if (!(i > 0)) {
            throw new Exception("Only CSV file are supported!!");
        }
        String extension = file_name.substring(i + 1);
        if (!extension.equals("csv")) {
            throw new Exception("Only CSV file are supported!!");
        }
        DreamHackImpl.processFile(file);
    }
}