import java.lang.reflect.Array;
import java.util.*;
import java.lang.Integer;

public class LSH {

    private static final int SHINGLE_LENGTH = 6;
    private static final int ALPHABET_NUMBER = 37;

    private HashMap<Character, Integer> characterMap = new HashMap<Character, Integer>();

    // Use hash functions h(x)=((a·x+b) mod p) mod N.
    // We generate 100 random a and b, then use them for the hash functions.
    private Long P;
    private int a[];
    private int b[];
    private Long N;

    private int minHashRows = 120;

    LSH() {
        this(120);
    }

    LSH(int min_hash_rows) {
        initializeCharacterMap();
        this.minHashRows = min_hash_rows;
        generateMinHashParameters();
    }

    // We use 20 hash functions
    private void generateMinHashParameters() {
        int param_max_value = 10000;
        a = new int[this.minHashRows];
        b = new int[this.minHashRows];
        P  = new Long("2565726421");
        N = new Double(Math.pow((double)ALPHABET_NUMBER, (double)SHINGLE_LENGTH)).longValue();
        for (int i=0; i<this.minHashRows; i++) {
            a[i] = (int)(Math.random() * param_max_value);
            b[i] = (int)(Math.random() * param_max_value);
        }
    }

    // The k_th permutation hash function
    private Long getMinHashRow(int k, Long row) {
        Long ret = ((a[k] * row + b[k]) % this.P) % this.N;
        return ret;
    }

    /* Build characterMap. We consider 0, 1, ..., 8, 9, ' ', a, b, ..., z. They represent the number from 0 to 36. */
    private void initializeCharacterMap() {
        for (char a = '0'; a <= '9'; a ++) {
            characterMap.put(a, (int)(a - '0'));
        }
        characterMap.put(' ', 10);
        for (char a = 'a'; a <= 'z'; a ++) {
            characterMap.put(a, 11 + (a - 'a'));
        }
    }

    private long getHashedValueForShingle(String shingle) {
        if (shingle.length() != this.SHINGLE_LENGTH) {
            System.out.println("The shingle length should be " + this.SHINGLE_LENGTH);
            System.exit(1);
        }

        long hash_value = 0;
        for (int i=0; i<shingle.length(); i++) {
            char c = shingle.charAt(i);
            Integer char_value = characterMap.get(c);
            if (char_value == null) {
                System.out.println("Character not considered. This shouldn't happen!");
                System.exit(1);
            }
            hash_value = hash_value * this.ALPHABET_NUMBER + char_value;
        }
        return hash_value;
    }

    private ArrayList<Long> getHashedShingleArrayFromFileContent(String content) {
        content = content.toLowerCase();
        StringBuilder processed_content_builder = new StringBuilder();
        boolean last_character_is_space = false;
        for (int i=0; i<content.length(); i++) {

            // Only consider the 37 characters
            if (characterMap.get(content.charAt(i)) != null) {
                if (content.charAt(i) != ' ') {
                    last_character_is_space = false;
                    processed_content_builder.append(content.charAt(i));
                } else if (!last_character_is_space) {
                    //merge consecutive spaces into one
                    last_character_is_space = true;
                    processed_content_builder.append(content.charAt(i));
                }
            }
        }
        content = processed_content_builder.toString();

        HashSet<Long> appeared_shingle_values = new HashSet<Long>();
        ArrayList<Long> shingle_values = new ArrayList<>();
        for (int i = 0; i <= content.length() - this.SHINGLE_LENGTH; i ++) {
            String shingle = content.substring(i, i + this.SHINGLE_LENGTH);
            Long hash_value = this.getHashedValueForShingle(shingle);
            if (!appeared_shingle_values.contains(hash_value)) {
                appeared_shingle_values.add(hash_value);
                shingle_values.add(hash_value);
            }
        }
        return shingle_values;
    }

