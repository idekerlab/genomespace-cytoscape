package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//public class LoadOntologyAndAnnotationFromGenomeSpace extends AbstractCyAction {
//	private static final long serialVersionUID = 7571788473486759L;
//	private static final Logger logger = LoggerFactory.getLogger(LoadOntologyAndAnnotationFromGenomeSpace.class);
//
//	public LoadOntologyAndAnnotationFromGenomeSpace() {
//		super("Load Ontology and Annotations...");
//
//		// Set the menu you'd like here.  Plugins don't need
//		// to live in the Plugins menu, so choose whatever
//		// is appropriate!
//		setPreferredMenu("File.Import.GenomeSpace");
//	}
//
//	public void actionPerformed(ActionEvent e) {
//		File tempFile = null;
//		try {
//			final GsSession client = gsContext.getSession(); 
//			final DataManagerClient dataManagerClient = client.getDataManagerClient();
//
//			// Select the GenomeSpace file:
//			final List<String> acceptableExtensions = new ArrayList<String>();
//			final GSFileBrowserDialog browserDialog =
//				new GSFileBrowserDialog(Cytoscape.getDesktop(), dataManagerClient,
//							acceptableExtensions,
//							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
//			final GSFileMetadata fileMetadata = browserDialog.getSelectedFileMetadata();
//			if (fileMetadata == null)
//				return;
//
//			// Download the GenomeSpace file:
//			tempFile = File.createTempFile("temp", "ont");
//			dataManagerClient.downloadFile(fileMetadata, tempFile, true);
//
//			final ImportTextTableDialog dialog =
//				new ImportTextTableDialog(Cytoscape.getDesktop(), tempFile,
//							  fileMetadata.getName(),
//							  ImportTextTableDialog.ONTOLOGY_AND_ANNOTATION_IMPORT);
//			dialog.pack();
//			dialog.setLocationRelativeTo(Cytoscape.getDesktop());
//			dialog.setVisible(true);
//		} catch (Exception ex) {
//			logger.error("GenomeSpace failed", ex);
//			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
//						      ex.getMessage(), "GenomeSpace Error",
//						      JOptionPane.ERROR_MESSAGE);
//		} finally {
//			if (tempFile != null)
//				tempFile.delete();
//		}
//	}
//}
