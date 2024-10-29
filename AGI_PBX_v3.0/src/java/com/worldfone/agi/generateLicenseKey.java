/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.queue.JobUseraction;
import static com.worldfone.utils.helpers.AsteriskCommon.QueueShow;
import java.util.HashMap;
import java.util.List;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.worldfone.access.DBSelectionSet;
import static com.worldfone.utils.helpers.LicenseKeyCommon.generateActiveCode;
import static com.worldfone.utils.helpers.LicenseKeyCommon.generateLicenseKey;

import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.Scanner;
import java.net.NetworkInterface;
import java.util.Collections;

/**
 *
 * @author Nguyen Quy Ho
 * @deverlop Nguyen Quy Ho
 *
 */
public class generateLicenseKey implements ServletContextListener {

//    @Override
//    public void service(AgiRequest ar, AgiChannel ac) {
//        try {
//            ac.verbose("Generating License Key....................", 1);
//            for(int i = 1; i < 5; i++){
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    System.out.println(e);
//                }
//                System.out.println("Generating" + i * 10 + " %....");
//            }
//        } catch (AgiException ex) {
//            System.out.println(ex.getClass() + ": " + ex.getMessage());
//        }
//    }
    
  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    //Notification that the servlet context is about to be shut down.   
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    // do all the tasks that you need to perform just after the server starts

