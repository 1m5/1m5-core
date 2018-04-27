package io.onemfive.core.orchestration.routes.security;

import io.onemfive.core.orchestration.routes.RoutingSlip;
import io.onemfive.core.orchestration.routes.SimpleRoute;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class SecurityVerificationRoute extends RoutingSlip {

    public SecurityVerificationRoute() {
        super("io.onemfive.core.SecurityService", "Verify");
        try {
            addRoute(new SimpleRoute("io.onemfive.core.infovault.InfoVaultService","Load"));
            addRoute(new SimpleRoute("io.onemfive.core.did.DIDService","Verify"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
