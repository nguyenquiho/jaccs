/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import static com.worldfone.utils.helpers.X239hga_LicenseKeyCommon.*;
import javax.servlet.*;

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
            String[]conho = {"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"            :@@@@@@@@                                                                                                                  \n" +
"            @@@@@@@@@@-                                                                                                                \n" +
"           +@@@@@@@@@@@@:                                                       ..-+@@@@@@@=                                           \n" +
"            @@@@@@@@@@@@@:                                                 .@@+@@@@@#----:.+@=.....                                    \n" +
"             *@@@@@@@@@@@@                                               =@- =@@@@. @@@@@@@@@@@@@@@@@*                                 \n" +
"               .@@@* *- *@+                                            -#=  @@@#  %@@@.    *#%#*: :%@@@.                               \n" +
"                  @@@@@- =@                                          *=    +@@   @@+       *@@@@#:  *@#==                              \n" +
"                 *@@@@@@@@                                       -@@@.     :@  .@     %@@   +@@@*   *@@ #@*                            \n" +
"                @#%= .@@%                              %@@@@@@@@@@@@+  = :  # .     =:@@@@   =@+        %@@@-   .@@.                   \n" +
"              @@   *%*@                             =@#@@@@%@@*@@@@@  @. -  .   .  .= @@@@@-       :#       =@ +@@:+                   \n" +
"            #@@@@. @@:                            -@@ *@@@+@* @@@@@. @* #:     :    *- %@# -%-           *%%# :@@@* #                  \n" +
"          %@. *@@@@                              @@@ +@@@@-=  @:+@@ @@..@-     @.    #%*  . -@@**#*+.  .@%+:   :@%.++                  \n" +
"       .@@= %+*@*                              @@@@@-@@ @@   := *@ @@@ +@@  . .@@    =  :  **:--::::*# **- :@%  :@%.                   \n" +
"     .@@@@@@@@                               #@@-@@# @: =@.  *  @. #@@  @@+  @:@@%     @    #@#-::::   :*#.   =  *                     \n" +
"    *=  -@@#                               @@+@@ -@% =   @#    *    @@@ :@@: #* @@@=  #      :     -+  +*-+@@#   @.                    \n" +
"    @#++@@=                           +@@@@@%-+@  *@  -  +#   +      @@@-%@@ -@.@%  :      % .- =  +       # @   %@@:                  \n" +
"    *    #=                .@@% *@@+ .@:@#.@%# @   @*    =            @@@*@@@=@: #     @   -.*@+**  =*  +: ::  =*:@@@+                 \n" +
"    #@@@@@@             %@@:+@+ @@+ :@-@@-.%*@ .   #--     -  :        @@@@@@@@@**-   @@     - .+-.       =: :: : ##@@                 \n" +
"     @@@@@@@        *@@@@+ @:@@#@@ @@*%@=* #-@@    * #    =+@=    =.   =%@@@@@@@@ =   @@:  %:  -:-   =   =:@  .++  @@@+                \n" +
"     +@# .@@@@@@@@@@@ @@@= *@@%#@@+@@+@@ % *+:@@   : @   :@@   -@*          =@@@@@:-+: +@*                   *#% +=%%+@@:              \n" +
"      *@*@@. @  =.@@@ #@@@-    #@@+@#*@% @. #% @@    @=+ %#  =@@-         =@@@@@@@@#*%    *-  :               :=+ *@.@@+@@=            \n" +
"        @@- *- .#:@@@@%.     -+%@-@@+@@- @%  -- #@  %@ @%#   @@   =@+    .         .*+*%==   -    =-    :+    +  *@ @@*+@@@@           \n" +
"           =%@@@@@@@.       @.=@* *@-@@. +@*     @ -@@-# =  %+ +@@-                   ** -=  %     *= . %: %=    @* %. @@@=-+          \n" +
"                           @--@-   @ :@@  .@@+   @-%@ .  +   *@@%  *@     -  :+         @  @+ @-      .   *%   =-@.   %@@  ##          \n" +
"                           @-@  -   : *@@   #@@  ##@*    %% .@@@  @@*  %@.@@@    :      *@@@@+.@@@@@+=@@=%@   .#:@.   @@   @%          \n" +
"                          :@@  #       +@% . %@@:= %     #%*+@@%  @@@ *@%#@@      %  =  :-.@@@ # %-   @=    @#*%.@    *   #@+          \n" +
"                          @@ %@= =      *@ -* @@           .@@@@@@@@@@@@@@@@-       =  #+  +  *@- =. %-       *@-@        @ -.         \n" +
"                          @#@@ . #   :   @ .@ -@          -# .@%+*           .%. :  :   %   +  @ =   -*         *@       :  .:         \n" +
"                         %@@. -.*-+  #   @..@ -@       #:#:-= -@@              = =---  %*:+=@-@%*::-+@%:         * *         :         \n" +
"                        :@@ =%+@* - +@   @ -@.@      .##=   +@@@+              .%@@%*+=+@-##%%@       *         -: %         -         \n" +
"                      +*@@@@.=@- @  @*   @ =@@*  =##*  -+%@@@@%*-                     .   .    +    -*-         # +=         -%        \n" +
"                     +-:@% .@@  =@ -@   *  @@@*%:             +-.                                 :.             @@          :         \n" +
"                     @ @+ @%@   @= %:   - =@@@: +   .@@       @@                                               :*         #   -#@#     \n" +
"                    * .% **@- .#@:=.      @%%@- @    :@@@@@@@@=                                                : -= +  =   *  #   #    \n" +
"                    . =  @=%#+:#@@       @= :%@ @@=    @@@@@@@                                                 .%:  +  +   =  :.  %    \n" +
"                   + *   =@ @#-.@ %@= =@@@--*  =-@@@@@:  .%#                                                     %@@ *:#=. =-..=@=#    \n" +
"                 =*@@ #%%@@*%*%=+%#**%.    %@@.  +@@@@@%=                                                           @#@#:*.% *#=+@:    \n" +
"               :#.  %.  =@=         *         :==-:                                                                     @%*##%**+      \n" +
"             .+      -  #=    :  : @                           -%%%%%%%%: #%#    =%@@@%=   +%%%%%%%  #%%%%%#+                          \n" +
"           *-         :%  @@@  -- @:                            ..@@@@..  @@@. .@@@=.*@@@  @@@:....  @@@*.*@@@                         \n" +
"          -::  =:     %    =@%@#@.                                #@@*    @@@. @@@   =++=  @@@@@@@#  @@@%+@@@@                         \n" +
"          +*  +-  :  =:                                           #@@*    @@@. @@@. =@@@@  @@@       @@@@@@@=                          \n" +
"           *-#  -+  :#                                            #@@*    @@@. .@@@#.*@@@  @@@#****  @@@. @@@@                         \n" +
"           :@:=@@.+@                                              +@@=    @@@    =@@@@@-   #@@@@@@@: @@@.  %@@*                        \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       \n" +
"                                                                                                                                       "
        };
            for (int i = 0; i < conho.length; i++) {
                System.out.println(conho[i]);
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
            
            
            String active_code = yGenActiveCode();
            String customer_id = DBSelectionSet.getInstance().getCustomerID();
            String max_channel = DBSelectionSet.getInstance().getMaxChannelConfig().toString();
            String license_key = xGenerateLicenseKey(active_code, max_channel, customer_id);
            System.out.println("Active Code: " + active_code);
            System.out.println("License Key: " + license_key);
        } catch (Exception ex) {
            System.out.println(ex.getClass() + ": " + ex.getMessage());
        }
  }
}
