package org.jboss.datavirt.ui.server.services.util;

import java.util.Collection;

import org.jboss.datavirt.ui.client.shared.beans.Constants;

public class TranslatorHelper {

	public static final String TEIID_FILE_DRIVER = "file"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_DRIVER = "google"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_DRIVER = "infinispan"; //$NON-NLS-1$
	public static final String TEIID_LDAP_DRIVER = "ldap"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_DRIVER = "mongodb"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_DRIVER = "salesforce"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_DRIVER = "webservice"; //$NON-NLS-1$
	public static final String TEIID_FILE_DRIVER_DISPLAYNAME = "FlatFile"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_DRIVER_DISPLAYNAME = "Google"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_DRIVER_DISPLAYNAME = "Infinispan"; //$NON-NLS-1$
	public static final String TEIID_LDAP_DRIVER_DISPLAYNAME = "LDAP"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_DRIVER_DISPLAYNAME = "MongoDB"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_DRIVER_DISPLAYNAME = "Salesforce"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_DRIVER_DISPLAYNAME = "WebService"; //$NON-NLS-1$

	public static final String TEIID_FILE_CLASS = "org.teiid.resource.adapter.file.FileManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_GOOGLE_CLASS = "org.teiid.resource.adapter.google.GoogleManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_INFINISPAN_CLASS = "org.teiid.resource.adapter.infinispan.InfinispanManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_LDAP_CLASS = "org.teiid.resource.adapter.ldap.LDAPManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_MONGODB_CLASS = "org.teiid.resource.adapter.mongodb.MongoDBManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_SALESORCE_CLASS = "org.teiid.resource.adapter.salesforce.SalesForceManagedConnectionFactory"; //$NON-NLS-1$
	public static final String TEIID_WEBSERVICE_CLASS = "org.teiid.resource.adapter.ws.WSManagedConnectionFactory"; //$NON-NLS-1$

	public static final String ACCESS = "access"; //$NON-NLS-1$
	public static final String DB2 = "db2"; //$NON-NLS-1$
	public static final String DERBY = "derby"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String GOOGLE_SPREADSHEET = "google-spreadsheet"; //$NON-NLS-1$
	public static final String H2 = "h2"; //$NON-NLS-1$
	public static final String HIVE = "hive"; //$NON-NLS-1$
	public static final String HSQL = "hsql"; //$NON-NLS-1$
	public static final String INFINISPAN = "infinispan-cache"; //$NON-NLS-1$
	public static final String INFORMIX = "informix"; //$NON-NLS-1$
	public static final String INGRES = "ingres"; //$NON-NLS-1$
	public static final String INGRES93 = "ingres93"; //$NON-NLS-1$
	public static final String INTERSYSTEMS_CACHE = "intersystems-cache"; //$NON-NLS-1$
	public static final String JDBC_ANSI = "jdbc-ansi"; //$NON-NLS-1$
	public static final String JDBC_SIMPLE = "jdbc-simple"; //$NON-NLS-1$
	public static final String JPA2 = "jpa2"; //$NON-NLS-1$
	public static final String LDAP = "ldap"; //$NON-NLS-1$
	public static final String LOOPBACK = "loopback"; //$NON-NLS-1$
	public static final String MAP_CACHE = "map-cache"; //$NON-NLS-1$
	public static final String METAMATRIX = "metamatrix"; //$NON-NLS-1$
	public static final String MODESHAPE = "modeshape"; //$NON-NLS-1$
	public static final String MONGODB = "mongodb"; //$NON-NLS-1$
	public static final String MYSQL = "mysql"; //$NON-NLS-1$
	public static final String MYSQL5 = "mysql5"; //$NON-NLS-1$
	public static final String NETEZZA = "netezza"; //$NON-NLS-1$
	public static final String OLAP = "olap"; //$NON-NLS-1$
	public static final String ORACLE = "oracle"; //$NON-NLS-1$
	// NOTE: For PostgreSQL vendor leaves off the QL, so we need to be careful to map this correctly
	public static final String POSTGRES = "postgres"; //$NON-NLS-1$
	public static final String POSTGRESQL = "postgresql"; //$NON-NLS-1$
	public static final String SALESFORCE = "salesforce"; //$NON-NLS-1$
	public static final String SQLSERVER = "sqlserver"; //$NON-NLS-1$
	public static final String SYBASE = "sybase"; //$NON-NLS-1$
	public static final String TEIID = "teiid"; //$NON-NLS-1$
	public static final String TERADATA = "teradata"; //$NON-NLS-1$
	public static final String WS = "ws"; //$NON-NLS-1$

