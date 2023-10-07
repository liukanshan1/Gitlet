package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 * @author CuiYuxin
 */
public class Repository implements Serializable {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The current commit. */
    private String head;
    /** The current branch. */
    private String branch;

    /**
     * Constructs a new repository.
     * @author CuiYuxin
     */
    public Repository() {
        if (GITLET_DIR.exists()) {
            String sp = File.separator;
            File repo = new File(".gitlet" + sp + "REPO");
            if (repo.exists()) {
                Repository repoObj = Utils.readObject(repo, Repository.class);
                head = repoObj.head;
                branch = repoObj.branch;
            }
        }
    }

    /**
     * Init a gitlet repository.
     * @author CuiYuxin
     */
    public void initGitlet() {
        if (!GITLET_DIR.exists()) {
            //create .gitlet directory
            GITLET_DIR.mkdir();
            //create init commit
            Commit cmt = new Commit();
            cmt.initCommit();
            head = cmt.write();
            //create master branch
            Branch br = new Branch("master", Utils.sha1(Utils.serialize(cmt)));
            br.write();
            branch = "master";
            //update repository status
            write();
        } else {
            String e = "A Gitlet version-control system already exists in the current directory.";
            System.out.println(e);
            System.exit(0);
        }
    }

    /**
     * Add a file to the staging area.
     * @author CuiYuxin
     */
    public void add(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        //copy file to blob directory
        Blob blob = new Blob(file);
        String blobName = blob.write();
        //update staging area
        File stageFile = new File(".gitlet/stage");
        Stage stage = new Stage();
        stage.add(fileName, blobName, head);
        stage.write();
    }

