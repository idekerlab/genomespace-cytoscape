package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class DownloadFileFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7777788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileFromGenomeSpaceAction.class);
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public DownloadFileFromGenomeSpaceAction(GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("Download File...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace");
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(final ActionEvent e) {
		try {
			final GsSession client = gsUtils.getSession(); 
			final DataManagerClient dataManagerClient = client.getDataManagerClient();

			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;

			final JFileChooser saveChooser = new JFileChooser();
			final File defaultSaveFile =
				new File(System.getProperty("user.home") + File.separator
					 + fileMetadata.getName());
			saveChooser.setSelectedFile(defaultSaveFile);
			if (saveChooser.showSaveDialog(frame)
			    != JFileChooser.APPROVE_OPTION)
				return;

			final File downloadTarget = saveChooser.getSelectedFile();
			dataManagerClient.downloadFile(fileMetadata, downloadTarget, true);
		} catch (Exception ex) {
			logger.error("GenomeSpace failed",ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
