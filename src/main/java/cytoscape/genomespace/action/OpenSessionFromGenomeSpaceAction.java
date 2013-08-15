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
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;
import cytoscape.genomespace.task.BasicFileTaskFactory;
import cytoscape.genomespace.task.SetFrameSessionTitleTask;


public class OpenSessionFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	static final Logger logger = LoggerFactory.getLogger(OpenSessionFromGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final BasicFileTaskFactory openSessionFileTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;
	
	
	public OpenSessionFromGenomeSpaceAction(DialogTaskManager dialogTaskManager, BasicFileTaskFactory openSessionFileTaskFactory, GenomeSpaceContext gsContext, JFrame frame) {
		super("Open from GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File");
		setMenuGravity(1.1f);
		this.dialogTaskManager = dialogTaskManager;
		this.openSessionFileTaskFactory = openSessionFileTaskFactory;
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
			acceptableExtensions.add("cys");
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG, "Open Session from GenomeSpace");
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;

			// Download the GenomeSpace file:
			final String baseName = fileMetadata.getName();
			File tempFile = new File(System.getProperty("java.io.tmpdir"), baseName);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(session, fileMetadata, tempFile, true));
			ti.append(openSessionFileTaskFactory.createTaskIterator(tempFile));
			ti.append(new SetFrameSessionTitleTask(frame, baseName));
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

