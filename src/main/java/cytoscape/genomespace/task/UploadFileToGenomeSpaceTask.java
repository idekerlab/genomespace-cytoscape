package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;

import cytoscape.genomespace.context.GenomeSpaceContext;

public class UploadFileToGenomeSpaceTask extends AbstractTask {
	
	private GsSession session;
	private File localFile;
	private String remoteFile;
	
	public UploadFileToGenomeSpaceTask(GsSession session, File localFile, String remoteFile) {
		this.session = session;
		this.localFile = localFile;
		this.remoteFile = remoteFile;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final DataManagerClient dataManagerClient = session.getDataManagerClient();
		dataManagerClient.uploadFile(localFile, remoteFile);
	}

}
