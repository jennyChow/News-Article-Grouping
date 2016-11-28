import java.util.ArrayList;

public class MinHashedFile {
    private int fileNumber;
    private ArrayList<Long> minHashSet;

    MinHashedFile(int file_num, ArrayList<Long> min_hash_set) {
        this.fileNumber = file_num;
        this.minHashSet = min_hash_set;
    }

    public ArrayList<Long> getMinHashSet() {
        return this.minHashSet;
    }

    public int getFileNumber() {
        return this.fileNumber;
    }
}
