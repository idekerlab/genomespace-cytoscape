package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;
import cytoscape.genomespace.task.UploadFileToGenomeSpaceTask;
import cytoscape.genomespace.ui.NetworkTypeSelectionDialog;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class SaveNetworkToGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 9988760123456789L;
	private static final Logger logger = LoggerFactory.getLogger(SaveNetworkToGenomeSpaceAction.class);
	private final CyApplicationManager cyApplicationManager;
	private final DialogTaskManager dialogTaskManager;
	private final ExportNetworkViewTaskFactory exportNetworkViewTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	
	public SaveNetworkToGenomeSpaceAction(CyApplicationManager cyApplicationManager, DialogTaskManager dialogTaskManager, ExportNetworkViewTaskFactory exportNetworkViewTaskFactory, GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("Save Network As");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Export.GenomeSpace");
		this.cyApplicationManager = cyApplicationManager;
		this.dialogTaskManager = dialogTaskManager;
		this.exportNetworkViewTaskFactory = exportNetworkViewTaskFactory;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String networkType =
				(new NetworkTypeSelectionDialog(frame)).getNetworkType();
			if (networkType == null)
				return;
			networkType = networkType.toLowerCase();
			final GsSession client = gsUtils.getSession();
			final DataManagerClient dataManagerClient = client.getDataManagerClient();

			final List<String> acceptableExtensions = new ArrayList<String>();
			acceptableExtensions.add(networkType.toLowerCase());
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.SAVE_AS_DIALOG);

			String saveFileName = dialog.getSaveFileName();
			if (saveFileName == null)
				return;

			// Make sure the file name ends with the network type extension:
			if (!saveFileName.toLowerCase().endsWith("." + networkType))
				saveFileName += "." + networkType;

			final File tempFile =File.createTempFile("tempNetwork", networkType);
			TaskIterator ti = exportNetworkViewTaskFactory.createTaskIterator(cyApplicationManager.getCurrentNetworkView(), tempFile);
			ti.append(new UploadFileToGenomeSpaceTask(gsUtils, tempFile, dialog.getSaveFileName()));
            dialogTaskManager.execute(ti);
            tempFile.delete();

		} catch (final Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
