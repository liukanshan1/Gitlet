package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.join;

/**
 * Represents files.
 * @author CuiYuxin
 */
public class Blob implements Serializable {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The Blob directory. */
    public static final File BLOB_DIR = join(CWD, ".gitlet", "blobs");
    /** The Blob file. */
    byte[] blob;

    /**
     * Constructor.
     * @author CuiYuxin
     */
    public Blob(File file) {
        blob = Utils.readContents(file);
        if (!BLOB_DIR.exists()) {
            BLOB_DIR.mkdir();
        }
    }

    /** Write the blob to disk and return file name(SHA1).
     *  @author CuiYuxin
     */
    public String write() {
        String sp = File.separator;
        String fileName = Utils.sha1(Utils.serialize(this));
        File blobFile = new File(".gitlet" + sp + "blobs" + sp + fileName);
        if (!blobFile.exists()) {
            try {
                blobFile.createNewFile();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            Utils.writeObject(blobFile, this);
        }
        return fileName;
    }

    /**
     * Get the file.
     * @author CuiYuxin
     */
    public static byte[] getBlob(String blobID) {
        String sp = File.separator;
        File blob = new File(".gitlet" + sp + "blobs" + sp + blobID);
        Blob b = Utils.readObject(blob, Blob.class);
        return b.blob;
    }
}
