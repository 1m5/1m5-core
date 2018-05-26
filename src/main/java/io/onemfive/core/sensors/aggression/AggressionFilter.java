package io.onemfive.core.sensors.aggression;

import java.util.logging.Logger;

/**
 * NSA-level filtering of messages deemed aggressive including child endangerment.
 * These filters are desired to be as strong as what the NSA uses to identify
 * aggressive actors such as child abusers and threats to people. Filtered
 * messages can be sent to others who have the ability to act on them. This
 * acts to prevent bad actors from using this network without the need for
 * mass surveillance of all communications by a centralized body. All filters
 * are open source and all actions are voluntary by the end user in sharing
 * information filtered.
 *
 * @author objectorange
 */
public class AggressionFilter {

    private static final Logger LOG = Logger.getLogger(AggressionFilter.class.getName());


}
