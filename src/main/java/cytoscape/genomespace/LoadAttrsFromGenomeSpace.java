package cytoscape.genomespace;


import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoadAttrsFromGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromGenomeSpace.class);
	private final DialogTaskManager dialogTaskManager;
	private final LoadTableFileTaskFactory loadTableFileTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public LoadAttrsFromGenomeSpace(DialogTaskManager dialogTaskManager, LoadTableFileTaskFactory loadTableFileTaskFactory, GSUtils gsUtils, JFrame frame) {
		super("Load Attributes...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.GenomeSpace");
		this.dialogTaskManager = dialogTaskManager;
		this.loadTableFileTaskFactory = loadTableFileTaskFactory;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		File tempFile = null;
		try {
			final GsSession client = gsUtils.getSession(); 
			final DataManagerClient dataManagerClient = client.getDataManagerClient();

			// Select the GenomeSpace file:
			final GSFileBrowserDialog dialog =
					new GSFileBrowserDialog(frame, dataManagerClient,
								GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;

			// Download the GenomeSpace file:
			final String fileName = fileMetadata.getName();
			tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
			dataManagerClient.downloadFile(fileMetadata, tempFile, true);
			dialogTaskManager.execute(loadTableFileTaskFactory.createTaskIterator(tempFile));
		} catch (Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
	}

}
