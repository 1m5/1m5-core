package io.onemfive.core.sensors.i2p.bote.email;

/**
 * A email header field such as "To" or "Subject".
 * <code>STATUS</code> is special. It is not a header field, and it is only used by {@link io.onemfive.core.sensors.i2p.bote.folder.Outbox}.
 *
 * I would have made this an inner class of Email, but Tomcat doesn't like inner classes in JSP functions.
 */
public enum EmailAttribute {FROM, TO, SUBJECT, DATE, CREATE_TIME, DELIVERED, STATUS}
