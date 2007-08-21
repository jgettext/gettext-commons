package org.xnap.commons.ant.gettext;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;

public class GettextMergeKeysTask extends AbstractGettextTask { 
    
    /**
     * @description msgcat command.
     * @parameter expression="${msgmergeCmd}" default-value="msgmerge"
     * @required 
     */
    protected String msgmergeCmd = "msgcat";
    public void setMsgmergeCmd(String msgmergeCmd) {
        this.msgmergeCmd = msgmergeCmd;
    }
    
    private void checkPreconditions() throws BuildException {
        if (poDirectory == null) {
            throw new BuildException("poDirectory must be set for msgmerge");
        }
    }
    
    public void execute() {
        checkPreconditions();
        log("Invoking msgmerge for po files in '" 
                + poDirectory + "'.");
        
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(poDirectory);
        ds.setIncludes(new String[] {"**/*.po"});
        ds.scan();
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            log("Processing " + files[i]);
            Commandline cl = new Commandline();
            cl.setExecutable(msgmergeCmd);
            cl.createArgument().setValue("-q");
            cl.createArgument().setValue("--backup=numbered");
            cl.createArgument().setValue("-U");
            cl.createArgument().setFile(new File(poDirectory, files[i]));
            cl.createArgument().setValue(new File(poDirectory, keysFile).getAbsolutePath());
            
            log("Executing: " + cl.toString(), Project.MSG_DEBUG);
            
            try {
                Runtime.getRuntime().exec(cl.getCommandline());
            } catch (IOException e) {
                log(e.getMessage(), Project.MSG_ERR);
            }
        }
    }
}
