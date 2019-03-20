package org.viper.mule.properties;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import org.apache.log4j.Logger;

/**
 * @author Vibhanshu Thakur
 * Extending PropertyPlaceholderConfigurer which is Springs way 
 * of loading properties into application.
 *
 */
public class ExternalProperties extends PropertyPlaceholderConfigurer {
	//for logging
	public static final Logger LOGGER = Logger.getLogger(ExternalProperties.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#convertProperties(java.util.Properties)
	 * This functions gets automatically triggered on creation of a bean of this class.
	 * Parameter props is not be passed at any point, function will self trigger.
	 */
	protected void convertProperties(Properties props) {
		LOGGER.info("properties loading process started");
		
		try {
			//loading properties from SFTP file into a StringReader(expected type for load())
			StringReader interimReaderProperties = new StringReader(ExternalProperties.readSFTP());
			//loading properties into application
			props.load(interimReaderProperties);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		LOGGER.info("Properties loaded successfully");
	}
	
	
	/**
	 * @return String containing the entire file picked up from SFTP
	 * @throws IOException
	 */
	private static String readSFTP() throws IOException {
        JSch jsch = new JSch();
        Session session = null;
        try {
        	//sftp credentials and details
            session = jsch.getSession("tester", "localhost", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("password");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            // file to be picked up
            InputStream stream = sftpChannel.get("config.properties");
            String finalFile = "";
            try {
            	//reading file
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null) {
                	//storing content of file to string for consolidating
                	finalFile+=line + "\n";
                }
            } catch (IOException io) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();

            }
            sftpChannel.exit();
            session.disconnect();
            return finalFile;
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        } catch (SftpException e) {
            e.printStackTrace();
            return null;
        }
    }
}
