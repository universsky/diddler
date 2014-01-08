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

package com.shark;

import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.provider.Settings;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main extends Activity {

	// Variable declarations for handling the view items in the layout.
	private Button start_button;
	private Button stop_button;
	private Button read_button;
	private EditText parameters;

	// Variable declarations for handling the TCPdump process.
	private TCPdump tcpdump = null;
	private TCPdumpHandler tcpDumpHandler = null;
	private SharedPreferences settings = null;

	// Variable declarations for handling the options and reader activities.
	private Intent optionsIntent = null;
	private Intent readerIntent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Associating the items in the view to the variables.
		start_button = (Button) findViewById(R.id.start_button);
		stop_button = (Button) findViewById(R.id.stop_button);
		read_button = (Button) findViewById(R.id.read_button);
		parameters = (EditText) findViewById(R.id.params_text);

		// Accessing the app's preferences.
		settings = getSharedPreferences(GlobalConstants.prefsName, 0);

		// Extracting the TCPdump binary to the app folder.
		if (RootTools.installBinary(Main.this, R.raw.tcpdump, "tcpdump") == false) {
			new AlertDialog.Builder(Main.this)
					.setTitle(R.string.extraction_error)
					.setMessage(R.string.extraction_error_msg)
					.setNeutralButton(R.string.ok, null).show();
		}

		// Creating a new TCPdump object.
		tcpdump = new TCPdump();

		// Creating a TCPdump handler for the TCPdump object created after.
		tcpDumpHandler = new TCPdumpHandler(tcpdump, this, this, true);

		// Obtaining the command from the options that were saved last time
		// Shark was running.
		tcpDumpHandler.generateCommand();

		start_button.setOnClickListener(new OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			@Override
			public void onClick(View v) {
				startTCPdump();
			}
		});

		stop_button.setOnClickListener(new OnClickListener() {
			// Setting the action to perform when the stop button is pressed.
			@Override
			public void onClick(View v) {
				stopTCPdump();
			}
		});

		read_button.setOnClickListener(new OnClickListener() {
			// Setting the action to perform when the open in reader button is
			// pressed.
			@Override
			public void onClick(View v) {
				launchReader();
			}
		});

		BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Setting the action to be performed when the network status
				// changes.
				if ((tcpDumpHandler.checkNetworkStatus() == false)
						&& (tcpdump.getProcessStatus())) {
					stopTCPdump();
					new AlertDialog.Builder(Main.this)
							.setTitle(
									getString(R.string.network_connection_down))
							.setMessage(
									getString(R.string.network_connection_down_msg))
							.setNeutralButton(getString(R.string.ok), null)
							.show();
				}
			}
		};

		// Registering the BroadcastReceiver and associating it with the
		// connectivity change event.
		registerReceiver(connectionReceiver, new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Setting the action to perform when returning to this activity from
		// another activity which had been called.
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			if (tcpdump.getProcessStatus()) {
				new AlertDialog.Builder(Main.this)
						.setTitle(getString(R.string.settings_changed))
						.setMessage(getString(R.string.settings_changed_msg))
						.setNeutralButton(getString(R.string.ok), null).show();
			}
			tcpDumpHandler.generateCommand();
		}
	}

	@Override
	public void onDestroy() {
		// Setting the action to perform when the Android O.S. kills this
		// activity.
		if (tcpdump.getProcessStatus()) {
			stopTCPdump();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// This code makes the activity to show a menu when the device's menu
		// key is pressed.
		menu.add(0, 0, 0, getString(R.string.options_text));
		menu.add(0, 1, 0, getString(R.string.about_text));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Setting the action to perform when an option from the menu is
		// selected.
		switch (item.getItemId()) {
		case 0:
			optionsIntent = new Intent(Main.this, Options.class);
			startActivityForResult(optionsIntent, 1);
			return true;
		case 1:
			new AlertDialog.Builder(Main.this).setTitle(R.string.about_text)
					.setMessage(getString(R.string.about_shark))
					.setNeutralButton(getString(R.string.ok), null).show();
			return true;
		}
		return false;
	}

	/**
	 * Calls TCPdumpHandler to try start the packet capture.
	 */
	private void startTCPdump() {
		if (tcpDumpHandler.checkNetworkStatus()) {

			switch (tcpDumpHandler.start(parameters.getText().toString())) {
			case 0:
				Toast.makeText(Main.this, getString(R.string.tcpdump_started),
						Toast.LENGTH_SHORT).show();
				break;
			case -1:
				Toast.makeText(Main.this,
						getString(R.string.tcpdump_already_started),
						Toast.LENGTH_SHORT).show();
				break;
			case -2:
				new AlertDialog.Builder(Main.this)
						.setTitle(getString(R.string.device_not_rooted_error))
						.setMessage(
								getString(R.string.device_not_rooted_error_msg))
						.setNeutralButton(getString(R.string.ok), null).show();
				break;
			case -4:
				new AlertDialog.Builder(Main.this).setTitle("Error")
						.setMessage(getString(R.string.command_error))
						.setNeutralButton(getString(R.string.ok), null).show();
				break;
			case -5:
				new AlertDialog.Builder(Main.this).setTitle("Error")
						.setMessage(getString(R.string.outputstream_error))
						.setNeutralButton(getString(R.string.ok), null).show();
				break;
			default:
				new AlertDialog.Builder(Main.this).setTitle("Error")
						.setMessage(getString(R.string.unknown_error))
						.setNeutralButton(getString(R.string.ok), null).show();
			}
		} else {
			new AlertDialog.Builder(Main.this)
					.setTitle(getString(R.string.network_connection_error))
					.setMessage(
							getString(R.string.network_connection_error_msg))
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Settings.ACTION_WIRELESS_SETTINGS));
								}
							}).setNegativeButton(getString(R.string.no), null)
					.show();
		}
	}

	/**
	 * Calls TCPdumpHandler to try to stop the packet capture.
	 */
	private void stopTCPdump() {
		switch (tcpDumpHandler.stop()) {
		case 0:
			Toast.makeText(Main.this, getString(R.string.tcpdump_stoped),
					Toast.LENGTH_SHORT).show();
			break;
		case -1:
			Toast.makeText(Main.this,
					getString(R.string.tcpdump_already_stoped),
					Toast.LENGTH_SHORT).show();
			break;
		case -2:
			new AlertDialog.Builder(Main.this)
					.setTitle(getString(R.string.device_not_rooted_error))
					.setMessage(getString(R.string.device_not_rooted_error_msg))
					.setNeutralButton(getString(R.string.ok), null).show();
			break;
		case -4:
			new AlertDialog.Builder(Main.this).setTitle("Error")
					.setMessage(getString(R.string.command_error))
					.setNeutralButton(getString(R.string.ok), null).show();
			break;
		case -5:
			new AlertDialog.Builder(Main.this).setTitle("Error")
					.setMessage(getString(R.string.outputstream_error))
					.setNeutralButton(getString(R.string.ok), null).show();
			break;
		case -6:
			new AlertDialog.Builder(Main.this).setTitle("Error")
					.setMessage(getString(R.string.close_shell_error))
					.setNeutralButton(getString(R.string.ok), null).show();
			break;
		case -7:
			new AlertDialog.Builder(Main.this).setTitle("Error")
					.setMessage(getString(R.string.process_finish_error))
					.setNeutralButton(getString(R.string.ok), null).show();
		default:
			new AlertDialog.Builder(Main.this).setTitle("Error")
					.setMessage(getString(R.string.unknown_error))
					.setNeutralButton(getString(R.string.ok), null).show();
		}

	}

	/**
	 * Tries to launch the reader activity.
	 */
	private void launchReader() {
		readerIntent = new Intent(Main.this, Reader.class);
		if (FileManager.checkFile(GlobalConstants.dirName,
				settings.getString("fileText", "shark_capture.pcap"))) {
			if (tcpdump.getProcessStatus() == false) {
				startActivity(readerIntent);
			} else {
				new AlertDialog.Builder(Main.this)
						.setTitle(getString(R.string.capture_in_progress_error))
						.setMessage(
								getString(R.string.capture_in_progress_error_msg))
						.setPositiveButton(getString(R.string.yes),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										stopTCPdump();
										startActivity(readerIntent);
									}
								})
						.setNegativeButton(getString(R.string.no), null).show();
			}
		} else {
			new AlertDialog.Builder(Main.this)
					.setTitle(getString(R.string.file_error))
					.setMessage(getString(R.string.file_error_msg))
					.setNeutralButton(getString(R.string.ok), null).show();
		}
	}
}
