package io.onemfive.core.sensors.i2p;

import io.onemfive.core.OneMFiveAppContext;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;

import java.io.File;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class I2PRouterUtil {

    public static Router getGlobalI2PRouter(Properties properties, boolean autoStart) {
        Router globalRouter = null;
        RouterContext routerContext = RouterContext.listContexts().get(0);
        if(routerContext != null) {
            globalRouter = routerContext.router();
            if(globalRouter == null) {
                System.out.println("Instantiating I2P Router...");
                File baseDir = OneMFiveAppContext.getInstance().getBaseDir();
                String baseDirPath = baseDir.getAbsolutePath();
                System.setProperty("i2p.dir.base", baseDirPath);
                System.setProperty("i2p.dir.config", baseDirPath);
                System.setProperty("wrapper.logfile", baseDirPath + "/wrapper.log");
                globalRouter = new Router(properties);
            }
            if(autoStart && !globalRouter.isAlive()) {
                System.out.println("Starting I2P Router...");
                globalRouter.setKillVMOnEnd(false);
                globalRouter.runRouter();
            }
        }
        return globalRouter;
    }

}