	public static final String URL_DB2 = "jdbc:db2://<host>:50000/<dbName>"; //$NON-NLS-1$
	public static final String URL_DERBY = "jdbc:derby://<host>:1527/<dbName>;create=true"; //$NON-NLS-1$
	public static final String URL_INFORMIX = "jdbc:informix-sqli://<host>:1526/<dbName>:INFORMIXSERVER=server"; //$NON-NLS-1$
	public static final String URL_INGRES = "jdbc:ingres://<host>:117/<dbName>"; //$NON-NLS-1$
	public static final String URL_MODESHAPE = "jdbc:jcr:http://<host>:8080/modeshape-rest/"; //$NON-NLS-1$
	public static final String URL_MYSQL = "jdbc:mysql://<host>:3306/<dbName>"; //$NON-NLS-1$
	public static final String URL_ORACLETHIN = "jdbc:oracle:thin:@<host>:1521:<dbName>"; //$NON-NLS-1$
	public static final String URL_POSTGRES = "jdbc:postgresql://<host>:5432/<dbName>"; //$NON-NLS-1$
	public static final String URL_SQLSERVER = "jdbc:sqlserver://<host>:1433;databaseName=<dbName>"; //$NON-NLS-1$
	public static final String URL_TEIID = "jdbc:teiid:<vdbName>@mms://<host>:31000"; //$NON-NLS-1$
	public static final String URL_JDBC = "jdbc://<host>:<port>"; //$NON-NLS-1$

	/**
	 * Get the best fit translator, given the driverName and list of translator names
	 * @param driverName the driver name
	 * @param translatorNames the list of current translators
	 * @param teiidVersion the Teiid Version
	 * @return the best fit translator for the provided driver
	 */
	public static String getTranslator(String driverName, Collection<String> translatorNames) {
		if(isEmpty(driverName)) return Constants.STATUS_UNKNOWN;
		if(isEmpty(translatorNames)) return Constants.STATUS_UNKNOWN;

		if(driverName.equals(TEIID_FILE_DRIVER) && translatorNames.contains(FILE)) {
			return FILE;
		}

		if(driverName.equals(TEIID_GOOGLE_DRIVER) && translatorNames.contains(GOOGLE_SPREADSHEET)) {
			return GOOGLE_SPREADSHEET;
		}

		if(driverName.equals(TEIID_INFINISPAN_DRIVER) && translatorNames.contains(INFINISPAN)) {
			return INFINISPAN;
		}

		if(driverName.equals(TEIID_LDAP_DRIVER) && translatorNames.contains(LDAP)) {
			return LDAP;
		}

		if(driverName.equals(TEIID_MONGODB_DRIVER) && translatorNames.contains(MONGODB)) {
			return MONGODB;
		}

		if(driverName.equals(TEIID_SALESORCE_DRIVER) && translatorNames.contains(SALESFORCE)) {
			return SALESFORCE;
		}

		if(driverName.equals(TEIID_WEBSERVICE_DRIVER) && translatorNames.contains(WS)) {
			return WS;
		}

		if(driverName.startsWith("derby")) { //$NON-NLS-1$
			return DERBY;
		}

		if(driverName.startsWith("mysql")) { //$NON-NLS-1$
			return MYSQL;
		}

		if(driverName.startsWith("ojdbc")) { //$NON-NLS-1$
			return ORACLE;
		}

		if(driverName.startsWith("db2")) { //$NON-NLS-1$
			return DB2;
		}

		if(driverName.startsWith("postgresql")) { //$NON-NLS-1$
			return POSTGRESQL;
		}

		if(driverName.startsWith("sqljdbc")) { //$NON-NLS-1$
			return SQLSERVER;
		}

		if(driverName.startsWith("teiid")) { //$NON-NLS-1$
			return TEIID;
		}

		if(driverName.startsWith("modeshape")) { //$NON-NLS-1$
			return MODESHAPE;
		}

		return JDBC_ANSI;
	}

