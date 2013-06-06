package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;

import cytoscape.genomespace.context.GenomeSpaceContext;

public class DownloadFileFromGenomeSpaceTask extends AbstractTask{
	
	private GenomeSpaceContext gsContext;
	private GSFileMetadata fileMetadata;
	private GSDataFormat dataFormat;
	private File targetLocalFile;
	private boolean overwriteIfExists;
	
	public DownloadFileFromGenomeSpaceTask(GenomeSpaceContext gsContext, GSFileMetadata fileMetadata, File targetLocalFile,
		    boolean overwriteIfExists) {
		this(gsContext, fileMetadata, null, targetLocalFile, overwriteIfExists);
	}
	
	public DownloadFileFromGenomeSpaceTask(GenomeSpaceContext gsContext, GSFileMetadata fileMetadata, GSDataFormat dataFormat, File targetLocalFile,
	    boolean overwriteIfExists) {
		this.gsContext = gsContext;
		this.fileMetadata = fileMetadata;
		this.dataFormat = dataFormat;
		this.targetLocalFile = targetLocalFile;
		this.overwriteIfExists = overwriteIfExists;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final GsSession client = gsContext.getSession(); 
		final DataManagerClient dataManagerClient = client.getDataManagerClient();
		dataManagerClient.downloadFile(fileMetadata, dataFormat, targetLocalFile, overwriteIfExists);
	}

}
