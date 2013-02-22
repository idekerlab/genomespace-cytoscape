package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSFileMetadata;

import cytoscape.genomespace.GSUtils;

public class UploadFileToGenomeSpaceTask extends AbstractTask {
	
	private GSUtils gsUtils;
	private File localFile;
	private GSFileMetadata gsRemoteFile;
	
	public UploadFileToGenomeSpaceTask(GSUtils gsUtils, File localFile, GSFileMetadata gsRemoteFile) {
		this.gsUtils = gsUtils;
		this.localFile = localFile;
		this.gsRemoteFile = gsRemoteFile;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final GsSession client = gsUtils.getSession(); 
		final DataManagerClient dataManagerClient = client.getDataManagerClient();
		
		dataManagerClient.uploadFile(localFile, gsRemoteFile.getPath(), gsRemoteFile.getName());
	}

}
