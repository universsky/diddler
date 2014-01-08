/**
 * This file is part of Diddler.
 * 
 * Diddler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Diddler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Diddler.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Sergio Jiménez Feijóo (sergio.jf89@gmail.com)
 */

package universsky.diddler;

import java.util.List;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Options extends Activity {

	// Variable declarations for handling the view items in the layout.
	private Spinner interface_spinner;
	private Spinner verbose_spinner;
	private CheckBox verbose_checkbox;
	private CheckBox snaplen_checkbox;
	private CheckBox save_checkbox;
	private CheckBox promisc_checkbox;
	private EditText snaplen_text;
	private EditText file_text;
	private Button save_button;
	private Button dischard_button;

	// Variable declarations for handling the return to the main activity.
	private Intent mainIntent = null;

	// Variable declarations for handling the preferences.
	private SharedPreferences settings = null;
	private SharedPreferences.Editor editor = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);

		// Associating the items in the view to the variables.
		interface_spinner = (Spinner) findViewById(R.id.interface_spinner);
		verbose_spinner = (Spinner) findViewById(R.id.verbose_spinner);
		verbose_checkbox = (CheckBox) findViewById(R.id.verbose_checkbox);
		snaplen_checkbox = (CheckBox) findViewById(R.id.snaplen_checkbox);
		promisc_checkbox = (CheckBox) findViewById(R.id.promisc_checkbox);
		save_checkbox = (CheckBox) findViewById(R.id.save_checkbox);
		snaplen_text = (EditText) findViewById(R.id.snaplen_text);
		file_text = (EditText) findViewById(R.id.file_text);
		save_button = (Button) findViewById(R.id.save_button);
		dischard_button = (Button) findViewById(R.id.dischard_button);

		mainIntent = getIntent();

		settings = getSharedPreferences(GlobalConstants.prefsName, 0);
		editor = settings.edit();

		List<TCPdumpInterface> ifList;

		if ((ifList = TCPdumpInterface.listInterfaces(true)) != null) {
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item,
					TCPdumpInterface.getIfnames(ifList));
			interface_spinner.setAdapter(spinnerArrayAdapter);
		}

		restorePreferences();

		// Setting the action to perform when an interface is selected.
		interface_spinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if ((interface_spinner.getSelectedItemPosition() == 0)
								|| (interface_spinner.getSelectedItemPosition() == 1)) {
							promisc_checkbox.setEnabled(false);
						} else {
							promisc_checkbox.setEnabled(true);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
				});

		// Setting the action to perform when the verbose CheckBox status is
		// changed.
		verbose_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked == true) {
							verbose_spinner.setEnabled(true);
						} else {
							verbose_spinner.setEnabled(false);
						}
					}
				});

		// Setting the action to perform when the snaplen CheckBox status is
		// changed.
		snaplen_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked == true) {
							snaplen_text.setEnabled(true);
						} else {
							snaplen_text.setEnabled(false);
						}
					}
				});

		// Setting the action to perform when the save CheckBox status is
		// changed.
		save_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == true) {
					file_text.setEnabled(true);
				} else {
					file_text.setEnabled(false);
				}
			}
		});

		save_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((save_checkbox.isChecked())
						&& (file_text.getText().toString().length() == 0)) {
					new AlertDialog.Builder(Options.this)
							.setTitle("No filename especified")
							.setMessage(
									"Diddler has detected that you want to save the packets to a file but the filename you entered is empty.")
							.setNeutralButton("Ok", null).show();
				} else {
					savePreferences();
					setResult(RESULT_OK, mainIntent);
					finish();
				}
			}
		});

		dischard_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, mainIntent);
				finish();
			}
		});
	}

	private void restorePreferences() {

		// Restore preferences. If they aren't set the default values are
		// used instead.

		if (settings.getInt("selectedInterface", 0) + 1 > interface_spinner
				.getCount()) {
			interface_spinner.setSelection(0);

		} else
			interface_spinner.setSelection(settings.getInt("selectedInterface",
					0));

		verbose_checkbox.setChecked(settings.getBoolean("verboseCheckbox",
				false));

		verbose_spinner.setSelection(settings.getInt("verboseLevel", 0));

		snaplen_checkbox.setChecked(settings.getBoolean("snaplenCheckbox",
				false));

		snaplen_text.setText(Integer.toString(settings
				.getInt("snaplenValue", 0)));

		promisc_checkbox.setChecked(settings.getBoolean("promiscCheckbox",
				false));

		save_checkbox.setChecked(settings.getBoolean("saveCheckbox", false));

		file_text.setText(settings.getString("fileText", "diddler_capture.pcap"));

		if (interface_spinner.getSelectedItemPosition() != 1)
			promisc_checkbox.setEnabled(false);

		if (verbose_checkbox.isChecked() == false)
			verbose_spinner.setEnabled(false);

		if (snaplen_checkbox.isChecked() == false)
			snaplen_text.setEnabled(false);

		if (save_checkbox.isChecked() == false)
			file_text.setEnabled(false);
	}

	private void savePreferences() {

		// Saving the preferences.
		editor.putInt("selectedInterface",
				interface_spinner.getSelectedItemPosition());

		editor.putBoolean("verboseCheckbox", verbose_checkbox.isChecked());

		editor.putInt("verboseLevel", verbose_spinner.getSelectedItemPosition());

		editor.putBoolean("snaplenCheckbox", snaplen_checkbox.isChecked());

		editor.putInt("snaplenValue",
				Integer.parseInt(snaplen_text.getText().toString()));

		editor.putBoolean("promiscCheckbox", promisc_checkbox.isChecked());

		editor.putBoolean("saveCheckbox", save_checkbox.isChecked());

		editor.putString("fileText", file_text.getText().toString());

		editor.commit();
	}
}