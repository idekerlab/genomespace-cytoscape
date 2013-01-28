package cytoscape.genomespace;


import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.FileUtil;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class UploadFileToGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 9988760123456789L;
	private static final Logger logger = LoggerFactory.getLogger(UploadFileToGenomeSpace.class);
	private final FileUtil fileUtil;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public UploadFileToGenomeSpace(FileUtil fileUtil, GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("Upload File");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Export.GenomeSpace");
		this.fileUtil = fileUtil;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			File f =  fileUtil.getFile(frame, "Select file to upload", FileDialog.LOAD, null);
			if (f == null)
				return;

			GsSession client = gsUtils.getSession(); 
			DataManagerClient dmc = client.getDataManagerClient();

			final String targetDirectoryPath =
				dmc.listDefaultDirectory().getDirectory().getPath();
			final GSFileMetadata uploadedFileMetadata =
				dmc.uploadFile(f, targetDirectoryPath, f.getName());
			if (uploadedFileMetadata != null)
				JOptionPane.showMessageDialog(
						frame,
						f.getName() + " successfully uploaded to GenomeSpace!",
						 "Information", JOptionPane.INFORMATION_MESSAGE);
			
		} catch (final Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
