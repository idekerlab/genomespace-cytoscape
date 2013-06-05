package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;


public class LoadNetworkFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;

	public LoadNetworkFromGenomeSpaceAction(DialogTaskManager dialogTaskManager, LoadNetworkFileTaskFactory loadNetworkFileTaskFactory, GSUtils gsUtils, JFrame frame) {
		super("Load Network...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.GenomeSpace");
		this.dialogTaskManager = dialogTaskManager;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			final GsSession session = gsUtils.getSession();
			if(!session.isLoggedIn()) return;
			final DataManagerClient dataManagerClient = session.getDataManagerClient();

			// Select the GenomeSpace file:
			final List<String> acceptableExtensions = new ArrayList<String>();
			acceptableExtensions.add("sif");
			acceptableExtensions.add("xgmml");
			acceptableExtensions.add("gml");
			acceptableExtensions.add("adj");
			acceptableExtensions.add("ndb");
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;
		
			GSDataFormat dataFormat = fileMetadata.getDataFormat();
			if ( dataFormat == null )
				throw new RuntimeException("file metadata has null data format");

			String ext = dataFormat.getFileExtension();
			if ( ext != null && ext.equalsIgnoreCase("adj") )
				dataFormat = gsUtils.findConversionFormat(fileMetadata.getAvailableDataFormats(), "xgmml");

			// Download the GenomeSpace file into a temp file
			final String origFileName = fileMetadata.getName();
			final String extension = gsUtils.getExtension(origFileName);
			File tempFile = File.createTempFile("tempGS", "." + extension);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(gsUtils, fileMetadata, dataFormat, tempFile, true));
			ti.append(loadNetworkFileTaskFactory.createTaskIterator(tempFile));
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch (Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
