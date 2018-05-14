package io.onemfive.core.sensors.i2p.bote.folder;

import java.util.List;

public interface EmailFolderManager {

    List<EmailFolder> getEmailFolders();

    boolean deleteEmail(EmailFolder folder, String messageId);
}
