/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Originally from:
 * http://blogs.sun.com/andreas/resource/InstallCert.java
 * Use:
 * java InstallCert hostname
 * Example:
 *% java InstallCert ecc.fedora.redhat.com
 */

/**
 * slightly modified by David da Guia Carvalho <dguiarj at gmail dot com>
 * Modified to work with the (unofficial) GUI for pentaho publish 
 */

import javax.net.ssl.*;
import java.io.*;
import java.util.Enumeration;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Class used to add the server's certificate to the KeyStore
 * with your trusted certificates.
 */

public class InstallCert {
	
    /**
     * Add certificate to the java store jssecacerts or cacerts
     * @param host String - Server that require certificate (google.com)
     * @param port int - port (443)
     * @param passphrase char[] - Default is "changeit"
     * @return return int - Added: 1 / Alredy exists: 2 / Error: 3
     * @throws Exception - In case of errors
     */
    static int addCert(String host, int port, char[] passphrase) throws Exception {
    	
        ///
        // Select store file either jsse or cacerts from java home (recomend to use java sun)
    	///
        File file = new File("jssecacerts");
        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP
                    + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }

        ///
        // Open store file
        ///
        System.out.println("Loading KeyStore " + file + "...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
//        ks.
        System.out.println("lista de certificados: "+ks.aliases().toString()+"\n----------------------------------------------------------\n\n");

        in.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        
        ///
        // Connect to a server to retrieve certificate
        ///
        System.out.println("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            System.out.println("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            System.out.println();
            System.out.println("No errors, certificate is already trusted");
            return 2;
        } catch (SSLException e) {
            System.out.println();
            e.printStackTrace(System.out);
            //throw new Exception(e);
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            System.out.println("Could not obtain server certificate chain");
//            return 3;
            throw new Exception("Could not obtain server certificate chain");
        }

        ///
        // Opem read stream (read from shell
        ///
        /*
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
		*/

        System.out.println();
        System.out.println("Server sent " + chain.length + " certificate(s):");
        System.out.println();
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            System.out.println
                    (" " + (i + 1) + " Subject " + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            System.out.println("   md5     " + toHexString(md5.digest()));
            System.out.println();
        }
        
        System.out.println("Enter certificate to add to trusted keystore or 'q' to quit: [1]");
        // make de read
        // String line = reader.readLine().trim();
        String line = "1";
        int k;
        try {
            k = (line.length() == 0) ? 0 : Integer.parseInt(line) - 1;
        } catch (NumberFormatException e) {
            System.out.println("KeyStore not changed");
            return 3;
        }

        X509Certificate cert = chain[k];
        String alias = host + "-" + (k + 1);
        ks.setCertificateEntry(alias, cert);

        ////
        // Write to que keystore
        ////
        // OutputStream out = new FileOutputStream("jssecacerts");
        
        OutputStream out = new FileOutputStream("cacerts");
        ks.store(out, passphrase);
        out.close();
		
        System.out.println();
        System.out.println(cert);
        System.out.println();
        System.out.println
                ("Added certificate to keystore 'jssecacerts' using alias '"
                        + alias + "'");
        return 1;
    }
/**
 * List certificate alias from keystore
 * @param passphrase - changeit
 * @return String[] - Array of alias
 * @throws Exception - Shitshappens
 */
    public static final String[] listCert(char[] passphrase) throws Exception{
    	//
        File file = new File("jssecacerts");
        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP
                    + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }
        ///
        // Open store file
        ///
        System.out.println("Loading KeyStore " + file + "...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
//        ks.
        System.out.println("lista de certificados: "+ks.aliases()+"\n");
        
        Enumeration<String> em = ks.aliases();
        String certs = "";
        int i;
        for (i=0; em.hasMoreElements(); i++) {
            String certAlias = (String)em.nextElement();
            // Does alias refer to a private key?
            // boolean b = ks.isKeyEntry(certAlias);
            // Does alias refer to a trusted certificate?
            // b = ks.isCertificateEntry(certAlias);
            if (ks.isCertificateEntry(certAlias)) {
            	// System.out.println(ks.getCertificate(certAlias).toString());
            	System.out.println("Certificado cadastrado: "+certAlias+"\n");
            	certs += certAlias+";";
            }
        }
        
        in.close();
        // String[] response = new String[i]; 
        return certs.split(";");
    }
    
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }
        

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}