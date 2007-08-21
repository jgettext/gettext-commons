package org.xnap.commons.ant.gettext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

public class GettextExtractKeysTask extends AbstractGettextTask {

    protected Vector<FileSet> filesets = new Vector<FileSet>();
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }
    private String xgettextCommand = "xgettext";
    public void setXgettextCommand(String xgettextCommand) {
        this.xgettextCommand = xgettextCommand;
    }
    
    private String encoding = "utf-8";
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    private String keywords = "-kgetStringResource -kgetFormattedStringResource -ktrc -ktr -kmarktr -ktrn:1,2";
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    private void checkPreconditions() throws BuildException {
        if (poDirectory == null) {
            throw new BuildException("poDirectory must be set for xgettext");
        }
        if (filesets.isEmpty()) {
            throw new BuildException("at least one fileset must be specified to search for .java files in");
        }
    }
    
    public void execute() {
     
        checkPreconditions();
        
        ArrayList<String> files = new ArrayList<String>();
        for (FileSet fileSet : filesets) {
            DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
            String names[] = scanner.getIncludedFiles();
            File parent = fileSet.getDir(getProject());
            for (String name : names) {
                files.add(getAbsolutePath(name, parent));
            }
        }
        File file = createListFile(files);
        
        Commandline cl = new Commandline();
        cl.setExecutable(xgettextCommand);
        cl.createArgument().setValue("-c");
        cl.createArgument().setValue("--from-code=" + encoding);
        cl.createArgument().setValue("--output=" + new File(poDirectory, keysFile).getAbsolutePath());
        cl.createArgument().setValue("--language=Java");
        cl.createArgument().setLine(keywords);
        
        cl.createArgument().setValue("--files-from=" + file.getAbsolutePath());
        
        log("Executing: " + cl.toString());
        
        try {
            Runtime.getRuntime().exec(cl.getCommandline());
        } catch (IOException e) {
            log(e.getMessage());
        }
    }
    
    private File createListFile(List<String> files) {
        try {
            File listFile = File.createTempFile("srcfiles", null);
            log(listFile.getAbsolutePath());
            listFile.deleteOnExit();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(listFile));
            try {
                for (String file : files) {
                    writer.write(file);
                    writer.newLine();
                }                
            } finally {
                writer.close();
            }
            
            return listFile;
        } catch (IOException e) {
            log(e.getMessage());
            return null;
        }
    }
    
    private String getAbsolutePath(String path, File file) {
        return file.getAbsolutePath() + File.separator + path;
    }
    
}
