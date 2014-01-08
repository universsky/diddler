
package universsky.diddler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnetpcap.PcapHeader;
import org.jnetpcap.packet.JScanner;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.format.FormatUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Allows an Android app to interact with the standard output of a TCPdump
 * process and create a notification that warns about TCPdump is running.
 */
public class TCPdumpHandler {

	private static final String capFileName = "diddler_capture.pcap";
	// Constants definition.
	private static final int defaultRefreshRate = 100;
	private static final int defaultBufferSize = 1024;
	
	// Your Main activity's ids for the View.
	private static final int paramsId = R.id.params_text;
	private static final int outputId = R.id.output_text;
	private static final int scrollerId = R.id.scroller;
	private static final int pbarId = R.id.running_progressbar;

	// TextView's refresh rate in ms.
	

	// Byte[] buffer's size.
	
	private static final int MaxSize = 20480;
	private int refreshRate = 100;
	private int bufferSize = 2048;
	private int countPackets = 0;

	private boolean notificationEnabled = false;
	private boolean refreshingActive = false;

	private TCPdump tcpdump = null;

	private Handler isHandler = null;

	private Context mContext = null;
	private SharedPreferences settings = null;
	private NotificationManager nManager = null;
	private Notification notification = null;

	private TextView outputText = null;
	private View scroller = null;
	private ProgressBar pbar = null;
	private EditText params = null;
	
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * This runnable is used for refreshing the TCPdump's process standard
	 * output.
	 */
	private Runnable updateOutputText = new Runnable() {
		public void run() {
			try {
				// 打开tcpdump资源文件
				mContext.getResources().openRawResource(R.raw.tcpdump);
				// 获取tcpdump命令执行的结果
				if ((tcpdump.getInputStream().available() > 0) == true) {
					byte[] buffer = new byte[bufferSize];
					//String getPath = "";
					//String hostName = "";
					//String buffer = "";
					try {
						// 把tcpdump命令执行的结果的 “输出流” 赋值给字节数组buffer
						//int java.io.DataInputStream.read(byte[] buffer, int offset, int length) throws IOException
						tcpdump.getInputStream().read(buffer, 0, bufferSize);
						//逐行读字节流
						
						//buffer = tcpdump.getInputStream().readLine();
						// Clears the screen if it's full.
//						if (outputText.length() + buffer.length() >= MaxSize)
//							outputText.setText("");
						
						if (outputText.length() + buffer.length >= MaxSize)
							outputText.setText("");
						
						// Refreshing the output TextView
						// Show the GET/HTTP data on the Scroll View of android
						
						
						/* Append the specified text to the TextView's display buffer, 
						 * upgrading it to BufferType.
						 * EDITABLE if it was not already editable. 
						 */
						 
//						String sBuffer = new String(buffer,"UTF-8");
//						outputText.append(sBuffer);
						/*
						if(buffer.indexOf("IP") != -1){
							buffer = "["+  countPackets + "]" +  " Request Header: \n";
							countPackets ++;
						}
						*/
						
						//读IP所在行的next line
//						buffer = tcpdump.getInputStream().readLine();
						//继续读nextline, 是为GET /path 数据行
//						buffer = tcpdump.getInputStream().readLine();
						/*
						if(buffer.indexOf("GET") != -1){
							//buffer = buffer.substring( buffer.indexOf("GET"), buffer.indexOf("HTTP/1.1"));
							buffer = buffer.substring( buffer.indexOf("GET") );
						}
						*/
						
						
						
//						if(buffer.indexOf("Host") != -1){
//							hostName = buffer.substring(buffer.indexOf("Host") + 6);
//						}
						
//						if( !("".equals(hostName)) && !("".equals(getPath)) ) {
//							String reqUrl = "http://" + hostName + getPath ; 
//							countPackets ++;
//							String appendStr = "["+ countPackets + "]" + reqUrl + "\n";
//							outputText.append(appendStr);
//						}
						
					} catch (IOException e) {
						stopRefreshing();
						return;
					}
//					outputText.append(buffer + "\n");
					//outputText.append(new String(buffer,"ISO-8859-1"));
					// get rid of non-ASCII chars
					String bufferStr = new String(buffer);
					
					/*
					String pattern = "[^\\x00-\\x7F]";
					Pattern p = Pattern.compile(pattern);
					Matcher m = p.matcher(bufferStr);
					String outBuffer = m.replaceAll("");
					outputText.append(outBuffer);
					*/
					
					/**
					 * Open Declaration java.util.regex.Pattern



Java supports a subset of Perl 5 regular expression syntax. An important gotcha is that Java has no regular expression literals, and uses plain old string literals instead. This means that you need an extra level of escaping. For example, the regular expression \s+ has to be represented as the string "\\s+". 

Escape sequences
\  Quote the following metacharacter (so \. matches a literal .). 
\Q  Quote all following metacharacters until \E. 
\E  Stop quoting metacharacters (started by \Q). 
\\  A literal backslash. 
\\uhhhh  The Unicode character U+hhhh (in hex). 
\xhh  The Unicode character U+00hh (in hex). 
\cx  The ASCII control character ^x (so \cH would be ^H, U+0008). 
\a  The ASCII bell character (U+0007). 
\e  The ASCII ESC character (U+001b). 
\f  The ASCII form feed character (U+000c). 
\n  The ASCII newline character (U+000a). 
\r  The ASCII carriage return character (U+000d). 
\t  The ASCII tab character (U+0009). 


Character classes
It's possible to construct arbitrary character classes using set operations: [abc]  Any one of a, b, or c. (Enumeration.) 
[a-c]  Any one of a, b, or c. (Range.) 
[^abc]  Any character except a, b, or c. (Negation.) 
[[a-f][0-9]]  Any character in either range. (Union.) 
[[a-z]&&[jkl]]  Any character in both ranges. (Intersection.) 


Most of the time, the built-in character classes are more useful: \d  Any digit character (see note below). 
\D  Any non-digit character (see note below). 
\s  Any whitespace character (see note below). 
\S  Any non-whitespace character (see note below). 
\w  Any word character (see note below). 
\W  Any non-word character (see note below). 
\p{NAME}  Any character in the class with the given NAME.  
\P{NAME}  Any character not in the named class.  


Note that these built-in classes don't just cover the traditional ASCII range. For example, \w is equivalent to the character class [\p{Ll}\p{Lu}\p{Lt}\p{Lo}\p{Nd}]. For more details see Unicode TR-18, and bear in mind that the set of characters in each class can vary between Unicode releases. If you actually want to match only ASCII characters, specify the explicit characters you want; if you mean 0-9 use [0-9] rather than \d, which would also include Gurmukhi digits and so forth. 

There are also a variety of named classes: 

Unicode category names, prefixed by Is. For example \p{IsLu} for all uppercase letters. 
POSIX class names. These are 'Alnum', 'Alpha', 'ASCII', 'Blank', 'Cntrl', 'Digit', 'Graph', 'Lower', 'Print', 'Punct', 'Upper', 'XDigit'. 
Unicode block names, as used by forName(String) prefixed by In. For example \p{InHebrew} for all characters in the Hebrew block. 
Character method names. These are all non-deprecated methods from Character whose name starts with is, but with the is replaced by java. For example, \p{javaLowerCase}. 
Quantifiers
Quantifiers match some number of instances of the preceding regular expression. *  Zero or more. 
?  Zero or one. 
+  One or more. 
{n}  Exactly n. 
{n,}  At least n. 
{n,m}  At least n but not more than m. 


Quantifiers are "greedy" by default, meaning that they will match the longest possible input sequence. There are also non-greedy quantifiers that match the shortest possible input sequence. They're same as the greedy ones but with a trailing ?: *?  Zero or more (non-greedy). 
??  Zero or one (non-greedy). 
+?  One or more (non-greedy). 
{n}?  Exactly n (non-greedy). 
{n,}?  At least n (non-greedy). 
{n,m}?  At least n but not more than m (non-greedy). 


Quantifiers allow backtracking by default. There are also possessive quantifiers to prevent backtracking. They're same as the greedy ones but with a trailing +: *+  Zero or more (possessive). 
?+  Zero or one (possessive). 
++  One or more (possessive). 
{n}+  Exactly n (possessive). 
{n,}+  At least n (possessive). 
{n,m}+  At least n but not more than m (possessive). 


Zero-width assertions
^  At beginning of line. 
$  At end of line. 
\A  At beginning of input. 
\b  At word boundary. 
\B  At non-word boundary. 
\G  At end of previous match. 
\z  At end of input. 
\Z  At end of input, or before newline at end. 


Look-around assertions
Look-around assertions assert that the subpattern does (positive) or doesn't (negative) match after (look-ahead) or before (look-behind) the current position, without including the matched text in the containing match. The maximum length of possible matches for look-behind patterns must not be unbounded. 

(?=a)  Zero-width positive look-ahead. 
(?!a)  Zero-width negative look-ahead. 
(?<=a)  Zero-width positive look-behind. 
(?<!a)  Zero-width negative look-behind. 


Groups
(a)  A capturing group. 
(?:a)  A non-capturing group. 
(?>a)  An independent non-capturing group. (The first match of the subgroup is the only match tried.) 
\n  The text already matched by capturing group n. 


See group() for details of how capturing groups are numbered and accessed. 

Operators
ab  Expression a followed by expression b. 
a|b  Either expression a or expression b. 


Flags
(?dimsux-dimsux:a)  Evaluates the expression a with the given flags enabled/disabled. 
(?dimsux-dimsux)  Evaluates the rest of the pattern with the given flags enabled/disabled. 


The flags are: i CASE_INSENSITIVE case insensitive matching 
d UNIX_LINES only accept '\n' as a line terminator 
m MULTILINE allow ^ and $ to match beginning/end of any line 
s DOTALL allow . to match '\n' ("s" for "single line") 
u UNICODE_CASE enable Unicode case folding 
x COMMENTS allow whitespace and comments 


Either set of flags may be empty. For example, (?i-m) would turn on case-insensitivity and turn off multiline mode, (?i) would just turn on case-insensitivity, and (?-m) would just turn off multiline mode. 

Note that on Android, UNICODE_CASE is always on: case-insensitive matching will always be Unicode-aware. 

There are two other flags not settable via this mechanism: CANON_EQ and LITERAL. Attempts to use CANON_EQ on Android will throw an exception. 

Implementation notes
The regular expression implementation used in Android is provided by ICU. The notation for the regular expressions is mostly a superset of those used in other Java language implementations. This means that existing applications will normally work as expected, but in rare cases Android may accept a regular expression that is not accepted by other implementations. 

In some cases, Android will recognize that a regular expression is a simple special case that can be handled more efficiently. This is true of both the convenience methods in String and the methods in Pattern.

See Also
Matcher 
Summary
Constants 
int CANON_EQ This constant specifies that a character in a Pattern and a character in the input string only match if they are canonically equivalent. 
int CASE_INSENSITIVE This constant specifies that a Pattern is matched case-insensitively. 
int COMMENTS This constant specifies that a Pattern may contain whitespace or comments. 
int DOTALL This constant specifies that the '.' meta character matches arbitrary characters, including line endings, which is normally not the case. 
int LITERAL This constant specifies that the whole Pattern is to be taken literally, that is, all meta characters lose their meanings. 
int MULTILINE This constant specifies that the meta characters '^' and '$' match only the beginning and end of an input line, respectively. 
int UNICODE_CASE This constant specifies that a Pattern that uses case-insensitive matching will use Unicode case folding. 
int UNIX_LINES This constant specifies that a pattern matches Unix line endings ('\n') only against the '.', '^', and '$' meta characters. 

0-127 the ASCII code range, in hex, 0x00-0x7F
					 */
					
					/**
					 * @author chenguangjian
					 * 		   2014.1.8
					 * 正则匹配出需要的数据包段
					 */
					String pStr = "GET\\s+[\\x00-\\x7F]*\\nHost:[\\x00-\\x7F]*\\n\\n";
//					String pStr = "GET\\s+[\\x00-\\x7F]*";
					Pattern p = Pattern.compile(pStr);
					Matcher m = p.matcher(bufferStr);
					String mStr = "";
					while(m.find()){
						int s = m.start();
						int e = m.end();
						mStr += "\n[" + countPackets + "]" + 
								bufferStr.substring(s, e) +
								"------------------------------------------\n\n";
						countPackets++;
					}
					outputText.append(mStr);
					
					
					/**
					String[] packetArray = {};
					String regularExpression = "\n\n";
					packetArray = sBuffer.split(regularExpression);
					
					
					String sLs = "\n------------------------------------\n\n";
					for(String s:packetArray){
						countPackets++;
						outputText.append("[" + countPackets + "]" + s + sLs);
						
						
					    String[] linesOfPacket = s.split("\n");
					    String url  = "";
					    String path = "";
					    String host = "";
					    for(String l:linesOfPacket){
					    	if(l.indexOf("GET") != -1 ){
					    		path = l.substring(l.indexOf("GET "), l.length() - l.indexOf("HTTP/1.1")-1);
					    	}
					    	if(l.indexOf("Host") != -1){
					    		host = l.substring(l.indexOf("Host:"));
					    	}
					    }
					    url ="http://" + host + path;
					    // Append the output string
					    String reqUrl = "Request URL: " + url + "\n";
					    outputText.append(reqUrl + s + sLs);
					   
					}
					
					 */
					
					// Forces the scrollbar to be at the bottom.
					scroller.post(new Runnable() {
						public void run() {
							scroller.scrollTo(outputText.getMeasuredWidth(),
									outputText.getMeasuredHeight());
						}
					});

				}
			} catch (IOException e) {
				stopRefreshing();
				return;
			}
			isHandler.postDelayed(updateOutputText, refreshRate);
		}
	};

	
	////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	public TCPdumpHandler(TCPdump tcpdump, Context mContext, Activity activity,
			boolean notificationEnabled) {

		// Acessing the app's settings.
		settings = mContext.getSharedPreferences(GlobalConstants.prefsName, 0);

		this.tcpdump = tcpdump;
		isHandler = new Handler();

		this.params = (EditText) activity.findViewById(paramsId);
		this.outputText = (TextView) activity.findViewById(outputId);
		this.scroller = (View) activity.findViewById(scrollerId);
		this.pbar = (ProgressBar) activity.findViewById(pbarId);

		this.mContext = mContext;
		this.notificationEnabled = notificationEnabled;

		if (notificationEnabled) {
			// Asociating the System's notification service with the
			// notification manager.
			nManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);

			// Defining a notification that will be displayed when TCPdump
			// starts.
			notification = new Notification(R.drawable.icon,
					mContext.getString(R.string.tcpdump_notification),
					System.currentTimeMillis());
			notification.setLatestEventInfo(mContext, "diddler", mContext
					.getString(R.string.tcpdump_notification_msg),
					PendingIntent.getActivity(mContext, 0, new Intent(mContext,
							Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP),
							PendingIntent.FLAG_CANCEL_CURRENT));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
	}

	/**
	 * Starts a TCPdump process, enables refreshing and posts a notification.
	 * 
	 * @param params
	 *            The parameters that TCPdump will use. For example: -i
	 *            [interface name] -s [snaplen size] -w [filename]
	 * 
	 * @return 0 Everything went OK.<br>
	 *         -1 TCPdump is already running.<br>
	 *         -2 The device isn't rooted.<br>
	 *         -4 Error when running the TCPdump command.<br>
	 *         -5 Error when flushing the DataOutputStream.
	 */
	
	public int start(String params) {
		
		int TCPdumpReturn;
		
		if ((TCPdumpReturn = tcpdump.start(params)) == 0) {
			// if save to file, the outputText show  
			if (settings.getBoolean("saveCheckbox", false) == true) {
				
				outputText.setText(
						mContext.getString(R.string.standard_output_disabled)
						+ GlobalConstants.dirName
						+ "/"
						+ settings.getString("fileText", capFileName)
						);
				
			} else {//if not save to file, show on the scroll view
				
				outputText.setText(mContext.getString(R.string.standard_output_enabled));
				startRefreshing();
				
			}
			
			setProgressbarVisible();
			if (notificationEnabled)
				postNotification();
			return 0;
		} else
			return TCPdumpReturn;
	}

	/**
	 * Stops the TCPdump process, disables refreshing and removes the
	 * notification.
	 * 
	 * 
	 * @return 0: Everything went OK.<br>
	 *         -1: TCPdump wasn't running.<br>
	 *         -2: The device isn't rooted.<br>
	 *         -4: Error when running the killall command.<br>
	 *         -5: Error when flushing the output stream.<br>
	 *         -6: Error when closing the shell.<br>
	 *         -7: Error when waiting for the process to finish.
	 */
	public int stop() {
		int TCPdumpReturn;
		if ((TCPdumpReturn = tcpdump.stop()) == 0) {
			stopRefreshing();
			setProgressbarInvisible();
			if (notificationEnabled)
				removeNotification();
			return 0;
		} else
			return TCPdumpReturn;
	}

	/**
	 * Starts refreshing the TextView.
	 */
	private void startRefreshing() {
		if (!refreshingActive) {
			isHandler.post(updateOutputText);
			refreshingActive = true;
		}
	}

	/**
	 * Stops refreshing the TextView.
	 */
	private void stopRefreshing() {
		if (refreshingActive) {
			isHandler.removeCallbacks(updateOutputText);
			refreshingActive = false;
		}
	}

	private void postNotification() {
		nManager.notify(0, notification);
	}

	private void removeNotification() {
		nManager.cancel(0);
	}

	private void setProgressbarVisible() {
		pbar.setVisibility(ProgressBar.VISIBLE);
	}

	private void setProgressbarInvisible() {
		pbar.setVisibility(ProgressBar.INVISIBLE);
	}

	/**
	 * Sets the refreshRate value. refreshRate must be > 0.
	 * 
	 * @param refreshRate
	 *            The TextView's refresh rate in ms.
	 * @return true if the new value has been set.<br>
	 *         false if refreshRate hasn't been modified.
	 */
	public boolean setRefreshRate(int refreshRate) {
		if ((refreshRate > 0) && (tcpdump.getProcessStatus() == false)) {
			this.refreshRate = refreshRate;
			return true;
		} else
			return false;
	}

	/**
	 * Sets the bufferSize value. bufferSize must be > 0.
	 * 
	 * @param bufferSize
	 *            The bufferSize must be > 0.
	 * @return true if the new value has been set.<br>
	 *         false if bufferSize hasn't been modified.
	 */
	public boolean setBufferSize(int bufferSize) {
		if ((bufferSize > 0) && (tcpdump.getProcessStatus() == false)) {
			this.bufferSize = bufferSize;
			return true;
		} else
			return false;
	}

	/**
	 * Checks if the device's interface that will be used for capturing is up.
	 * 
	 * @return true if the selected interface is up.<br>
	 *         false if the selected interface is down.
	 */
	public boolean checkNetworkStatus() {

		// Variables used for checking the network state.
		final ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if ((wifi.isConnected() == true) || (mobile.isConnected() == true)) {
			return true;
		} else
			return false;
	}

	/**
	 * Generates the parameters that TCPdump will use by reading the options and
	 * copies it in the parameters EditText.
	 * 
	 * @return A string with the parameters.
	 */
	public void generateCommand() {
		
		/*
		// Defining a String which will contain the command.
		String command = new String("");
		
		*/

		// Recognizing the chosen interface.
		/**
		 * -i
Listen on interface. If unspecified, tcpdump searches the system interface list for the lowest numbered, configured up interface (excluding loopback), which may turn out to be, for example, ``eth0''.
On Linux systems with 2.2 or later kernels, an interface argument of ``any'' can be used to capture packets from all interfaces. Note that captures on the ``any'' device will not be done in promiscuous mode.
If the -D flag is supported, an interface number as printed by that flag can be used as the interface argument.
		 */
		
		/*
		command = command
				+ "-i "
				+ TCPdumpInterface.listInterfaces(true)
						.get(settings.getInt("selectedInterface", 0))
			
						.getIfname();
*/
		
		
		// Recognizing the promiscuous mode.
		/**
		 * -p
Don't put the interface into promiscuous mode.
 Note that the interface might be in promiscuous mode for some other reason;
  hence, `-p' cannot be used as an abbreviation for `ether host {local-hw-addr}
   or ether broadcast'.
		 */
		
		/*
		 
		if (settings.getBoolean("promiscCheckbox", false) == false) {
			command = command + " -p";
		}
		
		*/

		// Recognizing the verbose level.
		
		/*
		 *  -v
			When parsing and printing, produce (slightly more) verbose output. For example, the time to live, identification, total length and options in an IP packet are printed. Also enables additional packet integrity checks such as verifying the IP and ICMP header checksum.
			When writing to a file with the -w option, report, every 10 seconds, the number of packets captured.
			-vv
			Even more verbose output. For example, additional fields are printed from NFS reply packets, and SMB packets are fully decoded.
			-vvv
			Even more verbose output. For example, telnet SB ... SE options are printed in full. With -X Telnet options are printed in hex as well.
		 */
		
		/*
		if (settings.getBoolean("verboseCheckbox", false) == true) {
			switch (settings.getInt("verboseLevel", 0)) {
			case 0: {
				command = command + " -v";
				break;
			}
			case 1: {
				command = command + " -vv";
				break;
			}
			case 2: {
				command = command + " -vvv";
				break;
			}
			}
		}
		*/

		// Recognizing the snaplen size.
		/*Snarf snaplen bytes of data from each packet 
		 * rather than the default of 65535 bytes. 
		 * Packets truncated because of a limited snapshot 
		 * are indicated in the output with ``[|proto]'',
		 *  where proto is the name of the protocol level at 
		 *  which the truncation has occurred. 
		 *  Note that taking larger snapshots both increases 
		 *  the amount of time it takes to process packets and, 
		 *  effectively, decreases the amount of packet buffering.
		 *   This may cause packets to be lost. 
		 *   You should limit snaplen to the smallest number that will 
		 *   capture the protocol information you're interested in.
		 *    Setting snaplen to 0 sets it to the default of 65535, 
		 *    for backwards compatibility with recent older versions of tcpdump.
		 * 
		 */
		
		/*
	 
		if (settings.getBoolean("snaplenCheckbox", false) == true) {
			command = command + " -s "
					+ Integer.toString(settings.getInt("snaplenValue", 0));
		}
		
		*/
		

		/**
		 * Capture the HTTP Requests 
		 */
		// hard set the command
		// hex : GE T --- 0x4745 54
		//       HT TP --- 0x4854 5450
		// tcpdump : datasize must be 1,2 or 4. 
		//String command1 =" -vvennSs 0 -i any tcp[20:2]=0x4745 or tcp[20:2]=0x4854";
		String command1 = "-Av -i any tcp[20:4]=0x47455420";
		// Recognising the output file.
		// if save to file on sdcard
		
		/*
		if (settings.getBoolean("saveCheckbox", false) == true) {
			// If the directory in the sdcard isn't created we are going to
			// create it now.
			FileManager.checkDirectory(GlobalConstants.dirName);
			
			//String sdPath = Environment.getExternalStorageDirectory().getPath();
			
			command1 = command1 + " -w "+ "/mnt/sdcard/" + GlobalConstants.dirName
					+ "/"
					+ settings.getString("fileText", capFileName);
			
/*
			command = command + " -w /mnt/sdcard/" + GlobalConstants.dirName
					+ "/"
					+ settings.getString("fileText", capFileName);
					*/
		//}
		
		params.setText(command1);
	}
	

	
	
}
