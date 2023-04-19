package org.ufba.raide.java.filedetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;

public class JGitBlame {
    
    public static String getCommit(String gitDirPath, String fileName, int lineNumber) throws Exception {

        // inicializa o objeto Git
        Git git = Git.open(new File(gitDirPath));

        // obtém o ObjectId do HEAD
        ObjectId head = git.getRepository().resolve(Constants.HEAD);

        // obtém a lista de commits
        Iterable<RevCommit> commits =  git.log().all().call();

        // inicializa o RevWalk
        RevWalk revWalk = new RevWalk(git.getRepository());
        //---------------
        String result = getHash(gitDirPath,  fileName, lineNumber, git, revWalk);
        
        // fecha o RevWalk e o repositório
        revWalk.close();
        git.getRepository().close();
        return result;
    }
    
    public static String getHash(String gitDirPath,  String fileName, int lineNumber, Git git, RevWalk revWalk) throws Exception {
    	// obtém a Blame para a linha especificada
        BlameResult blameResult = git.blame().setFilePath(fileName).setStartCommit(git.getRepository().resolve(Constants.HEAD)).call();
        RevCommit commit = revWalk.parseCommit(blameResult.getSourceCommit(lineNumber - 1));
        String author = commit.getAuthorIdent().getName();

        // imprime o autor e a mensagem de commit
        //System.out.println("Autor: " + author);
        //System.out.println("Mensagem de commit: " + commit.getFullMessage());
        //System.out.println("Id do commit: " + commit.getName());
        return commit.getName();
    }
    
}
