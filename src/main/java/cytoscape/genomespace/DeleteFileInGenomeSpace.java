package cytoscape.genomespace;


import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeleteFileInGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 4234432889999989L;
	private static final Logger logger = LoggerFactory.getLogger(DeleteFileInGenomeSpace.class);
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public DeleteFileInGenomeSpace(GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("Delete File in GenomeSpace");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace");
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			final GsSession session = gsUtils.getSession(); 
			final DataManagerClient dataManagerClient = session.getDataManagerClient();
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							GSFileBrowserDialog.DialogType.FILE_DELETION_DIALOG);
		} catch (Exception ex) {
			logger.error("GenomeSpace failed",ex);
		}
	}

	private String getSelectedFile(Collection<String> names) {
		String s = (String)JOptionPane.showInputDialog(
                    frame, "Select a file to delete:",
                    "Delete from GenomeSpace",
                    JOptionPane.WARNING_MESSAGE,
                    null, names.toArray() ,null);
		return s;
	}
}
