package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.UploadFileToGenomeSpaceTask;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class ExportNetworkToGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 9988760123456789L;
	private static final Logger logger = LoggerFactory.getLogger(ExportNetworkToGenomeSpaceAction.class);
	private final CyApplicationManager cyApplicationManager;
	private final DialogTaskManager dialogTaskManager;
	private final CyNetworkViewWriterManager cyNetworkViewWriterManager;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;
	
	
	public ExportNetworkToGenomeSpaceAction(CyApplicationManager cyApplicationManager, CyNetworkViewManager cyNetworkViewManager,  DialogTaskManager dialogTaskManager, CyNetworkViewWriterManager cyNetworkViewWriterManager, GenomeSpaceContext gsContext, JFrame frame) {
		// Give your action a name here
		super("Network to GenomeSpace...", cyApplicationManager, "networkAndView", cyNetworkViewManager);

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Export");
		setMenuGravity(1.15f);
		this.cyApplicationManager = cyApplicationManager;
		this.dialogTaskManager = dialogTaskManager;
		this.cyNetworkViewWriterManager = cyNetworkViewWriterManager;
		this.gsContext = gsContext;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(!gsContext.loginIfNotAlready()) return;
			final GsSession session = gsContext.getSession();
			final DataManagerClient dataManagerClient = session.getDataManagerClient();
			List<CyFileFilter> filters = cyNetworkViewWriterManager.getAvailableWriterFilters();
			Map<String, CyFileFilter> filterMap = new TreeMap<String, CyFileFilter>();
			String sifFilterDescription = null;
			for (CyFileFilter filter: filters) {
				filterMap.put(filter.getDescription(), filter);
				if(filter.getExtensions().contains("sif"))
					sifFilterDescription = filter.getDescription();
			}
			String formatDescriptor = (String) JOptionPane.showInputDialog(frame, "Select the export file format", "Export Network", 
					JOptionPane.PLAIN_MESSAGE, null, filterMap.keySet().toArray(),
					sifFilterDescription);
			if(formatDescriptor == null)
				return;
			
			CyFileFilter filter = filterMap.get(formatDescriptor);
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							filter.getExtensions(),
							GSFileBrowserDialog.DialogType.SAVE_AS_DIALOG, "Export Network to GenomeSpace");

			String saveFileName = dialog.getSaveFileName();
			if (saveFileName == null)
				return;
			
			final String baseName = gsContext.baseName(saveFileName);
			final File tempFile = new File(System.getProperty("java.io.tmpdir"), baseName);
			TaskIterator ti = new TaskIterator(cyNetworkViewWriterManager.getWriter(cyApplicationManager.getCurrentNetworkView(), filter, tempFile));
			ti.append(new UploadFileToGenomeSpaceTask(session, tempFile, saveFileName));
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
