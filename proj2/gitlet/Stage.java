package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Stage implements Serializable {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The stage map. */
    private Map<String, String> blobmap = new java.util.HashMap<>();
    /** Remove filename. Key:filename Value:SHA1 */
    private List<String> removeFile = new ArrayList<>();

    /**
     * Constructor for Stage.
     * @author CuiYuxin
     */
    public Stage() {
        File stagefile = new File(".gitlet/stage");
        if (!stagefile.exists()) {
            try {
                stagefile.createNewFile();
                write();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        } else {
            this.blobmap = Utils.readObject(stagefile, Stage.class).blobmap;
            this.removeFile = Utils.readObject(stagefile, Stage.class).removeFile;
        }
    }

    /**
     * Return the stage map.
     * @author CuiYuxin
     */
    public Map<String, String> getBlobmap() {
        return blobmap;
    }

    /**
     * Return the remove file list.
     * @author CuiYuxin
     */
    public List<String> getRemoveFile() {
        return removeFile;
    }

    /**
     * Add a file to stage.
     * @author CuiYuxin
     */
    public void add(String fileName, String blobName, String head) {
        Commit headObj = Commit.read(head);
        Map<String, String> oldBlobs = headObj.getBlobs();
        if (removeFile.contains(fileName)) {
            removeFile.remove(fileName);
        }
        if (oldBlobs.getOrDefault(fileName, "").equals(blobName)) {
            if (this.blobmap.containsKey(fileName)) {
                this.blobmap.remove(fileName);
            }
        } else {
            this.blobmap.put(fileName, blobName);
        }
    }

    /**
     * return if the stage is empty.
     * @author CuiYuxin
     */
    public boolean isEmpty() {
        return this.blobmap.isEmpty() && this.removeFile.isEmpty();
    }

    /**
     * Remove a file from stage.
     * @author CuiYuxin
     */
    public void rm(String fileName, String head) {
        boolean fail = true;
        if (this.blobmap.containsKey(fileName)) {
            fail = false;
            this.blobmap.remove(fileName);
        }
        Commit headObj = Commit.read(head);
        if (headObj.getBlobs().containsKey(fileName)) {
            fail = false;
            removeFile.add(fileName);
            Utils.restrictedDelete(fileName);
        }
        if (fail) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** Write the stage status to disk.
     *  @author CuiYuxin
     */
    public void write() {
        File stageFile = new File(".gitlet/stage");
        Utils.writeObject(stageFile, this);
    }

    /** Clean the stage.
     *  @author CuiYuxin
     */
    public void clear() {
        blobmap = new java.util.HashMap<>();
        removeFile = new ArrayList<>();
        this.write();
    }

    /** Return staged filename.
     *  @author CuiYuxin
     */
    public String[] getStagedFiles() {
        String[] files = blobmap.keySet().toArray(new String[0]);
        Arrays.sort(files);
        return files;
    }

    /** Return removed filename.
     *  @author CuiYuxin
     */
    public String[] getRemovedFiles() {
        String[] files = removeFile.toArray(new String[0]);
        Arrays.sort(files);
        return files;
    }

    /** Return modified but not staged for commit filename and type.
     *  @author CuiYuxin
     */
    public String[] getUnstagedFiles(String head) {
        List<String> unstaged = new ArrayList<>();
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        Commit headObj = Commit.read(head);
        for (String file : allFiles) {
            File f = new File(file);
            Blob b = new Blob(f);
            String sha1 = Utils.sha1(Utils.serialize(b));
            if (headObj.getBlobs().containsKey(file) && !blobmap.containsKey(file)) {
                if (!sha1.equals(headObj.getBlobs().get(file))) {
                    unstaged.add(file + " (modified)");
                }
            } else if (blobmap.containsKey(file) && !sha1.equals(blobmap.get(file))) {
                unstaged.add(file + " (modified)");
            }
        }
        for (String file : blobmap.keySet()) {
            if (!allFiles.contains(file)) {
                unstaged.add(file + " (deleted)");
            }
        }
        for (String file : headObj.getBlobs().keySet()) {
            if (!allFiles.contains(file) && !removeFile.contains(file)) {
                if (!unstaged.contains(file + " (deleted)")) {
                    unstaged.add(file + " (deleted)");
                }
            }
        }
        String[] files = unstaged.toArray(new String[0]);
        Arrays.sort(files);
        return files;
    }

    /** Return the untracked filename.
     *  @author CuiYuxin
     */
    public String[] getUntrackedFiles(String head) {
        List<String> untracked = new ArrayList<>();
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        Commit headCmt = Commit.read(head);
        for (String file : allFiles) {
            if (!this.blobmap.containsKey(file)) {
                if (!headCmt.getBlobs().containsKey(file)) {
                    untracked.add(file);
                } else {
                    if (this.removeFile.contains(file)) {
                        untracked.add(file);
                    }
                }
            }
        }
        String[] files = untracked.toArray(new String[0]);
        Arrays.sort(files);
        return files;
    }
}
