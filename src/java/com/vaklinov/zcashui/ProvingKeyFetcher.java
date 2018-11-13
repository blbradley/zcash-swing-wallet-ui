package com.vaklinov.zcashui;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.xml.bind.DatatypeConverter;

import com.vaklinov.zcashui.OSUtil.OS_TYPE;


/**
 * Fetches the proving key.  Deliberately hardcoded.
 * @author zab
 */
public class ProvingKeyFetcher {
    class ExpectedFile {
        String name;
        int size;
        String sha256;
        String url;

        public ExpectedFile(String name, int size, String sha256, String url) {
            this.name = name;
            this.size = size;
            this.sha256 = sha256;
            this.url = url;
        }
    }

    private static final int PROVING_KEY_SIZE = 910173851;
    private static final String SHA256 = "8bc20a7f013b2b58970cddd2e7ea028975c88ae7ceb9259a5344a16bc2c0eef7";
    private static final String pathURL = "https://zensystem.io/downloads/sprout-proving.key";
    // TODO: add backups
    private static LanguageUtil langUtil = LanguageUtil.instance();

    public void fetchIfMissing(StartupProgressDialog parent) throws IOException {
        try {
            verifyOrFetch(parent);
        } catch (InterruptedIOException iox) {
            JOptionPane.showMessageDialog(parent, langUtil.getString("proving.key.fetcher.option.pane.message"));
            System.exit(-3);
        }
    }
    
