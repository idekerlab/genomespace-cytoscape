package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;
import cytoscape.genomespace.task.BasicFileTaskFactory;


public class ImportNetworkFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkFromGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final BasicFileTaskFactory importNetworkFileTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;

	public ImportNetworkFromGenomeSpaceAction(DialogTaskManager dialogTaskManager, BasicFileTaskFactory importNetworkFileTaskFactory, GenomeSpaceContext gsContext, JFrame frame) {
		super("GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.Network");
		setMenuGravity(3.0f);
		this.dialogTaskManager = dialogTaskManager;
		this.importNetworkFileTaskFactory = importNetworkFileTaskFactory;
		this.gsContext = gsContext;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(!gsContext.loginIfNotAlready()) return;
			final GsSession session = gsContext.getSession();
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
							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG, 
							"Import Network from GenomeSpace");
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;
		
			GSDataFormat dataFormat = fileMetadata.getDataFormat();
			if ( dataFormat == null )
				throw new RuntimeException("file metadata has null data format");

			String ext = dataFormat.getFileExtension();
			if ( ext != null && ext.equalsIgnoreCase("adj") )
				dataFormat = gsContext.findConversionFormat(fileMetadata.getAvailableDataFormats(), "xgmml");

			// Download the GenomeSpace file into a temp file
			final String fileName = fileMetadata.getName();
			File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(session, fileMetadata, dataFormat, tempFile, true));
			ti.append(importNetworkFileTaskFactory.createTaskIterator(tempFile));
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
