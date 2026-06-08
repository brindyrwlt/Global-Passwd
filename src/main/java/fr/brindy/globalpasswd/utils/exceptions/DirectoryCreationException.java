package fr.brindy.globalpasswd.utils.exceptions;

import java.nio.file.FileSystemException;

public class DirectoryCreationException extends FileSystemException {
    public DirectoryCreationException() {
        super("plugins/GlobalPasswd", null, "Cannot create plugin directory.");
    }
}
