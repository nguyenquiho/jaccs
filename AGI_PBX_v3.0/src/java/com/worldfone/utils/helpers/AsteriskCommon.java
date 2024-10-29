/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.helpers;

import com.worldfone.agi.baseskillrouting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.ExtensionStateAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ExtensionStateResponse;

/**
 *
 * @author MitsuyoRai
 */
public final class AsteriskCommon {

    private static final String AST_SERVER = "127.0.0.1";
    private static final String AST_USER = "admin";
    private static final String AST_PWD = "Admin@Stel7779";

    public static boolean checkIDLE(String extension) {
        try {
            ManagerConnectionFactory factory = new ManagerConnectionFactory(AST_SERVER, AST_USER, AST_PWD);
            ManagerConnection managerConnection = factory.createManagerConnection();
            managerConnection.login();
            ExtensionStateAction ext_action = new ExtensionStateAction();
            ext_action.setExten(extension);
            ext_action.setContext("from-internal");
            ExtensionStateResponse response = (ExtensionStateResponse) managerConnection.sendAction(ext_action, 2000);
            //System.out.println(response);
            int status = response.getStatus();
            // and finally log off and disconnect
            managerConnection.logoff();
            return (status == 0);
        } catch (IllegalStateException | IOException | AuthenticationFailedException | TimeoutException ex) {
            Logger.getLogger(baseskillrouting.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static List<String> QueueShow(String queue) {
        List<String> ext_available = new ArrayList<>();
        try {
            ManagerConnectionFactory factory = new ManagerConnectionFactory(AST_SERVER, AST_USER, AST_PWD);
            ManagerConnection managerConnection = factory.createManagerConnection();
            managerConnection.login();
            CommandAction command = new CommandAction();
            command.setCommand("queue show " + queue);
            // send the originate action and wait for a maximum of 30 seconds for Asterisk to send a reply
            CommandResponse response = (CommandResponse) managerConnection.sendAction(command, 2000);
            Iterator lineIterator = response.getResult().iterator();
//            int i = 0;
            while (lineIterator.hasNext()) {
                String line = lineIterator.next().toString().trim();
                if (line.startsWith("sip/")) {
                    if (line.contains("Not in use") && !line.contains("paused")) {
                        String[] str_array = line.split(" ");
                        String extension = str_array[0].replace("sip/", "");
                        //    if(i>0){
                        ext_available.add(extension);
                        //  }else{
                        //      ext_available=ext_available+extension;
                        // }
//                        i++;
                    }
                }
            }
            // and finally log off and disconnect
            managerConnection.logoff();
        } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException | AuthenticationFailedException ex) {
            System.out.println(ex);
        }
        return ext_available;
    }

    public static int QueueCountAvailable(String queue) {
        try {
            int i = 0;
            ManagerConnectionFactory factory = new ManagerConnectionFactory(AST_SERVER, AST_USER, AST_PWD);
            ManagerConnection managerConnection = factory.createManagerConnection();
            managerConnection.login();
            CommandAction command = new CommandAction();
            command.setCommand("queue show " + queue);
            // send the originate action and wait for a maximum of 30 seconds for Asterisk to send a reply
            CommandResponse response = (CommandResponse) managerConnection.sendAction(command, 2000);
            Iterator lineIterator = response.getResult().iterator();
            while (lineIterator.hasNext()) {
                String line = lineIterator.next().toString().trim();
                if (line.startsWith("sip/")) {
                    if (line.contains("(Not in use)") && !line.contains("(paused)")) {
                        i++;
                    }
                }
            }
            // and finally log off and disconnect
            managerConnection.logoff();
            return i;
        } catch (IOException | TimeoutException | IllegalArgumentException | IllegalStateException | AuthenticationFailedException ex) {
            System.out.println(ex);
            return 0;
        }
    }

}
