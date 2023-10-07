# Gitlet 自己实现的本地Git版本控制工具
Berkeley CS61B 数据结构课程项目
## 支持init初始化仓库命令

- **Usage**: `java gitlet.Main init`
- **Description**: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message `initial commit` (just like that, with no punctuation). It will have a single branch: `master`, which initially points to this initial commit, and `master` will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0.) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.
- **Runtime**: Should be constant relative to any significant measure.
- **Failure cases**: If there is already a Gitlet version-control system in the current directory, it should abort. It should NOT overwrite the existing system with a new one. Should print the error message `A Gitlet version-control system already exists in the current directory.`

## 支持add添加文件到暂存区命令
- **Usage**: `java gitlet.Main add [file name]`
- **Description**: Adds a copy of the file as it currently exists to the *staging area* (see the description of the `commit` command). For this reason, adding a file is also called *staging* the file *for addition*. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in `.gitlet`. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version). The file will no longer be staged for removal (see `gitlet rm`), if it was at the time of the command.
- **Runtime**: In the worst case, should run in linear time relative to the size of the file being added and lgN, for N the number of files in the commit.
- **Failure cases**: If the file does not exist, print the error message `File does not exist.` and exit without changing anything.

## 支持commit提交代码命令
- **Usage**: `java gitlet.Main commit [message]`

- **Description**: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be *tracking* the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being *staged for removal* by the `rm` command (below).

  The bottom line: By default a commit has the same file contents as its parent. Files staged for addition and removal are the updates to the commit. Of course, the date (and likely the mesage) will also different from the parent.

  Some additional points about commit:

  - The staging area is cleared after a commit.
  - The commit command never adds, changes, or removes files in the working directory (other than those in the `.gitlet` directory). The `rm` command *will* remove such files, as well as staging them for removal, so that they will be untracked after a `commit`.
  - Any changes made to files after staging for addition or removal are ignored by the `commit` command, which *only* modifies the contents of the `.gitlet` directory. For example, if you remove a tracked file using the Unix `rm` command (rather than Gitlet’s command of the same name), it has no effect on the next commit, which will still contain the (now deleted) version of the file.
  - After the commit command, the new commit is added as a new node in the commit tree.
  - The commit just made becomes the “current commit”, and the head pointer now points to it. The previous head commit is this commit’s parent commit.
  - Each commit should contain the date and time it was made.
  - Each commit has a log message associated with it that describes the changes to the files in the commit. This is specified by the user. The entire message should take up only one entry in the array `args` that is passed to `main`. To include multiword messages, you’ll have to surround them in quotes.
  - Each commit is identified by its SHA-1 id, which must include the file (blob) references of its files, parent reference, log message, and commit time.

- **Runtime**: Runtime should be constant with respect to any measure of number of commits. Runtime must be no worse than linear with respect to the total size of files the commit is tracking. Additionally, this command has a memory requirement: Committing must increase the size of the `.gitlet` directory by no more than the total size of the files staged for addition at the time of commit, not including additional metadata. This means don’t store redundant copies of versions of files that a commit receives from its parent (hint: remember that blobs are content addressable and use the SHA1 to your advantage). You *are* allowed to save whole additional copies of files; don’t worry about only saving diffs, or anything like that.

- **Failure cases**: If no files have been staged, abort. Print the message `No changes added to the commit.` Every commit must have a non-blank message. If it doesn’t, print the error message `Please enter a commit message.` It is *not* a failure for tracked files to be missing from the working directory or changed in the working directory. Just ignore everything outside the `.gitlet` directory entirely.

## 支持rm从暂存区删除文件命令

- **Usage**: `java gitlet.Main rm [file name]`
- **Description**: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do *not* remove it unless it is tracked in the current commit).
- **Runtime**: Should run in constant time relative to any significant measure.
- **Failure cases**: If the file is neither staged nor tracked by the head commit, print the error message `No reason to remove the file.`

## 支持log打印提交记录命令

- **Usage**: `java gitlet.Main log`

- **Description**: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with `git log --first-parent`). This set of commit nodes is called the commit’s *history*. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message. 

- **Runtime**: Should be linear with respect to the number of nodes in head’s history.

- **Failure cases**: None

## 支持global-log打印所有提交记录命令

- **Usage**: `java gitlet.Main global-log`

- **Description**: Like log, except displays information about all commits ever made. The order of the commits does not matter. Hint: there is a useful method in `gitlet.Utils` that will help you iterate over files within a directory.

- **Runtime**: Linear with respect to the number of commits ever made.

## 支持find查找提交记录命令

- **Usage**: `java gitlet.Main find [commit message]`
- **Description**: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the `commit` command below. Hint: the hint for this command is the same as the one for `global-log`.
- **Runtime**: Should be linear relative to the number of commits.
- **Failure cases**: If no such commit exists, prints the error message `Found no commit with that message.`

## 支持status打印当前仓库状态命令

- **Usage**: `java gitlet.Main status`
- **Description**: Displays what branches currently exist, and marks the current branch with a `*`. Also displays what files have been staged for addition or removal. 
- **Runtime**: Make sure this depends only on the amount of data in the working directory plus the number of files staged to be added or deleted plus the number of branches.
- **Failure cases**: None

## 支持checkout提取文件、切换提交记录、切换分支命令

Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you’ll see 3 numbered points. Each corresponds to the respective usage of checkout.

- **Usages**:
  1. `java gitlet.Main checkout -- [file name]`
  2. `java gitlet.Main checkout [commit id] -- [file name]`
  3. `java gitlet.Main checkout [branch name]`
