package io.onemfive.core.util;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class BrowserUtil {

    private static Logger LOG = Logger.getLogger(BrowserUtil.class.getName());

    public static void launch(String url) {
        String[] cmd = null;
        if (SystemVersion.isLinux()) {
            LOG.info("OS is Linux.");
            String[] browsers = new String[]{"purebrowser", "epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"};
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < browsers.length; ++i) {
                if (i == 0) {
                    sb.append(String.format("%s \"%s\"", browsers[i], url));
                } else {
                    sb.append(String.format(" || %s \"%s\"", browsers[i], url));
                }
            }

            cmd = new String[]{"sh", "-c", sb.toString()};
        } else {
            if (!SystemVersion.isMac()) {
                if (SystemVersion.isWindows()) {
                    LOG.info("OS is Windows.");

                    try {
                        Desktop.getDesktop().browse((new URL(url)).toURI());
                    } catch (MalformedURLException var6) {
                        LOG.severe("MalformedURLException caught while launching browser for windows. Error message: " + var6.getLocalizedMessage());
                    } catch (IOException var7) {
                        LOG.severe("IOException caught while launching browser for windows. Error message: " + var7.getLocalizedMessage());
                    } catch (URISyntaxException var8) {
                        LOG.severe("URISyntaxException caught while launching browser for windows. Error message: " + var8.getLocalizedMessage());
                    }

                    return;
                }

                LOG.warning("Unable to determine OS therefore unable to launch a browser.");
                return;
            }

            LOG.info("OS is Mac.");
            cmd = new String[]{"open " + url};
        }

        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException var5) {
            LOG.warning(var5.getLocalizedMessage());
            var5.printStackTrace();
        }

    }
}
