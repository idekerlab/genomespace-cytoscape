package cytoscape.genomespace;

import java.util.Properties;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.sws.SimpleWebServer;
import org.osgi.framework.BundleContext;


/**
 * This class is used to instantiate your plugin. Put whatever initialization code
 * you need into the no argument constructor (the only one that will be called).
 * The actual functionality of your plugin can be in this class, but should 
 * probably be separated into separted classes that get instantiated here.
 */
public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		// Properly initializes things.
		super();
	}

	public void start(BundleContext bc) throws Exception {
		CySwingApplication cySwingApplication = getService(bc, CySwingApplication.class);
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");
		FileUtil fileUtil = getService(bc, FileUtil.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		LoadTableFileTaskFactory loadTableFileTaskFactory = getService(bc, LoadTableFileTaskFactory.class);
		LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc, ExportNetworkViewTaskFactory.class);
		GSUtils gsUtils = new GSUtils(cytoscapePropertiesServiceRef, cySwingApplication.getJFrame());
		JFrame frame = cySwingApplication.getJFrame();
		// set up the URL loaders
		LoadNetworkFromURL loadNetworkURL = new LoadNetworkFromURL(dialogTaskManager, loadNetworkFileTaskFactory, gsUtils);
		LoadSessionFromURL loadSessionURL = new LoadSessionFromURL(dialogTaskManager, openSessionTaskFactory, gsUtils);
//		LoadCyTableFromURL loadNodeAttrURL = new LoadCyTableFromURL("node.cytable",Cytoscape.getNodeAttributes());
//		LoadCyTableFromURL loadEdgeAttrURL = new LoadCyTableFromURL("edge.cytable",Cytoscape.getEdgeAttributes());

		SimpleWebServer sws = new SimpleWebServer(60161);
		sws.registerListener(loadNetworkURL);
//		sws.registerListener(loadNodeAttrURL);
//		sws.registerListener(loadEdgeAttrURL);
		sws.registerListener(loadSessionURL);
		sws.start();

		// This action represents the actual behavior of the plugin.
		UploadFileToGenomeSpace uploadAction = new UploadFileToGenomeSpace(fileUtil, gsUtils, frame);
		registerService(bc,uploadAction,CyAction.class, new Properties());

		DeleteFileInGenomeSpace deleteAction = new DeleteFileInGenomeSpace(gsUtils, frame);
		registerService(bc,deleteAction,CyAction.class, new Properties());

		DownloadFileFromGenomeSpace downloadAction = new DownloadFileFromGenomeSpace(gsUtils, frame);
		registerService(bc,downloadAction,CyAction.class, new Properties());

		ListFilesInGenomeSpace listAction = new ListFilesInGenomeSpace(gsUtils, frame);
		registerService(bc,listAction,CyAction.class, new Properties());

		LoadNetworkFromGenomeSpace loadNetworkAction = new LoadNetworkFromGenomeSpace(dialogTaskManager, loadNetworkFileTaskFactory, gsUtils, frame);
		registerService(bc,loadNetworkAction,CyAction.class, new Properties());

		LoadAttrsFromGenomeSpace loadAttrsAction = new LoadAttrsFromGenomeSpace(dialogTaskManager, loadTableFileTaskFactory, gsUtils, frame);
		registerService(bc,loadAttrsAction,CyAction.class, new Properties());

//		LoadCyTableFromGenomeSpace loadCyTableAction = new LoadCyTableFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadCyTableAction);


		LoadSessionFromGenomeSpace loadSessionAction = new LoadSessionFromGenomeSpace(dialogTaskManager, openSessionTaskFactory, gsUtils, frame);
		registerService(bc,loadSessionAction,CyAction.class, new Properties());

		SaveSessionToGenomeSpace saveSessionAction = new SaveSessionToGenomeSpace(dialogTaskManager, saveSessionAsTaskFactory, gsUtils, frame);
		registerService(bc,saveSessionAction,CyAction.class, new Properties());

		SaveNetworkToGenomeSpace saveNetworkAction = new SaveNetworkToGenomeSpace(cyApplicationManager, dialogTaskManager, exportNetworkViewTaskFactory, gsUtils, frame);
		registerService(bc,saveNetworkAction,CyAction.class, new Properties());

//		LoadOntologyAndAnnotationFromGenomeSpace loadOntologyAndAnnotationFromGenomeSpace =
//			new LoadOntologyAndAnnotationFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadOntologyAndAnnotationFromGenomeSpace);

		LoginToGenomeSpace loginToGenomeSpace = new LoginToGenomeSpace(gsUtils);
		registerService(bc,loginToGenomeSpace,CyAction.class, new Properties());

//		JMenu gsMenu = cySwingApplication.getJMenuBar().getMenu("File.GenomeSpace");
//		LaunchToolMenu ltm = new LaunchToolMenu(gsMenu);
//		gsMenu.add(ltm);
		

		// load any initial arguments
		String sessionProp = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.session");
		if ( sessionProp != null ) {
			loadSessionURL.loadSession(sessionProp);
		} else {
			String networkProp = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.network");
			loadNetworkURL.loadNetwork(networkProp);

//			String nodeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("node.cytable");
//			loadNodeAttrURL.loadTable(nodeTableProp);
//
//			String edgeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("edge.cytable");
//			loadEdgeAttrURL.loadTable(edgeTableProp);
		}
	}
}
