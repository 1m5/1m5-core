package io.onemfive.core.orchestration.routes.security;

import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.orchestration.routes.RoutingSlip;
import io.onemfive.core.orchestration.routes.SimpleRoute;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DIDCreationRoute extends RoutingSlip {

    public DIDCreationRoute() {
        super(DIDCreationRoute.class.getName(), "Create");
        try {
            addRoute(new SimpleRoute(DIDService.class.getName(), DIDService.OPERATION_CREATE));
            addRoute(new SimpleRoute(InfoVaultService.class.getName(), InfoVaultService.OPERATION_SAVE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
