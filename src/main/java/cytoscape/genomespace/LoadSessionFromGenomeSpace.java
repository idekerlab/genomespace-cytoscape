package cytoscape.genomespace;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoadSessionFromGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromGenomeSpace.class);
	private final DialogTaskManager dialogTaskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	
	public LoadSessionFromGenomeSpace(DialogTaskManager dialogTaskManager, OpenSessionTaskFactory openSessionTaskFactory, GSUtils gsUtils, JFrame frame) {
		super("Load Session...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.GenomeSpace");
		this.dialogTaskManager = dialogTaskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			final GsSession client = gsUtils.getSession(); 
			final DataManagerClient dataManagerClient = client.getDataManagerClient();

			// Select the GenomeSpace file:
			final List<String> acceptableExtensions = new ArrayList<String>();
			acceptableExtensions.add("cys");
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;

			// Download the GenomeSpace file:
			final String origFileName = fileMetadata.getName();
			final String extension = gsUtils.getExtension(origFileName);
			File tempFile = File.createTempFile("temp", "." + extension);
			dataManagerClient.downloadFile(fileMetadata, tempFile, true);

			dialogTaskManager.execute(openSessionTaskFactory.createTaskIterator(tempFile));

		} catch (Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}

}