    /**
     * Creating a new commit.
     * @author CuiYuxin
     */
    public void commit(String message) {
        if (message == null || message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit oldCmt = Commit.read(head); //read old commit
        Stage stage = new Stage(); //read stage
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Map<String, String> cmtMap = Commit.mergeBlobs(stage, oldCmt);
        Commit cmt = new Commit(message, head, "", cmtMap); //create new commit
        head = cmt.write(); //update head
        stage.clear(); //clear stage
        //read current branch and update branch
        Branch br = Branch.read(branch);
        br.update(head);
        br.write();
        write(); //update repository status
    }

    /**
     * Remove a file from the staging area.
     * @author CuiYuxin
     */
    public void rm(String fileName) {
        //update staging area
        File stageFile = new File(".gitlet/stage");
        Stage stage;
        if (stageFile.exists()) {
            stage = Utils.readObject(stageFile, Stage.class);
        } else {
            stage = new Stage();
        }
        stage.rm(fileName, head);
        stage.write();
    }

    /**
     * Print the log the current branch.
     * @author CuiYuxin
     */
    public void log() {
        String cmtID = head;
        while (!cmtID.equals("")) {
            Commit cmt = Commit.read(cmtID);
            System.out.println(cmt.toString());
            cmtID = cmt.getParent();
        }
    }

    /**
     * Print all commit.
     * @author CuiYuxin
     */
    public void globalLog() {
        List<String> cmtLogs = Commit.getCommitLog();
        for (String cmtLog : cmtLogs) {
            System.out.print(cmtLog);
            System.out.print("\n");
        }
    }

    /**
     * Print the status of the repository.
     * @author CuiYuxin
     */
    public void status() {
        // Branch status
        System.out.println("=== Branches ===");
        for (String branchName : Branch.allBranches()) {
            if (branchName.equals(this.branch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        // Staged files
        Stage stage = new Stage();
        System.out.println("=== Staged Files ===");
        for (String fileName : stage.getStagedFiles()) {
            System.out.println(fileName);
        }
        System.out.println();
        // Removed files
        System.out.println("=== Removed Files ===");
        for (String fileName : stage.getRemovedFiles()) {
            System.out.println(fileName);
        }
        System.out.println();
        // Unstaged files
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : stage.getUnstagedFiles(head)) {
            System.out.println(fileName);
        }
        System.out.println();
        // Untracked files
        System.out.println("=== Untracked Files ===");
        for (String fileName : stage.getUntrackedFiles(head)) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /**
     * Find the commit which has given message.
     * @author CuiYuxin
     */
    public void find(String message) {
        List<String> cmtID = Commit.find(message);
        if (cmtID.size() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        for (String cmt : cmtID) {
            System.out.println(cmt);
        }
    }


    /** Write repository status to disk.
     *  @author CuiYuxin */
    public void write() {
        String sp = File.separator;
        File repo = new File(".gitlet" + sp + "REPO");
        if (!repo.exists()) {
            try {
                repo.createNewFile();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        Utils.writeObject(repo, this);
    }

    /**
     * Check if current folder is a repository.
     * @author CuiYuxin
     */
    public static boolean isRepo() {
        return GITLET_DIR.exists();
    }

    /**
     * Checkout file in given commit.
     * @author CuiYuxin
     */
    public void checkout(String cmtID, String fileName) {
        if (cmtID.equals("head")) {
            cmtID = head;
        }
        Commit cmt = Commit.read(cmtID);
        if (cmt == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!cmt.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String fileID = cmt.getBlobs().get(fileName);
        byte[] fileContent = Blob.getBlob(fileID);
        Utils.writeContents(new File(fileName), fileContent);
    }

    /**
     * Checkout branch.
     * @author CuiYuxin
     */
    public void checkout(String branchName) {
        if (branchName.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Branch br = Branch.read(branchName);
        if (br == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        helpCheck(br.getLatestCommit());
        // update repo
        branch = branchName;
        head = br.getLatestCommit();
        write();
        // update stage
        Stage stage = new Stage();
        stage.clear();
        stage.write();
    }

    /**
     * A helping method for checkout.
     * @author CuiYuxin
     */
    private void helpCheck(String commitID) {
        // other branch's latest commit
        Commit cmt = Commit.read(commitID);
        Set<String> cmtFiles = cmt.getBlobs().keySet();
        // current branch's latest commit
        Commit curCmt = Commit.read(head);
        Set<String> curCmtFiles = curCmt.getBlobs().keySet();
        checkOverwritten(cmt, curCmt);
        for (String file : cmtFiles) {
            byte[] fileContent = Blob.getBlob(cmt.getBlobs().get(file));
            Utils.writeContents(new File(file), fileContent);
        }
        for (String file : curCmtFiles) {
            if (!cmtFiles.contains(file)) {
                Utils.restrictedDelete(new File(file));
            }
        }
    }

    /**
     * Check if overwritten could happen.
     * @author CuiYuxin
     */
    private void checkOverwritten(Commit otherCmt,  Commit curCmt) {
        // current directory files
        List<String> curDirFiles = Utils.plainFilenamesIn(CWD);
        // other branch's latest files
        Set<String> cmtFiles = otherCmt.getBlobs().keySet();
        for (String fileName : curDirFiles) {
            if (!curCmt.getBlobs().containsKey(fileName)) {
                if (cmtFiles.contains(fileName)) {
                    String fileID = otherCmt.getBlobs().get(fileName);
                    if (!fileID.equals(Utils.readContents(new File(fileName)))) {
                        System.out.print("There is an untracked file in the way;");
                        System.out.print(" delete it, or add and commit it first.\n");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /**
     * Create a new branch.
     * @author CuiYuxin
     */
    public void createBranch(String branchName) {
        if (Branch.allBranches().contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch br = new Branch(branchName, head);
        br.write();
    }

    /**
     * Remove a branch.
     * @author CuiYuxin
     */
    public void removeBranch(String branchName) {
        if (!Branch.allBranches().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        Branch.remove(branchName);
    }

    /**
     * Checkout a commit.
     * @author CuiYuxin
     */
    public void reset(String commitID) {
        Commit cmt = Commit.read(commitID);
        if (cmt == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        helpCheck(commitID);
        // update repo
        head = Utils.sha1(Utils.serialize(cmt));
        write();
        // update stage
        Stage stage = new Stage();
        stage.clear();
        stage.write();
    }

    /**
     * Merge two branches.
     * @author CuiYuxin
     */
    public void merge(String branchName) {
        Stage stage = new Stage();
        Branch otherBranch = Branch.read(branchName);
        String splitID = checkFailCases(branchName, stage, otherBranch);
        Commit split = Commit.read(splitID);
        Commit other = Commit.read(otherBranch.getLatestCommit());
        Commit headCmt = Commit.read(this.head);
        checkOverwritten(other, headCmt);
        Map<String, String> headBlobs = headCmt.getBlobs();
        Map<String, String> otherBlobs = other.getBlobs();
        Map<String, String> splitBlobs = split.getBlobs();
        List<String> curFiles = Utils.plainFilenamesIn(CWD);
        boolean conflict = false;
        for (String file : headBlobs.keySet()) {
            if (splitBlobs.getOrDefault(file, "").equals(headBlobs.get(file))) {
                if (!otherBlobs.containsKey(file)) { //6
                    stage.rm(file, head);
                    continue;
                } else if (!otherBlobs.get(file).equals(headBlobs.get(file))) { //1
                    checkout(otherBranch.getLatestCommit(), file);
                    stage.add(file, otherBlobs.get(file), head);
                    continue;
                }
            }
            if (!otherBlobs.containsKey(file) && !splitBlobs.containsKey(file)) { //4
                continue;
            }
            if (otherBlobs.getOrDefault(file, "").equals(headBlobs.get(file))) { //3.1
                continue;
            }
            String splitID2 = splitBlobs.getOrDefault(file, "");
            if (!otherBlobs.getOrDefault(file, "").equals(headBlobs.get(file))) { //3.2
                if (splitBlobs.getOrDefault(file, "").equals(headBlobs.get(file))) {
                    continue;
                } else if (splitID2.equals(otherBlobs.getOrDefault(file, ""))) {
                    continue;
                } else {
                    conflict = true;
                    dC(stage, headBlobs, otherBlobs, file);
                }
            }
        }
        for (String file : otherBlobs.keySet()) {
            if (splitBlobs.getOrDefault(file, "").equals(otherBlobs.get(file))) {
                if (!headBlobs.containsKey(file)) { //7
                    continue;
                } else if (!headBlobs.getOrDefault(file, "").equals(otherBlobs.get(file))) { //2
                    continue;
                }
            }
            if (!headBlobs.containsKey(file) && !splitBlobs.containsKey(file)) { //5
                checkout(otherBranch.getLatestCommit(), file);
                stage.add(file, otherBlobs.get(file), head);
                continue;
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        String message = "Merged " + branchName + " into " + this.branch + ".";
        Map<String, String> cmtMap = Commit.mergeBlobs(stage, headCmt);
        Commit commit = new Commit(message, head, otherBranch.getLatestCommit(), cmtMap);
        head = commit.write(); //update head
        stage.clear(); //clear stage and write to disk
        Branch br = Branch.read(branch);
        br.update(head);
        br.write();
        write(); //update repository status
    }

    /**
     * Deal with conflicts.
     * @author CuiYuxin
     */
    private void dC(Stage s, Map<String, String> hB, Map<String, String> oB, String f) {
        byte[] file1 = Blob.getBlob(hB.get(f));
        String content1 = new String(file1, StandardCharsets.UTF_8);
        String content2;
        if (!oB.containsKey(f)) {
            content2 = "";
        } else {
            byte[] file2 = Blob.getBlob(oB.get(f));
            content2 = new String(file2, StandardCharsets.UTF_8);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<<<<<<< HEAD\n");
        sb.append(content1);
        sb.append("=======\n");
        sb.append(content2);
        sb.append(">>>>>>>\n");
        File conFile = new File(f);
        Utils.writeContents(conFile, sb.toString());
        Blob conBlob = new Blob(conFile);
        s.add(f, conBlob.write(), head);
    }

    /**
     * Check merge failure cases.
     * @author CuiYuxin
     */
    private String checkFailCases(String branchName, Stage stage, Branch otherBranch) {
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (otherBranch == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String splitID = findSplitPoint(head, otherBranch.getLatestCommit());
        if (splitID.equals(otherBranch.getLatestCommit())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitID.equals(head)) {
            System.out.println("Current branch fast-forwarded.");
            checkout(branchName);
            System.exit(0);
        }
        return splitID;
    }

    /**
     * Find two branches split point.
     *  @author CuiYuxin
     */
    private String findSplitPoint(String cmt1, String cmt2) {
        List<String> cmt1List = new ArrayList<>();
        while (!cmt1.equals("")) {
            cmt1List.add(cmt1);
            Commit cmt = Commit.read(cmt1);
            cmt1 = cmt.getParent();
        }
        while (!cmt2.equals("")) {
            if (cmt1List.contains(cmt2)) {
                return cmt2;
            }
            Commit cmt = Commit.read(cmt2);
            cmt2 = cmt.getParent();
        }
        return "";
    }
}
