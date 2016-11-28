import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class P2 {

    private String getContentFromFile(String file_name) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file_name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        String line = null;
        while (true) {
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line == null) {
                break;
            }
            sb.append(line);
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void processFiles() {
        // Read in all the file names
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> file_names = new ArrayList<>();
        String line = null;
        String base_path = "/home/mwang2/test/coen281/";
        while (true) {
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() == 0) {
                continue;
            }
            String[] splited_names = line.split("\\s+");
            for(String name: splited_names) {
                String file_name = name.trim();
                if (!file_name.startsWith("/")) {
                    file_name = base_path + file_name;
                }
                file_names.add(file_name);
            }
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if the file exists
        for (int i=0; i<file_names.size(); i ++) {
            File file = new File(file_names.get(i));
            if (!file.exists()) {
                System.out.println("Error! File not exists! Exit now.");
                System.exit(1);
            }
        }

        LSH lsh = new LSH();
        ArrayList<MinHashedFile> min_hash_matrix = new ArrayList<>();
        for (int i=0; i<file_names.size(); i++) {
            String content = this.getContentFromFile(file_names.get(i));
            min_hash_matrix.add(new MinHashedFile(i, lsh.getMinHashSetsFromFileContent(content)));
        }

        HashSet<Cluster> clusters = lsh.doLSHandCluserting(min_hash_matrix);
        int group_cnt = 0;
        for (Cluster cluster: clusters) {
            group_cnt ++;
            System.out.print("Group " + group_cnt + ":");
            for (int j=0; j<cluster.getFiles().size(); j++) {
                int file_num = cluster.getFiles().get(j).getFileNumber();
                System.out.print(file_names.get(file_num) + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        P2 p2 = new P2();
        p2.processFiles();
    }
}