    //Notification that the web application initialization process is starting
        try {
            String[]TIGER_IMG = {"                                                                                                                        \n" +
"                                                                                                                        \n" +
"                -+**=                                                                              :=*+-.               \n" +
"            %@@@@@@@@@@@@*                                                                    -@@@@@%@@@@@@@:           \n" +
"           %@@@@@%@@#+***%@@.                                                               @@@@**+*#@%#@@@@@.          \n" +
"          =@@##*++=+=+=:  .*@#                  =*#*%@@@@@@@@%@@@@%%#**+                  +#%+  .===-==+++=*@@          \n" +
"          @@@**+::+**:      := *           -*=      -=+:   *@   :+=:      :==           ++.:.      +#+: :+=#%@:         \n" +
"          @@%++%@*.    .-+  :=  -#:    :+. .-*@@@@@@@@@@@@@+#@@@@@@@@@@@@@#-.  --     *#  -:-  =:     -%%++*%@#         \n" +
"          @%#=+*%@@@#+: ...::: :  %@@@@@@@#-              :@@:              .=%@%%@@@@: :. -..    -*@@@@*+=#%@#         \n" +
"          #%*=-#@@@@%.       :++=.   #@#::-+@@@@@@@@@@@@@% :+ %@@@@@@@@@@@@@*=:.-@@-   #+=+:       -@@@%#*=*#@          \n" +
"          :@=+%@@@#:+%*@@*=-: =%%#%=-=+:: :-%%%*:        :@@@#.         =###-: .:--:-*#@%=  :-=@@###-+@@@@%+*@          \n" +
"           *@-*#%%%@@%%**%@@@*::+-:    :+*-      -@@@.:@@@@@@@@@@: #@@=       =+-    .:=: :#@@@*=*%@@%%@@%**@           \n" +
"            @%*++#@@@@@####%*++=.   +@*      :%@@@= :@@@@%-#+:*@@@@. :@@@%-      =%+   .====***+=+@@@@%#*##@*           \n" +
"             @@#%###%#*-.    ::       -. -@@@@@+   @@*- .:-@@.:. :*@%   =@@@@#.  :        :     .-+**###@@@*            \n" +
"              #@@%#@@@%@@#%.      .%@* :@@@@@          ..-@@@@-            +@@@@  -#=        =+@@*%%@%#@@@:             \n" +
"               @@@%*@@@@@@@+   -%@@#   @@#             :%@@@@@@@:             =@@   +@@+.   -@@@@@@@#*@@@-              \n" +
"               +@-:@@%@@@=   =%@@%::  :#         ++.-#@@@@@%%@@@@@#: =-         *     =@@*:   .%@@%@@+.:@               \n" +
"               =: =@@@.   .+%@@# %@:          @@@@@@@@: .:-=*:.   %@@@@@@@          :@* :@@@=     #@@*: *               \n" +
"               ===@@=    -%@@+:*@@+        :@@@@@@@-   .::-=+-:.    :%@@@@@@.        *@@=  #@#-    :%@#:#               \n" +
"               .-#@%   #@@@@:-%@@@        -@@@#.     - .+#@@=@%=  .     .#@@@:        @@@=  -@@@=   *@@+#               \n" +
"                @@.  -@@@@%..#@@@#                *@@#@@%@@-=@@@@@+@@*                %@@@=  :@@@@-   *%-               \n" +
"                    %@@%*  :%@@@@#        @   =  @@  -%@@#==+%@@@%:  @@  -   %        %@@@@+   :=@@#                    \n" +
"                   @@@ :   --#@@%%  .   :@@ *@@.    @@@-=#:=*+*=:@@@    -@@- @@       %##@=       *@@                   \n" +
"           .     #@@@-   +@*    @# @@@# =@- @@=    %@* :%++=*%=*- +@-    +@@ #@- %@@@ #@    :=     @@@*     .           \n" +
"            .-  =@@@@+ -@@@#  +@@. @@@@@ @# @@    @@% : =@-++@@-   %@*    @@ @% @@@@% :@@:  *@@+  .@@@*.  :-            \n" +
"          .-.  .+@@@@  @@@@* :@@@   %%%@@  =+@   #@*-##.@@:*#:@@ #* %@.   @ -  @@#%+   @@@  #@@@*  %@@#-    --          \n" +
"               -@@@%  :@@@@   @@%.:         =@   @@@@%-@*::=*+.#@.#@+%.  -*.         :=@@#   %@@@   *@@#.               \n" +
"       .      #@@@@:  @@@.  =@@@@@%@#@@@@@@@- *+==*@*=+:.. =#=:.:@:#@   =- .@@@@@@@#@#@@@@%   :@@%  =%@%@*              \n" +
"       :    #-%@@@@+  @@@   #@@@*@#.-@@#%@@ @@: @@@@#+#=-.:*#-..====#@@-  @@@@   @@. *@%@@@    @@@  -@@@#+-*    .       \n" +
"          : :.@@@@+   @@@  %@=@#*@@  +%-  %@@@@: =@@@#*#+*=====-::*@@*   @@     .%+  @@##@:@-  @@@   *@@@*:. .          \n" +
"        -  -.=*@@@@- =@@: +@#@@@@*@@.  @@@@@@@@@  +@@@@@++*-:-++*=%@%   @@@@@@@@@  :@@#@#@*##  .@@  -@@@@-: -  -        \n" +
"         :   .@@@@*  =@@* %@:@@-@@*#@@:    =@@@@@=@@@@@@+=+--++++#*#@* @@@@@-    :@@%#@+=@@ @  +@@   *@@@@    =         \n" +
"        +    #@@@@@- -@@@.@% %@@..%**@@@@@+  @@@@@@@@@@===---+=..+*%@@@@@@#  -@@@@#+@* :@@. %  @@@  +@@@@%-    *        \n" +
"       +      *@@@@#. #@@ +@.:@@@-.@@@@@@@@+ @@@@@@@@%*=:-:.-:: :.-#@@@@@@  :@@@@@@@@ *@@@  @  *@   =@@@@-      +       \n" +
"       -     #@@@@@@  :@@.-@@ +@@@%=*%%*%@@@%@@@@@@@@+--.   :-  ..:=#@@@@@* .---=#%*=@@@@  @-  +@   %@@@@%*     =       \n" +
"       +-    %@@@@@-  -@@=:-@@ .*@@:  :=#*%@@@@@@@%@=::... ..   : .=-+%#*+*-....    :@@.  @    @*   :@@@@@*    -+       \n" +
"       +::-.-+@@@@@@=  @@@* .@@-  +=@@#:=+#@@@@@@@##-:     .        :.++=:=--  . @@@==   +=  =@@.   %@@@@@=..=.:=       \n" +
"        #+. =%@@@@@+. .%@@@%=.-@#:.=-++**@@@@@@@@@%**-:. :..          :*= :*###.- ::.  ==   +@@@    +@@@@@*.  =#        \n" +
"      . + ::%-@@@@@  .+@@@@@@@@@@@@+=*@:@%@@@@@@@@%%%=+-. -           .#-. .-@.= @@#*@@@@@@@@@@@%    @@@@@.%.  = .      \n" +
"       =. . =#@@@@@   *@@@@@%@@@@@%@+.@+%@@@%%@@@@@**+=:-. -.       ..=@*     --==  -*@@%+=+@@@@=    #@@@@*-    =       \n" +
"       +. .- -@@@@@-:.-@@@@*.  -@@# -**@@%%##@@@@@@***==::::.         %@*      #-:.  .@     #@@@-    @@@@#. -  .=       \n" +
"        -#  .*@@@@@@ :=@@@@@@:::*@--@@@@###+#@@@@@@##+=---.=:       ..+#:       #@*#  @    *@@@@*   %@@@@#=   #.        \n" +
"        @::= +@@@@@@%**=:.-*@@@*%@#@@@*@@*==#@@@@@@%*=-:::=-. .      -=-.      =:.@#  @  *@*     ::*#@@@@@= =: %        \n" +
"       .%#   -%@@@@@@@@@@@@%.:@@@@@@@=@@+..=#@@@@@@@+=:-.:-..:.   . .=+@-      -@# @:=- #   *@@@@@@@@@@@@#.   %#.       \n" +
"        :=   ==%@@@@@@@@@%@@@@%%@@@@@@@*#+%@%@@+@@@%%*:. :- ::-.:..  *@   % % =::@+-@#*+ +@@@@%@@@@@@@@@#::   =:        \n" +
"         +- -+*-.:*##@@@@@@@%-#%#@@@=%@@*+##%%@ @@@@%*-:=.  .*:-   .#@*  =      #@= @@*+%*=%@@@@@@@#*- .:*=. .=         \n" +
"         --  +%- =-*%@@@#@*-*@@@@@@@* -@@@@#%@% %@@@@@@=.  :-.   @@@@=   :   :%@%  -@@@@@@@*-#@%@@@#-.- .#-  ::         \n" +
"            -+: : :=+#@@#@@%#%#@@@##%=   +#@@@@#  *@@@@@%=:=#- *@@#-     =*++-     *-*@@%#%#%@%#@%*+-  . .=.            \n" +
"             . =. =-   -**@@@@@%=@@@@@@@=     .=@-   :@@@@*#@#+@@:     :        #@%@@@%*%@@@@@+-    -: ::               \n" +
"            =:.  :-  +.-:-:==:=@*@%@@@-*@@@@@*:        :@@@@@@@@:         :#@@@#  #@@#@**- :::-.: :  .:  ..=            \n" +
"            :..+*.  #:+ ::-:-::#@@%@@*@#:  .-=**%%##*-:  :@@@@:   .=+%**=-      *@+@@%%+*. ..::: + =   ++. :            \n" +
"               .:  *.:.:-:  =  -#*@@*@+@@#@@+           .. @@..            :%@#@@+@#@%+#       .: .:-  .                \n" +
"                  .=:- .  .  . .=+@@+@@#%+=-=-=--: .  ::==#@@@+-..    ....    .++@%*%%--          :::.                  \n" +
"                   *:.  :=::=. .+:+%@%@%@@@@@@@#@+**-*#@@@@@@@@@@=. *:++*@@@@@@@@@*@*= -  ::..=.  ..=.                  \n" +
"                  .  *:+@@# =+= *-+*@#@#@@@@@@#*@@@@@@@@@@#@@@@@@@@@@%**#%@*@@@*@@+@:=== =*= +%%+-+                     \n" +
"                     :+%@@%##*+#@=**+*%#@%@@@@@@@@@@%*-==##.:#*=:=**@%@*@@%@@@%@*%+==*-#==++*#@@*=:  .                  \n" +
"                        #@@=#@@@+@@-=*#@%@@@@@@@*++@%%%+=-+=== :+*==+..-*@@@@@@+%#+:=%%:@@%-=%@#                        \n" +
"                         . *@@%#@@*%++@%@@@@@@@@*==:.==:..-++=          +%@@@@@#%%+#+*@@=#@@=                           \n" +
"                           #@%:*@@@+*@@%@@@@@@@@*.- .:. ...            .+@@@@@@@=@#-*@%%-:%#*                           \n" +
"                          . % -=:@%-*#:@@@@%*@@@+%-.----              :.#@@#*@@@@-*= @% =.=#                            \n" +
"                            :    :-*=:*=*@%#%%@@@#*::#%-+=:   . -=   .-@@@@*#@@=--.=*:.    :                            \n" +
"                                    : .  =   #-@@*#@@##*@*=.:.+:#*#%%*%##-=  .:    :+                                   \n" +
"                                                         --- =-.                                                        \n" +
"                                                                                                                        \n" +
"                                                                                                                        \n" +
"                                                                                                                        "
        };
//            String[] lines = {
//            "   __      __                               ",
//            "  |  \\    |  \\                              ",
//            " _| $$_    \\$$  ______    ______    ______  ",
//            "|   $$ \\  |  \\ /      \\  /      \\  /      \\ ",
//            " \\$$$$$$  | $$|  $$$$$$\\|  $$$$$$\\|  $$$$$$\\",
//            "  | $$ __ | $$| $$  | $$| $$    $$| $$   \\$$ ",
//            "  | $$|  \\| $$| $$__| $$| $$$$$$$$| $$       ",
//            "   \\$$  $$| $$ \\$$    $$ \\$$     \\| $$       ",
//            "    \\$$$$  \\$$ _\\$$$$$$$  \\$$$$$$$ \\$$      ",
//            "              |  \\__| $$                     ",
//            "               \\$$    $$                     ",
//            "                \\$$$$$$                      "
//            };
//
//            // Tính chiều rộng của console
//            int consoleWidth = 80; // Bạn có thể điều chỉnh chiều rộng console ở đây
//
//            for (String line : lines) {
//                int padding = (consoleWidth - line.length()) / 2; // Tính khoảng trắng ở bên trái
//                String centeredLine = String.format("%" + (padding + line.length()) + "s", line);
//                System.out.println(centeredLine);
//            }
            for (int i = 0; i < TIGER_IMG.length; i++) {
                System.out.println(TIGER_IMG[i]);
            }
            System.out.println("Generating License Key...");
            for(int i = 1; i <= 10; i++){
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println(e);
                }
                System.out.println("Generating " + i * 10 + " %");
            }
            
            String active_code = "";
            active_code = generateActiveCode();
            String customer_id = DBSelectionSet.getInstance().getCustomerID();
            String max_channel = DBSelectionSet.getInstance().getMaxChannelConfig().toString();
            String license_key = generateLicenseKey(active_code, max_channel, customer_id);
            System.out.println("Active Code: " + active_code);
            System.out.println("License Key: " + license_key);
        } catch (Exception ex) {
            System.out.println(ex.getClass() + ": " + ex.getMessage());
        }
  }
}
