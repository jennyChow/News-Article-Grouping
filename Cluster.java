import java.util.ArrayList;
import java.util.HashMap;

public class Cluster {
    // clusteroid == -1 means that the cluster is empty
    private MinHashedFile clustroid;
    private ArrayList<MinHashedFile> files;

    Cluster() {
        clustroid = null;
        files = new ArrayList<>();
    }

    public ArrayList<MinHashedFile> getFiles() {
        return this.files;
    }

    public void addFile(MinHashedFile file) {
        files.add(file);
        computeClustroid();
    }

    private void computeClustroid() {
        if (files.isEmpty()) {
            return;
        }

        if (files.size() <= 2) {
            clustroid = files.get(0);
            return;
        }

        ArrayList<Double> similarities = new ArrayList<>();
        for (int i=0; i<files.size(); i++) {
            double similarity_sum = 0;
            for (int j=0; j<files.size(); j++) {
                if (i != j) {
                    similarity_sum += LSH.getFileSimilarity(files.get(i), files.get(j));
                }
            }
            similarities.add(similarity_sum);
        }

        int min_similarity_index = 0;
        for (int j=1; j<similarities.size(); j++) {
            if (similarities.get(j) < similarities.get(min_similarity_index)) {
                min_similarity_index = j;
            }
        }
        clustroid = files.get(min_similarity_index);
    }

    public boolean shouldMergeCluster(Cluster cluster, double threshold) {
        MinHashedFile clustroid = cluster.getClustroid();
        if (LSH.getFileSimilarity(this.clustroid, clustroid) <= threshold) {
            return true;
        } else {
            return false;
        }
    }

    private void destroy() {
        files = null;
        clustroid = null;
    }

    public void mergeCluster(Cluster cluster) {
        files.addAll(cluster.files);
        computeClustroid();
        cluster.destroy();
    }

    public MinHashedFile getClustroid() {
        return clustroid;
    }
}
