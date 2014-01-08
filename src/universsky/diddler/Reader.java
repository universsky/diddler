/**
 * This file is part of Shark.
 * 
 * Shark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Shark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Shark.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Sergio Jiménez Feijóo (sergio.jf89@gmail.com)
 */

package universsky.diddler;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;



// Jnetpcap's imports. Jnetpcap is a Java wrapper with JNI for libpcap.
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import org.jnetpcap.protocol.voip.Rtp;


public class Reader extends ListActivity {

	// Variable declarations for handling the settings.
	private SharedPreferences settings = null;

	// ArrayList which will contain the parsed packets.
	private ArrayList<JPacket> packets = null;

	// Custom adapter for an ArrayList of JPacket.
	private JPacketAdapter p_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reader);

		// Accessing the preferences.
		settings = getSharedPreferences(GlobalConstants.prefsName, 0);

		// Initializing the JPacket ArrayList.
		packets = new ArrayList<JPacket>();

		// Parsing the packets form the .pcap file.
		getPackets();

		// Binding the ArrayAdapter with the view for each row of the ListView.
		p_adapter = new JPacketAdapter(this, R.layout.list_item, packets);

		// Binding the ArrayAdapter with the ListView.
		setListAdapter(p_adapter);

	}

	/**
	 * Opens the .pcap file which is defined in the preferences and parses all
	 * the packets that it contains.<br>
	 * The packet data is copied into the JPacket ArrayList.
	 */
	private void getPackets() {

		StringBuilder errbuf = new StringBuilder();
        boolean SDCardIsMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		String sdPath = Environment.getExternalStorageDirectory().getPath();
        // Opening the .pcap file.
		
        /*
		final Pcap parser = Pcap.openOffline(
				"/mnt/sdcard/" + GlobalConstants.dirName + "/"
						+ settings.getString("fileText", "diddler_capture.pcap"),
				errbuf);
		*/
		
		String fname = sdPath ;
		String suffix = "/";
        if(!sdPath.endsWith(suffix)){
        	fname = sdPath + suffix;
        }
        
		fname = fname + 
				GlobalConstants.dirName + suffix + 
				settings.getString("fileText", "diddler_capture.pcap");
        
        final Pcap parser = Pcap.openOffline(fname, errbuf);
		

		// Creating a handler for packet capture.
		JPacketHandler<String> handler = new JPacketHandler<String>() {

			// Defining the action that will be performed each time a packet is
			// read for the file.
			@Override
			public void nextPacket(JPacket packet, String user) {
				packets.add(packet);
			}

		};

		parser.loop(-1, handler, null);
		parser.close();
	}

	/**
	 * Custom ArrayAdapter for the Jnetpcap's JPacket class.<br>
	 * Sets the view for each single packet that is stored in the ArrayList.
	 */
	private class JPacketAdapter extends ArrayAdapter<JPacket> {

		private ArrayList<JPacket> packets;

		public JPacketAdapter(Context context, int textViewResourceId,
				ArrayList<JPacket> packets) {
			super(context, textViewResourceId, packets);
			this.packets = packets;
		}

		Ip4 ip4 = new Ip4();
		Ip6 ip6 = new Ip6();
		Tcp tcp = new Tcp();
		Udp udp = new Udp();
		Icmp icmp = new Icmp();
		Http http = new Http();
		Rtp rtp = new Rtp();

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			JPacket p = packets.get(position);
			if (p != null) {

				TextView network_protocol = (TextView) v
						.findViewById(R.id.network_protocol_text);
				TextView src_network = (TextView) v
						.findViewById(R.id.src_network_text);
				TextView dst_network = (TextView) v
						.findViewById(R.id.dst_network_text);

				TextView transport_protocol = (TextView) v
						.findViewById(R.id.transport_protocol_text);
				TextView src_transport = (TextView) v
						.findViewById(R.id.src_transport_text);
				TextView dst_transport = (TextView) v
						.findViewById(R.id.dst_transport_text);

				TextView application_protocol = (TextView) v
						.findViewById(R.id.application_protocol_text);

				TextView packet_number = (TextView) v
						.findViewById(R.id.packet_number_text);

				packet_number.setText(Integer.toString(position + 1));

				if (p.hasHeader(Ip4.ID)) {
					p.getHeader(ip4);
					network_protocol.setText("IPv4");
					src_network.setText(FormatUtils.ip(ip4.source()));
					dst_network.setText(FormatUtils.ip(ip4.destination()));

					if (p.hasHeader(Tcp.ID)) {
						p.getHeader(tcp);
						transport_protocol.setText("TCP");
						src_transport.setText(Integer.toString(tcp.source()));
						dst_transport.setText(Integer.toString(tcp
								.destination()));

						if (p.hasHeader(Http.ID)) {
							p.getHeader(http);
							application_protocol.setText("HTTP");
						} else if (p.hasHeader(Rtp.ID)) {
							p.getHeader(rtp);
							application_protocol.setText("RTP");
						} else
							application_protocol
									.setText(getString(R.string.unknown));
					}

					else if (p.hasHeader(Udp.ID)) {
						p.getHeader(udp);
						transport_protocol.setText("UDP");
						src_transport.setText(Integer.toString(udp.source()));
						dst_transport.setText(Integer.toString(udp
								.destination()));
						if (p.hasHeader(Rtp.ID)) {
							p.getHeader(rtp);
							application_protocol.setText("RTP");
						}
					}
				}

				else if (p.hasHeader(Icmp.ID)) {
					p.getHeader(icmp);
					network_protocol.setText("ICMP");
					src_network.setText(getString(R.string.unknown));
					dst_network.setText(getString(R.string.unknown));
				}

				else if (p.hasHeader(Ip6.ID)) {
					p.getHeader(ip6);
					network_protocol.setText("IPv6");
					src_network.setText(FormatUtils.asStringIp6(ip6.source(),
							true));
					dst_network.setText(FormatUtils.asStringIp6(
							ip6.destination(), true));

					if (p.hasHeader(Tcp.ID)) {
						p.getHeader(tcp);
						transport_protocol.setText("TCP");
						src_transport.setText(Integer.toString(tcp.source()));
						dst_transport.setText(Integer.toString(tcp
								.destination()));

						if (p.hasHeader(Http.ID)) {
							p.getHeader(http);
							application_protocol.setText("HTTP");
						} else if (p.hasHeader(Rtp.ID)) {
							p.getHeader(rtp);
							application_protocol.setText("RTP");
						} else
							application_protocol
									.setText(getString(R.string.unknown));
					}

					else if (p.hasHeader(Udp.ID)) {
						p.getHeader(udp);
						transport_protocol.setText("UDP");
						src_transport.setText(Integer.toString(udp.source()));
						dst_transport.setText(Integer.toString(udp
								.destination()));
						if (p.hasHeader(Rtp.ID)) {
							p.getHeader(rtp);
							application_protocol.setText("RTP");
						}
					}
				}
			}
			return v;
		}
	}
}