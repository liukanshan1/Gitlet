package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.join;

public class Branch implements Serializable  {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The branch directory. */
    public static final File BRANCH_DIR = join(CWD, ".gitlet", "branches");
    /** The name of the branch. */
    private String name;
    /** The commit SHA1 of the head of this branch. */
    private String latestCommit;

    /**
     * Read a branch
     * @author CuiYuxin
     */
    public static Branch read(String name) {
        String sp = File.separator;
        File branchFile = new File(".gitlet" + sp + "branches" + sp + name);
        if (!branchFile.exists()) {
            return null;
        }
        return Utils.readObject(branchFile, Branch.class);
    }

    /**
     * Remove a branch.
     * @author CuiYuxin
     */
    public static void remove(String branchName) {
        String sp = File.separator;
        File branchFile = new File(".gitlet" + sp + "branches" + sp + branchName);
        if (!branchFile.isDirectory()) {
            branchFile.delete();
        }
    }

    /**
     * Update branch latest commit.
     * @author CuiYuxin
     */
    public void update(String commitName) {
        this.latestCommit = commitName;
    }

    /**
     * Creates a new branch
     * @author CuiYuxin
     */
    public Branch(String name, String latestCommit) {
        this.name = name;
        this.latestCommit = latestCommit;
    }

    /**
     * Return the latest commit(SHA1).
     * @author CuiYuxin
     */
    public String getLatestCommit() {
        return latestCommit;
    }

    /**
     * Write this Branch.
     * @author CuiYuxin
     */
    public void write() {
        if (!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }
        String sp = File.separator;
        File branchFile = new File(".gitlet" + sp + "branches" + sp + name);
        if (!branchFile.exists()) {
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        Utils.writeObject(branchFile, this);
    }

    /**
     * Return all branches.
     * @author CuiYuxin
     */
    public static List<String> allBranches() {
        return Utils.plainFilenamesIn(BRANCH_DIR);
    }
}
