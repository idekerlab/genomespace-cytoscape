package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;

import cytoscape.genomespace.util.GSUtils;

public class UploadFileToGenomeSpaceTask extends AbstractTask {
	
	private GSUtils gsUtils;
	private File localFile;
	private String remoteFile;
	
	public UploadFileToGenomeSpaceTask(GSUtils gsUtils, File localFile, String remoteFile) {
		this.gsUtils = gsUtils;
		this.localFile = localFile;
		this.remoteFile = remoteFile;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final GsSession client = gsUtils.getSession(); 
		final DataManagerClient dataManagerClient = client.getDataManagerClient();
		dataManagerClient.uploadFile(localFile, remoteFile);
	}

}
