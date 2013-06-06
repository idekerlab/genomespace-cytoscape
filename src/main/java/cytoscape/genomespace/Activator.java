package cytoscape.genomespace;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private CyActivator cyActivator = new CyActivator();

	public void start(BundleContext bc) throws Exception {
		cyActivator.start(bc);
	}

	public void stop(BundleContext bc) throws Exception {
		cyActivator.stop(bc);
		cyActivator.cleanup();
	}

}
