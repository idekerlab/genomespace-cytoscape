package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;

import cytoscape.genomespace.util.GSUtils;

public class DownloadFileFromGenomeSpaceTask extends AbstractTask{
	
	private GSUtils gsUtils;
	private GSFileMetadata fileMetadata;
	private GSDataFormat dataFormat;
	private File targetLocalFile;
	private boolean overwriteIfExists;
	
	public DownloadFileFromGenomeSpaceTask(GSUtils gsUtils, GSFileMetadata fileMetadata, File targetLocalFile,
		    boolean overwriteIfExists) {
		this(gsUtils, fileMetadata, null, targetLocalFile, overwriteIfExists);
	}
	
	public DownloadFileFromGenomeSpaceTask(GSUtils gsUtils, GSFileMetadata fileMetadata, GSDataFormat dataFormat, File targetLocalFile,
	    boolean overwriteIfExists) {
		this.gsUtils = gsUtils;
		this.fileMetadata = fileMetadata;
		this.dataFormat = dataFormat;
		this.targetLocalFile = targetLocalFile;
		this.overwriteIfExists = overwriteIfExists;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final GsSession client = gsUtils.getSession(); 
		final DataManagerClient dataManagerClient = client.getDataManagerClient();
		dataManagerClient.downloadFile(fileMetadata, dataFormat, targetLocalFile, overwriteIfExists);
	}

}
