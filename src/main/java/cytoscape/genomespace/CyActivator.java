package cytoscape.genomespace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.ConfigurationUrls;
import org.genomespace.client.GsSession;
import org.genomespace.sws.SimpleWebServer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.action.ExportNetworkToGenomeSpaceAction;
import cytoscape.genomespace.action.ImportNetworkFromGenomeSpaceAction;
import cytoscape.genomespace.action.ImportTableFromGenomeSpaceAction;
import cytoscape.genomespace.action.LaunchToolAction;
import cytoscape.genomespace.action.LoginToGenomeSpaceAction;
import cytoscape.genomespace.action.OpenSessionFromGenomeSpaceAction;
import cytoscape.genomespace.action.SaveSessionToGenomeSpaceAction;
import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.event.CytoscapeGSURLHandler;
import cytoscape.genomespace.task.BasicFileTaskFactory;


/**
 * This class is used to instantiate your plugin. Put whatever initialization code
 * you need into the no argument constructor (the only one that will be called).
 * The actual functionality of your plugin can be in this class, but should 
 * probably be separated into separted classes that get instantiated here.
 */
public class CyActivator extends AbstractCyActivator {
	private static final Logger logger = LoggerFactory.getLogger(LaunchToolAction.class);
	private CySwingApplication cySwingApplication;
	private CyServiceRegistrar cyServiceRegistrar;
	private SimpleWebServer sws;
	private Set<LaunchToolAction> launchToolActions;
	
	public CyActivator() {
		// Properly initializes things.
		super();
	}

	public void start(BundleContext bc) throws Exception {
		this.cySwingApplication = getService(bc, CySwingApplication.class);
		this.cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManager = getService(bc, CyNetworkViewManager.class);
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=commandline.props)");
		String gsenv = cytoscapePropertiesServiceRef.getProperties().getProperty("genomespace.environment","dev").toString();
		ConfigurationUrls.init(gsenv);
		
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		TunableSetter tunableSetter = getService(bc, TunableSetter.class);
		BasicFileTaskFactory loadTableFileTaskFactory = new BasicFileTaskFactory(getService(bc, LoadTableFileTaskFactory.class), tunableSetter);
		BasicFileTaskFactory loadNetworkFileTaskFactory = new BasicFileTaskFactory(getService(bc, LoadNetworkFileTaskFactory.class), tunableSetter);
		BasicFileTaskFactory loadSessionFileTaskFactory = new BasicFileTaskFactory(getService(bc, OpenSessionTaskFactory.class), tunableSetter);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc, ExportNetworkViewTaskFactory.class);
		JFrame frame = cySwingApplication.getJFrame();
		GenomeSpaceContext gsContext = new GenomeSpaceContext(cySwingApplication, this);
		
		// set up the URL loaders
		CytoscapeGSURLHandler gsUrlHandler = new CytoscapeGSURLHandler(dialogTaskManager, loadNetworkFileTaskFactory, loadSessionFileTaskFactory, frame, gsContext);
//		LoadCyTableFromURL loadNodeAttrURL = new LoadCyTableFromURL("node.cytable",Cytoscape.getNodeAttributes());
//		LoadCyTableFromURL loadEdgeAttrURL = new LoadCyTableFromURL("edge.cytable",Cytoscape.getEdgeAttributes());
		
		sws = new SimpleWebServer(60161);
		sws.registerListener(gsUrlHandler);
//		sws.registerListener(loadNodeAttrURL);
//		sws.registerListener(loadEdgeAttrURL);
		sws.start();
		
		launchToolActions = new HashSet<LaunchToolAction>();

		// This action represents the actual behavior of the plugin.
		
		Properties enableForNetwork = new Properties();
		enableForNetwork.put("enableFor", "network");

		ImportNetworkFromGenomeSpaceAction importNetworkFromGenomeSpaceAction = new ImportNetworkFromGenomeSpaceAction(dialogTaskManager, loadNetworkFileTaskFactory, gsContext, frame);
		registerService(bc,importNetworkFromGenomeSpaceAction,CyAction.class, new Properties());

		ImportTableFromGenomeSpaceAction importTableFromGenomeSpaceAction = new ImportTableFromGenomeSpaceAction(cyApplicationManager, cyNetworkViewManager, dialogTaskManager, loadTableFileTaskFactory, gsContext, frame);
		registerService(bc,importTableFromGenomeSpaceAction,CyAction.class, new Properties());
		
		ExportNetworkToGenomeSpaceAction exportNetworkToGenomeSpaceAction = new ExportNetworkToGenomeSpaceAction(cyApplicationManager, cyNetworkViewManager, dialogTaskManager, exportNetworkViewTaskFactory, gsContext, frame);
		registerService(bc,exportNetworkToGenomeSpaceAction,CyAction.class, new Properties());

//		ImportOntologyAndAnnotationFromGenomeSpaceAction importOntologyAndAnnotationFromGenomeSpaceAction =
//		new ImportOntologyAndAnnotationFromGenomeSpaceAction();
//		Cytoscape.getDesktop().getCyMenus().addAction(importOntologyAndAnnotationFromGenomeSpaceAction);
		
		OpenSessionFromGenomeSpaceAction openSessionFromGenomeSpaceAction = new OpenSessionFromGenomeSpaceAction(dialogTaskManager, loadSessionFileTaskFactory, gsContext, frame);
		registerService(bc,openSessionFromGenomeSpaceAction,CyAction.class, new Properties());

		SaveSessionToGenomeSpaceAction saveSessionToGenomeSpaceAction = new SaveSessionToGenomeSpaceAction(dialogTaskManager, saveSessionAsTaskFactory, gsContext, frame);
		registerService(bc,saveSessionToGenomeSpaceAction,CyAction.class, new Properties());

		LoginToGenomeSpaceAction loginToGenomeSpaceAction = new LoginToGenomeSpaceAction(gsContext);
		registerService(bc,loginToGenomeSpaceAction,CyAction.class, new Properties());

		// load any initial arguments
		String fileUrl = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.network");
		if ( fileUrl != null ) {
			gsUrlHandler.loadFromURL(fileUrl);
		} else {

//			String nodeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("node.cytable");
//			loadNodeAttrURL.loadTable(nodeTableProp);
//
//			String edgeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("edge.cytable");
//			loadEdgeAttrURL.loadTable(edgeTableProp);
		}
		updateMenus(gsContext.getSession());
	}
	
	public void updateMenus(GsSession session) {
		for(Iterator<LaunchToolAction> i = launchToolActions.iterator(); i.hasNext();){
			cyServiceRegistrar.unregisterAllServices(i.next());
			i.remove();
		}
		try {
			if(session.isLoggedIn()) {
				for ( WebToolDescriptor webTool : session.getAnalysisToolManagerClient().getWebTools() ) {
					if ( webTool.getName().equalsIgnoreCase("cytoscape") )
						continue;
					LaunchToolAction action = new LaunchToolAction(webTool, cySwingApplication.getJFrame());
					cyServiceRegistrar.registerAllServices(action, new Properties());
					launchToolActions.add(action);
				}
			}
		} catch (Exception ex) { 
			logger.warn("problem finding web tools", ex); 
		}
		JMenu launchMenu = cySwingApplication.getJMenu("Apps.GenomeSpace.Launch");
		if ((launchMenu != null)) {
			launchMenu.setEnabled(launchMenu.getItemCount() > 0);
		}
	}
	
	public void cleanup() {
		sws.halt();
		for(LaunchToolAction action: launchToolActions){
			cyServiceRegistrar.unregisterAllServices(action);
		}
		launchToolActions.clear();
	}

	
}
