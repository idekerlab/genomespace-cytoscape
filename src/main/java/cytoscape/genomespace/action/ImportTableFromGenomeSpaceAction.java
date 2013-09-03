package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.BasicFileTaskFactory;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;


public class ImportTableFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(ImportTableFromGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final BasicFileTaskFactory importTableFileTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final BundleContext bc;
	private final JFrame frame;
	
	public ImportTableFromGenomeSpaceAction(CyApplicationManager cyApplicationManager, CyNetworkViewManager cyNetworkViewManager, DialogTaskManager dialogTaskManager, BasicFileTaskFactory importTableFileTaskFactory, GenomeSpaceContext gsContext, BundleContext bc, JFrame frame, ImageIcon icon) {
		super("GenomeSpace...", cyApplicationManager, "network", cyNetworkViewManager);

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.Table");
		setMenuGravity(1.3f);
		putValue(SMALL_ICON, icon);

		this.dialogTaskManager = dialogTaskManager;
		this.importTableFileTaskFactory = importTableFileTaskFactory;
		this.gsContext = gsContext;
		this.bc = bc;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(!gsContext.loginIfNotAlready()) return;
			final GsSession session = gsContext.getSession(); 
			final DataManagerClient dataManagerClient = session.getDataManagerClient();
			ServiceReference[] readerServiceReferences = bc.getServiceReferences(InputStreamTaskFactory.class.getName(), null);
			
			// Select the GenomeSpace file:
			final Set<String> acceptableExtensions = new HashSet<String>();
			for(ServiceReference readerServiceReference: readerServiceReferences) {
				InputStreamTaskFactory tf = (InputStreamTaskFactory) bc.getService(readerServiceReference);
				CyFileFilter filter = tf.getFileFilter();
				if(filter.getDataCategory() == DataCategory.NETWORK)
					acceptableExtensions.addAll(filter.getExtensions());
			}
			
			final GSFileBrowserDialog dialog =
					new GSFileBrowserDialog(frame, dataManagerClient, acceptableExtensions,
								GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG, "Import Table from GenomeSpace");
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;
			
			GSDataFormat dataFormat = fileMetadata.getDataFormat();
			if ( dataFormat != null && dataFormat.getFileExtension() != null ) { 
				String extension = dataFormat.getFileExtension();
				if ( extension != null && (extension.equalsIgnoreCase("gct") ||
						extension.equalsIgnoreCase("odf")) )
					dataFormat = gsContext.findConversionFormat(fileMetadata.getAvailableDataFormats(), "attr");
			}

			// Download the GenomeSpace file:
			String baseName = fileMetadata.getName();
			File tempFile = new File(System.getProperty("java.io.tmpdir"), baseName);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(session, fileMetadata, tempFile, true));
			ti.append(importTableFileTaskFactory.createTaskIterator(tempFile));
			ti.append(new DeleteFileTask(tempFile));
			dialogTaskManager.execute(ti);
		} catch (GSClientException ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame, "<html>The GenomeSpace server is inaccessible or not responding properly at this time.<br/>" +
					"Please check your Internet connection and try again.</html>", "GenomeSpace Error",
			        JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, ex, "Exception",
			        JOptionPane.ERROR_MESSAGE);
		}
	}

}
