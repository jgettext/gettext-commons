package org.xnap.commons.ant.gettext;

import org.apache.tools.ant.Task;

public class AbstractGettextTask extends Task {

    /**
     * PO directory.
     */
    protected String poDirectory;
    public void setPoDirectory(String poDirectory) {
        this.poDirectory = poDirectory;
    }


    /**
     * Filename of the .pot file
     */
    protected String keysFile = "keys.pot";
    public void setKeysFile(String keysFile) {
        this.keysFile = keysFile;
    }
    
}
