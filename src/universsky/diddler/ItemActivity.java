/**
 * 
 */
package universsky.diddler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author 东海陈光剑
 * 2014年1月21日  下午5:34:27
 */
public class ItemActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item);
		TextView itemTextView = (TextView) findViewById(R.id.itemView1);
		TextView urlTextView = (TextView) findViewById(R.id.urlView);
		
		//Retrieves a map of extended data from the intent.
		Bundle bundle = getIntent().getExtras();
		
		//Returns the value associated with the given key,
		//or null if no mapping of the desired type exists for the given key 
		//or a null value is explicitly associated with the key
		CharSequence item = bundle.getCharSequence("item");//读出数据
		//Open Declaration void android.widget.TextView.setText(CharSequence text)
		itemTextView.setText(item);
		//itemTextView.append(item);
		
		String path = "";
		String host = "";
		String url = "http://";
		String itemStr = item.toString();
		
		Matcher m1 = Pattern.compile("GET\\s(.*)\\sHTTP/1.1").matcher(itemStr);
		
		if (m1.find()){
			path = itemStr.substring(m1.start(),m1.end());
			path = path.substring( path.indexOf("GET") + 4, path.indexOf("HTTP/1.1")-1);
		}
		
		Matcher m2 = Pattern.compile("Host:\\s(.*)").matcher(itemStr);
		if (m2.find()){
			host = itemStr.substring(m2.start(),m2.end());
			host = host.substring( host.indexOf("Host:") + 5);
		}
		
		url += host + path;
		url = url.replaceAll("\\s*|\t|\r|\n", "");
		urlTextView.setText(url);		
		
	}

}