    private ArrayList<Long> getMinHashSets(ArrayList<Long> shingle_values) {
        ArrayList<Long> min_hash_values = new ArrayList<>();
        for (int i=0; i<this.minHashRows; i++) {
            min_hash_values.add(Long.MAX_VALUE);
        }
        for (Long shingle_value: shingle_values) {
            for (int i=0; i<this.minHashRows; i++) {
                Long hash_row = this.getMinHashRow(i, shingle_value);
                if (hash_row < min_hash_values.get(i)) {
                    min_hash_values.set(i, hash_row);
                }
            }
        }
        return min_hash_values;
    }

    public ArrayList<Long> getMinHashSetsFromFileContent(String content) {
        ArrayList<Long> hashed_shingles = this.getHashedShingleArrayFromFileContent(content);
        ArrayList<Long> min_hash_sets = this.getMinHashSets(hashed_shingles);
        return min_hash_sets;
    }

    // Hash the r rows into a number between 0 - 99999.
    // Use the function (x1 % 10000 + x2 % 10000 + x3 % 10000 + x4 % 10000 + x5 % 10000) % 10007.
    private int getBandHashedValue(ArrayList<Long> min_hash_set, int start_position, int r) {
        if (min_hash_set.size() < r) {
            System.out.println("Error! The number of rows should be at least " + r + ". Exit the program!");
            System.exit(1);
        }

        Long hash_value = new Long(0);
        for (int i = start_position; i < start_position + r; i ++) {
            hash_value += min_hash_set.get(i) % 10000;
        }
        hash_value = hash_value % 10007;
        return hash_value.intValue();
    }

    public static double getFileSimilarity(MinHashedFile file1, MinHashedFile file2) {
        ArrayList<Long> min_hash1 = file1.getMinHashSet();
        ArrayList<Long> min_hash2 = file2.getMinHashSet();

        int dividend = 0, denomitor = 0;
        for (int i=0; i<min_hash1.size(); i++) {
           if (min_hash1.get(i) == min_hash2.get(i)) {
               dividend ++;
           }
           denomitor ++;
        }
        return (double)dividend / (double)denomitor;
    }

    public HashSet<Cluster> doLSHandCluserting(ArrayList<MinHashedFile> files) {
        ArrayList<Cluster> fileToClusterMap = new ArrayList<>();

        // At first, every file belongs to a cluster that only has itself
        for (int i=0; i<files.size(); i++) {
            Cluster cluster = new Cluster();
            cluster.addFile(files.get(i));
            fileToClusterMap.add(cluster);
        }

        int b = 20, r = 6;
        double similarity_threshold = 0.6;
        for (int i = 0; i < b; i ++) {
            HashMap<Integer, ArrayList<MinHashedFile>> bucketToFiles = new HashMap<>();

            int start_position = i * r;
            for (int j=0; j<files.size(); j++) {
                int hash_value = this.getBandHashedValue(files.get(j).getMinHashSet(), start_position, r);
                if (bucketToFiles.get(hash_value) == null) {
                    bucketToFiles.put(hash_value, new ArrayList<MinHashedFile>());
                }

                Cluster current_file_cluster = fileToClusterMap.get(j);
                ArrayList<MinHashedFile> same_bucket_files = bucketToFiles.get(hash_value);
                for (int k=0; k<same_bucket_files.size(); k++) {
                    MinHashedFile tmp_file = same_bucket_files.get(k);
                    Cluster tmp_cluster = fileToClusterMap.get(tmp_file.getFileNumber());
                    if (!current_file_cluster.equals(tmp_cluster)) {
                        if (current_file_cluster.shouldMergeCluster(tmp_cluster, similarity_threshold)) {
                            current_file_cluster.mergeCluster(tmp_cluster);
                            ArrayList<MinHashedFile> new_cluster_files = current_file_cluster.getFiles();
                            for(MinHashedFile file: new_cluster_files) {
                                fileToClusterMap.set(file.getFileNumber(), current_file_cluster);
                            }
                        }
                    }
                }
                bucketToFiles.get(hash_value).add(files.get(j));
            }
        }

        HashSet<Cluster> clusters = new HashSet<>();
        for(Cluster cluster: fileToClusterMap) {
            if (!clusters.contains(cluster)) {
                clusters.add(cluster);
            }
        }
        return clusters;
    }
}
