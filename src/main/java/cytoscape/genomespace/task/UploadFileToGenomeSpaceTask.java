package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;

import cytoscape.genomespace.context.GenomeSpaceContext;

public class UploadFileToGenomeSpaceTask extends AbstractTask {
	
	private GenomeSpaceContext gsContext;
	private File localFile;
	private String remoteFile;
	
	public UploadFileToGenomeSpaceTask(GenomeSpaceContext gsContext, File localFile, String remoteFile) {
		this.gsContext = gsContext;
		this.localFile = localFile;
		this.remoteFile = remoteFile;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final GsSession client = gsContext.getSession(); 
		final DataManagerClient dataManagerClient = client.getDataManagerClient();
		dataManagerClient.uploadFile(localFile, remoteFile);
	}

}
