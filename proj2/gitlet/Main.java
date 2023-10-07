package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author CuiYuxin
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.print("Please enter a command.\n");
            System.exit(0);
        }
        String firstArg = args[0];
        Repository repo = new Repository();
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                repo.initGitlet();
                break;
            case "add":
                validateNumArgs(args, 2);
                repo.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                repo.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                repo.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                repo.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                repo.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                repo.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                repo.status();
                break;
            case "checkout":
                validateNumArgs(args, 2);
                if (args.length == 2) {
                    repo.checkout(args[1]);
                } else if (args.length == 3) {
                    repo.checkout("head", args[2]);
                } else if (args.length == 4) {
                    repo.checkout(args[1], args[3]);
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                repo.createBranch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                repo.removeBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                repo.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                repo.merge(args[1]);
                break;
            default:
                System.out.print("No command with that name exists.\n");
                System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws an error if they do not match.
     * @author CuiYuxin
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (!args[0].equals("init") && !Repository.isRepo()) {
            System.out.print("Not in an initialized Gitlet directory.\n");
            System.exit(0);
        }
        if (args[0].equals("checkout")) {
            if (args.length == 2) {
                return;
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.print("Incorrect operands.\n");
                    System.exit(0);
                }
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.print("Incorrect operands.\n");
                    System.exit(0);
                }
            } else {
                System.out.print("Incorrect operands.\n");
                System.exit(0);
            }
            return;
        }
        if (args.length != n) {
            System.out.print("Incorrect operands.\n");
            System.exit(0);
        }
    }
}
