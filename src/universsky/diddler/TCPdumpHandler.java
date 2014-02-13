
package universsky.diddler;

import java.io.IOException;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
	//private static final int outputId = R.id.output_text;
	//private static final int scrollerId = R.id.scroller;
	private static final int pbarId = R.id.running_progressbar;

	// TextView's refresh rate in ms.
	

	// Byte[] buffer's size.
	
	private static final int MaxSize = 10240;
	private int refreshRate = 100;
	private int bufferSize = 1024;
	private int countPackets = 0;
	private int MAX_COUNT = 200;

	private boolean notificationEnabled = false;
	private boolean refreshingActive = false;

	private TCPdump tcpdump = null;

	private Handler isHandler = null;

	private Context mContext = null;
	private SharedPreferences settings = null;
	private NotificationManager nManager = null;
	private Notification notification = null;

	private ProgressBar pbar = null;
	private EditText params = null;
	private List<String> itemList = new ArrayList<String>();
	private List<String> itemList2 = new ArrayList<String>();
	private ListView list = null;
	private Activity activity = null;
	
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
						
						
						/**
						if (outputText.length() + buffer.length >= MaxSize)
							outputText.setText("");
						*/
						
						// 防止无限加载，导致内存溢出
						
						if ( itemList.size() > MAX_COUNT ) {
							// 把itemList置空
							itemList.clear();	
							itemList2.clear();
						}
						
						
					} catch (IOException e) {
						stopRefreshing();
						return;
					}
					String bufferStr = new String(buffer);
					
					/**
					 * @author chenguangjian
					 * 		   2014.1.8
					 * 正则匹配出需要的数据包段
					 */
					
					String pStr = "GET\\s+[\\x00-\\x7F]*\\nHost:[\\x00-\\x7F]*\\n\\n";
//					String pStr = "GET\\s+[\\x00-\\x7F]*";
					Pattern p = Pattern.compile(pStr);
					Matcher m0 = p.matcher(bufferStr);
//					String mStr = "";
					while(m0.find()){
						String item = "";
						int s = m0.start();
						int e = m0.end();
						item = bufferStr.substring(s, e);
						itemList.add(item);
//						mStr += "\n[" + countPackets + "]" + 
//								bufferStr.substring(s, e);
						countPackets++;
					}
					
					//outputText.append(mStr);
					
					final ArrayList<HashMap<String,Object>> listItem = new ArrayList<HashMap<String,Object>>();
					final ArrayList<HashMap<String,Object>> listItem2 = new ArrayList<HashMap<String,Object>>();
					for(int i = 0; i < itemList.size(); i++){
						HashMap<String,Object> map = new HashMap<String,Object>();
						java.text.DateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						String s =  fmt.format(new Date());
						map.put("item_title", "[ " + s + "]" + "第" + i + "个HTTP请求:  " );
						
						//获取GET path
						String item = itemList.get(i);
						map.put("item_text0", item);
						listItem2.add(map);
						String request = "";
						Matcher m1 = Pattern.compile("GET\\s(.*)\\sHTTP/1.1").matcher(item);
						if (m1.find()){
							request = item.substring(m1.start(),m1.end());						
							map.put("item_text", request);
							listItem.add(map);
						}
					}
					//生成适配器的Item和动态数组对应的元素
					/**
					 * android.widget.SimpleAdapter.SimpleAdapter(
					 * Context context, 
					 * List<? extends Map<String, ?>> data, 
					 * int resource, 
					 * String[] from, 
					 * int[] to
					 * )
					 */
					android.widget.SimpleAdapter listItemAdapter = new android.widget.SimpleAdapter(
							mContext,     	// Context
							listItem,	// List
							R.layout.list_items, // int resource 
							new String[]{"item_title","item_text"}, // String[] from 
							new int[]{R.id.ItemTitle,R.id.ItemText} // int[] to
							);
					//Sets the data behind this ListView. 
					list.setAdapter(listItemAdapter);
					//选中listview的指定列，选中了，自然就得让这个item可见，自然就滚动咯  
					list.setSelection(list.getBottom());
					 //添加点击  
			        list.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
								long arg3) {
							CharSequence item = (CharSequence) listItem2.get(arg2).get("item_text0");
							//Open Declaration Toast android.widget.Toast.makeText(Context context, CharSequence text, int duration)
							//Toast.makeText(mContext, item, Toast.LENGTH_LONG).show();
							Intent intent=new Intent();
							intent.setClass( activity, ItemActivity.class );
							Bundle mBundle=new Bundle();
							/*
							 * Inserts a CharSequence value into the mapping of this Bundle, replacing any existing value for the given key. Either key or value may be null.
							 */
							mBundle.putCharSequence("item",  item);
							intent.putExtras(mBundle);
							mContext.startActivity(intent);
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

	
	public TCPdumpHandler(TCPdump tcpdump, Context mContext, Activity activity,
			boolean notificationEnabled) {

		this.activity = activity;
		// Acessing the app's settings.
		settings = mContext.getSharedPreferences(GlobalConstants.prefsName, 0);

		this.tcpdump = tcpdump;
		isHandler = new Handler();

		this.params = (EditText) activity.findViewById(paramsId);
		//this.outputText = (TextView) activity.findViewById(outputId);
		//this.scroller = (View) activity.findViewById(scrollerId);
		this.list = (ListView)activity.findViewById(R.id.listView1);
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
			} else {//if not save to file, show on the scroll view
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
