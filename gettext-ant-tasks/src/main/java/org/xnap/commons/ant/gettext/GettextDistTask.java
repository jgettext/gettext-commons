package org.xnap.commons.ant.gettext;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;

public class GettextDistTask extends AbstractGettextTask {
    
    /**
     * msgcat command.
     */
    protected String msgcatCmd = "msgcat";
    public void setMsgcatCmd(String msgcatCmd) {
        this.msgcatCmd = msgcatCmd;
    }
    
    /**
     * @description msgfmt command.
     * @parameter expression="${msgfmtCmd}" default-value="msgfmt"
     * @required 
     */
    protected String msgfmtCmd = "msgfmt";
    public void setMsgfmtCmd(String msgfmtCmd) {
        this.msgfmtCmd = msgfmtCmd;
    }
    
    /**
     * @description target package.
     * @parameter expression="${targetBundle}"
     * @required 
     */
    protected String targetBundle = "Messages";
    public void setTargetBundle(String targetBundle) {
        this.targetBundle = targetBundle;
    }
    
    /**
     * @description Output format ("class" or "properties")
     * @parameter expression="${outputFormat}" default-value="class"
     * @required 
     */
    protected String outputFormat = "class";
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    /**
     * Java version.
     * Can be "1" or "2".
     * @parameter expression="${javaVersion}" default-value="2"
     * @required
     */
    protected String javaVersion = "2";
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    /**
     * @parameter expression="${sourceLocale}" default-value="en"
     * @required
     */
    protected String sourceLocale = "en";
    
    public void setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
    }
    
    protected String outputDirectory;
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    

    public void execute() {
        
        CommandlineFactory cf = null;
        if ("class".equals(outputFormat)) {
            cf = new MsgFmtCommandlineFactory();
        } else if ("properties".equals(outputFormat)) {
            cf = new MsgCatCommandlineFactory();
        } else  
            throw new BuildException("Unknown output format: " 
                    + outputFormat + ". Should be 'class' or 'properties'.");

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(poDirectory);
        ds.setIncludes(new String[] {"**/*.po"});
        ds.scan();
        
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            log("Processing " + files[i]);
            Commandline cl = cf.createCommandline(new File(poDirectory, files[i]));
            log("Executing: " + cl.toString(), Project.MSG_DEBUG);
            try {
                Runtime.getRuntime().exec(cl.getCommandline());
            } catch (IOException e) {
                log("Could not execute " + cl.getExecutable() + "." + e.getMessage(), Project.MSG_ERR);
            }
        }
        
        String basepath = targetBundle.replace('.', File.separatorChar);
        log("Creating resource bundle for source locale", Project.MSG_INFO);
        touch(new File(outputDirectory, basepath + "_" + sourceLocale + ".properties"));
        log("Creating default resource bundle", Project.MSG_INFO);
        touch(new File(outputDirectory, basepath + ".properties"));
    }
        
    private void touch(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log("Could not touch file: " + file.getName() +  e.getMessage(), Project.MSG_WARN);
            }
        }
    }
    
    private interface CommandlineFactory {
        Commandline createCommandline(File file);
    }
    
    private class MsgFmtCommandlineFactory implements CommandlineFactory {
        public Commandline createCommandline(File file) {
            String locale = file.getName().substring(0, file.getName().lastIndexOf('.'));

            Commandline cl = new Commandline();
            cl.setExecutable(msgfmtCmd);
            
            if ("2".equals(javaVersion)) {
                cl.createArgument().setValue("--java2");
            } else {
                cl.createArgument().setValue("--java");
            }
            
            cl.createArgument().setValue("-d");
            cl.createArgument().setValue(outputDirectory);
            cl.createArgument().setValue("-r");
            cl.createArgument().setValue(targetBundle);
            cl.createArgument().setValue("-l");
            cl.createArgument().setValue(locale);
            cl.createArgument().setFile(file);
            log(cl.toString(), Project.MSG_WARN);
            return cl;
        }
    }

    private class MsgCatCommandlineFactory implements CommandlineFactory {
        public Commandline createCommandline(File file) {
            String basepath = targetBundle.replace('.', File.separatorChar);
            String locale = file.getName().substring(0, file.getName().lastIndexOf('.'));
            File target = new File(outputDirectory, basepath + "_" + locale + ".properties");
            Commandline cl = new Commandline();
        
            cl.setExecutable(msgfmtCmd);
        
            cl.createArgument().setValue("--no-location");
            cl.createArgument().setValue("-p");
            cl.createArgument().setFile(file);
            cl.createArgument().setValue("-o");
            cl.createArgument().setFile(target);

            return cl;
        }
     }
    
}