    private void verifyOrFetch(StartupProgressDialog parent) 
    	throws IOException 
    {
    	OS_TYPE ost = OSUtil.getOSType();
        
    	File zCashParams = null;
        // TODO: isolate getting ZcashParams in a utility method
        if (ost == OS_TYPE.WINDOWS)  
        {
        	zCashParams = new File(System.getenv("APPDATA") + "/ZcashParams");
        } else if (ost == OS_TYPE.MAC_OS)
        {
        	File userHome = new File(System.getProperty("user.home"));
        	zCashParams = new File(userHome, "Library/Application Support/ZcashParams");
        }
        
        zCashParams = zCashParams.getCanonicalFile();
        
        boolean needsFetch = false;
        if (!zCashParams.exists()) 
        {    
            needsFetch = true;
            zCashParams.mkdirs();
        }

        ZCashInstallationObserver installationObserver = new ZCashInstallationObserver(OSUtil.getProgramDirectory());

        List<String> localparamfiles = new ArrayList<>();

        if (installationObserver.isOnTestNet()) {
            localparamfiles.add("sapling-spend-testnet.params");
            localparamfiles.add("sapling-output-testnet.params");
        }


        // always copy small params files
        for (String filename: localparamfiles) {
            File smallKeyFile = new File(zCashParams, filename);
            FileOutputStream fos = new FileOutputStream(smallKeyFile);

            String resourcePath = "keys/" + filename;
            InputStream is = ProvingKeyFetcher.class.getClassLoader().getResourceAsStream(resourcePath);

            copy(is,fos);
            fos.close();
            is.close();
        }

        List<ExpectedFile> remoteparamfiles = new ArrayList<>();
        remoteparamfiles.add(new ExpectedFile(
                "sprout-proving.key",
                910173851,
                "8bc20a7f013b2b58970cddd2e7ea028975c88ae7ceb9259a5344a16bc2c0eef7",
                "https://z.cash/downloads/sprout-proving.key"
        ));
        remoteparamfiles.add(new ExpectedFile(
                "sprout-verifying.key",
                1449,
                "4bd498dae0aacfd8e98dc306338d017d9c08dd0918ead18172bd0aec2fc5df82",
                "https://z.cash/downloads/sprout-verifying.key"
        ));
        remoteparamfiles.add(new ExpectedFile(
                "sapling-spend.params",
                47958396,
                "8e48ffd23abb3a5fd9c5589204f32d9c31285a04b78096ba40a79b75677efc13",
                "https://z.cash/downloads/sapling-spend.params"
        ));
        remoteparamfiles.add(new ExpectedFile(
                "sapling-output.params",
                3592860,
                "2f0ebbcbb9bb0bcffe95a397e7eba89c29eb4dde6191c339db88570e3f3fb0e4",
                "https://z.cash/downloads/sapling-output.params"
        ));
        remoteparamfiles.add(new ExpectedFile(
                "sprout-groth16.params",
                725523612,
                "b685d700c60328498fbde589c8c7c484c722b788b265b72af448a5bf0ee55b50",
                "https://z.cash/downloads/sprout-groth16.params"
        ));

        if (installationObserver.isOnTestNet()) {
            remoteparamfiles.add(new ExpectedFile(
                    "sprout-groth16-testnet.params",
                    725471388,
                    "58ae56ce8d2c4d4001a55c002c7d6be273835818187881aab41cdfc704b9dbf9",
                    "https://z.cash/downloads/sprout-groth16-testnet.params"
            ));
        }

        for (ExpectedFile expectedFile: remoteparamfiles) {
            File largeKeyFile = new File(zCashParams, expectedFile.name);
            largeKeyFile = largeKeyFile.getCanonicalFile();
            if (!largeKeyFile.exists())
            {
                needsFetch = true;
            } else if (largeKeyFile.length() != expectedFile.size)
            {
                needsFetch = true;
            }

            if (!needsFetch)
            {
                continue;
            }

            JOptionPane.showMessageDialog(
                    parent,
                    langUtil.getString("proving.key.fetcher.option.pane.verify.message"));

            parent.setProgressText(langUtil.getString("proving.key.fetcher.option.pane.verify.progress.text"));
            largeKeyFile.delete();
            fetch(largeKeyFile, expectedFile, parent);
            parent.setProgressText(langUtil.getString("proving.key.fetcher.option.pane.verify.key.text"));
            if (!checkSHA256(largeKeyFile, expectedFile, parent))
            {
                JOptionPane.showMessageDialog(parent, langUtil.getString("proving.key.fetcher.option.pane.verify.key.failed.text"));
                System.exit(-4);
            }
        }
    }


    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[0x1 << 13];
        int read;
        while ((read = is.read(buf)) >- 0) {
            os.write(buf,0,read);
        }
        os.flush();
    }

    private static void fetch(File largeKeyFile, ExpectedFile expectedFile, Component parent) throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(largeKeyFile));
        URL keyURL = new URL(expectedFile.url);
        URLConnection urlc = keyURL.openConnection();
        urlc.setRequestProperty("User-Agent", "Wget/1.17.1 (linux-gnu)");

        InputStream is = urlc.getInputStream();
        ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent, langUtil.getString("proving.key.fetcher.option.pane.verify.progress.monitor.text"), is);
        pmis.getProgressMonitor().setMaximum(expectedFile.size);
        pmis.getProgressMonitor().setMillisToPopup(10);

        copy(pmis,os);
        os.close();
        is.close();
    }
    
    private static boolean checkSHA256(File provingKey, ExpectedFile expectedFile, Component parent) throws IOException {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException(impossible);
        }
        try (InputStream is = new BufferedInputStream(new FileInputStream(provingKey))) {
            ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent,
                    LanguageUtil.instance().getString("proving.key.fetcher.option.pane.verify.progress.monitor.text"),
                    is);
            pmis.getProgressMonitor().setMaximum(expectedFile.size);
            pmis.getProgressMonitor().setMillisToPopup(10);
            DigestInputStream dis = new DigestInputStream(pmis, sha256);
            byte [] temp = new byte[0x1 << 13];
            while(dis.read(temp) >= 0);
            byte [] digest = sha256.digest();
            return expectedFile.sha256.equalsIgnoreCase(DatatypeConverter.printHexBinary(digest));
        }
    }
}