	/**
	 * Test if supplied string is null or zero length
	 * @param text
	 */
	private static boolean isEmpty(String text) {
		return (text == null || text.length() == 0);
	}

	/**
	 * Test if supplied list is null or zero length
	 * @param list
	 */
	private static boolean isEmpty(Collection<String> list) {
		return (list == null || list.size() == 0);
	}

	/**
	 * Get the Driver Name for the supplied class and server version
	 * @param driverClassName the driver class name
	 * @return the driver name
	 */
	public static String getDriverNameForClass(String driverClassName) {
		String driverName = null;
		if(!isEmpty(driverClassName)) {
			if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_FILE_CLASS)) {
				driverName = TranslatorHelper.TEIID_FILE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_GOOGLE_CLASS)) {
				driverName = TranslatorHelper.TEIID_GOOGLE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_INFINISPAN_CLASS)) {
				driverName = TranslatorHelper.TEIID_INFINISPAN_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_LDAP_CLASS)) {
				driverName = TranslatorHelper.TEIID_LDAP_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_MONGODB_CLASS)) {
				driverName = TranslatorHelper.TEIID_MONGODB_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_SALESORCE_CLASS)) {
				driverName = TranslatorHelper.TEIID_SALESORCE_DRIVER;
			} else if(driverClassName.equalsIgnoreCase(TranslatorHelper.TEIID_WEBSERVICE_CLASS)) {
				driverName = TranslatorHelper.TEIID_WEBSERVICE_DRIVER;
			}
		}
		return driverName;
	}

	/**
	 * Get the URL Template given a driver name
	 * @param driverName the driver name
	 * @return the URL Template
	 */
	public static String getUrlTemplate(String driverName) {
		if(isEmpty(driverName)) return Constants.STATUS_UNKNOWN; 

		if(driverName.toLowerCase().startsWith("derby")) { //$NON-NLS-1$
			return URL_DERBY;
		}

		if(driverName.toLowerCase().startsWith("mysql")) { //$NON-NLS-1$
			return URL_MYSQL;
		}

		if(driverName.toLowerCase().startsWith("ojdbc")) { //$NON-NLS-1$
			return URL_ORACLETHIN;
		}

		if(driverName.toLowerCase().startsWith("db2")) { //$NON-NLS-1$
			return URL_DB2;
		}

		if(driverName.toLowerCase().startsWith("postgresql")) { //$NON-NLS-1$
			return URL_POSTGRES;
		}

		if(driverName.toLowerCase().startsWith("sqljdbc")) { //$NON-NLS-1$
			return URL_SQLSERVER;
		}

		if(driverName.toLowerCase().startsWith("ifxjdbc")) { //$NON-NLS-1$
			return URL_INFORMIX;
		}

		if(driverName.toLowerCase().startsWith("iijdbc")) { //$NON-NLS-1$
			return URL_INGRES;
		}

		if(driverName.toLowerCase().startsWith("teiid")) { //$NON-NLS-1$
			return URL_TEIID;
		}

		if(driverName.toLowerCase().startsWith("modeshape")) { //$NON-NLS-1$
			return URL_MODESHAPE;
		}

		return URL_JDBC;
	}
}