- **Descriptions**:
  1. Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
  2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
  3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see **Failure cases** below).
- **Runtimes**:
  1. Should be linear relative to the size of the file being checked out.
  2. Should be linear with respect to the total size of the files in the commit’s snapshot. Should be constant with respect to any measure involving number of commits. Should be constant with respect to the number of branches.
- **Failure cases**:
  1. If the file does not exist in the previous commit, abort, printing the error message `File does not exist in that commit.` Do not change the CWD.
  2. If no commit with the given id exists, print `No commit with that id exists.` Otherwise, if the file does not exist in the given commit, print the same message as for failure case 1. Do not change the CWD.
  3. If no branch with that name exists, print `No such branch exists.` If that branch is the current branch, print `No need to checkout the current branch.` If a working file is untracked in the current branch and would be overwritten by the checkout, print `There is an untracked file in the way; delete it, or add and commit it first.` and exit; perform this check before doing anything else. Do not change the CWD.

## 支持branch创建分支命令

- **Usage**: `java gitlet.Main branch [branch name]`
- **Description**: Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called “master”.
- **Runtime**: Should be constant relative to any significant measure.
- **Failure cases**: If a branch with the given name already exists, print the error message `A branch with that name already exists.`

## 支持rm-branch删除分支命令

- **Usage**: `java gitlet.Main rm-branch [branch name]`
- **Description**: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
- **Runtime**: Should be constant relative to any significant measure.
- **Failure cases**: If a branch with the given name does not exist, aborts. Print the error message `A branch with that name does not exist.` If you try to remove the branch you’re currently on, aborts, printing the error message `Cannot remove the current branch.`

## 支持reset恢复到提交记录命令

- **Usage**: `java gitlet.Main reset [commit id]`
- **Description**: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The `[commit id]` may be abbreviated as for `checkout`. The staging area is cleared. The command is essentially `checkout` of an arbitrary commit that also changes the current branch head.
- **Runtime**: Should be linear with respect to the total size of files tracked by the given commit’s snapshot. Should be constant with respect to any measure involving number of commits.

## 支持merge合并分支命令

- **Usage**: `java gitlet.Main merge [branch name]`
- **Description**: Merges files from the given branch into the current branch.
- **Failure cases**: If there are staged additions or removals present, print the error message `You have uncommitted changes.` and exit. If a branch with the given name does not exist, print the error message `A branch with that name does not exist.` If attempting to merge a branch with itself, print the error message `Cannot merge a branch with itself.` If merge would generate an error because the commit that it does has no changes in it, just let the normal commit error message for this go through. If an untracked file in the current commit would be overwritten or deleted by the merge, print `There is an untracked file in the way; delete it, or add and commit it first.` and exit; perform this check before doing anything else.

# 通过测试得分Autograder Score 1528.296 /1536.0

### Failed Tests

- [36a) merge-parent2 (0/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#merge-parent2)
- [37) reset1 (0/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#reset1)

### Passed Tests

- [Velocity Limiting (0/0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#Velocity Limiting)
- [File Checking (0/0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#File Checking)
- [Compilation (0/0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#Compilation)
- [Style (0/0)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#Style)
- [01) init (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#init)
- [02) basic-checkout (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#basic-checkout)
- [03) basic-log (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#basic-log)
- [04) prev-checkout (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#prev-checkout)
- [10) init-err (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#init-err)
- [11) basic-status (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#basic-status)
- [12) add-status (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#add-status)
- [13) remove-status (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#remove-status)
- [14) add-remove-status (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#add-remove-status)
- [15) remove-add-status (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#remove-add-status)
- [16) empty-commit-err (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#empty-commit-err)
- [17) empty-commit-message-err (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#empty-commit-message-err)
- [18) nop-add (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#nop-add)
- [19) add-missing-err (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#add-missing-err)
- [20) status-after-commit (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#status-after-commit)
- [21) nop-remove-err (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#nop-remove-err)
- [22) remove-deleted-file (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#remove-deleted-file)
- [23) global-log (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#global-log)
- [24) global-log-prev (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#global-log-prev)
- [25) successful-find (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#successful-find)
- [26) successful-find-orphan (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#successful-find-orphan)
- [27) unsuccessful-find-err (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#unsuccessful-find-err)
- [28) checkout-detail (29.63/29.63)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#checkout-detail)
- [29) bad-checkouts-err (44.444/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#bad-checkouts-err)
- [30) branches (44.444/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#branches)
- [30a) rm-branch (44.444/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#rm-branch)
- [31) duplicate-branch-err (44.444/44.444)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#duplicate-branch-err)
- [31a) rm-branch-err (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#rm-branch-err)
- [32) file-overwrite-err (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#file-overwrite-err)
- [33) merge-no-conflicts (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#merge-no-conflicts)
- [34) merge-conflicts (74.074/74.074)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#merge-conflicts)
- [35) merge-rm-conflicts (74.074/74.074)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#merge-rm-conflicts)
- [36) merge-err (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#merge-err)
- [38) bad-resets-err (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#bad-resets-err)
- [39) short-uid (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#short-uid)
- [40) special-merge-cases (59.259/59.259)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#special-merge-cases)
- [41) no-command-err (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#no-command-err)
- [42) other-err (14.815/14.815)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#other-err)
- [43) bai-merge (88.889/88.889)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#bai-merge)
- [101) ec-untracked (32/32)](https://www.gradescope.com/courses/137626/assignments/1473129/submissions/124716665?view=results#ec-untracked)
