package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;

public class DownloadFileFromGenomeSpaceTask extends AbstractTask{
	
	private GsSession session;
	private GSFileMetadata fileMetadata;
	private GSDataFormat dataFormat;
	private File targetLocalFile;
	private boolean overwriteIfExists;
	
	public DownloadFileFromGenomeSpaceTask(GsSession session, GSFileMetadata fileMetadata, File targetLocalFile,
		    boolean overwriteIfExists) {
		this(session, fileMetadata, null, targetLocalFile, overwriteIfExists);
	}
	
	public DownloadFileFromGenomeSpaceTask(GsSession session, GSFileMetadata fileMetadata, GSDataFormat dataFormat, File targetLocalFile,
	    boolean overwriteIfExists) {
		this.session = session;
		this.fileMetadata = fileMetadata;
		this.dataFormat = dataFormat;
		this.targetLocalFile = targetLocalFile;
		this.overwriteIfExists = overwriteIfExists;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final DataManagerClient dataManagerClient = session.getDataManagerClient();
		dataManagerClient.downloadFile(fileMetadata, dataFormat, targetLocalFile, overwriteIfExists);
	}

}
