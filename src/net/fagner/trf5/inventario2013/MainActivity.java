package net.fagner.trf5.inventario2013;


import java.io.IOException;
import java.util.Date;

import net.sourceforge.zbar.Symbol;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

public class MainActivity extends Activity {
	private TimeTrackerAdapter timeTrackerAdapter;
	private TimeTrackerDatabaseHelper databaseHelper;
    public static final int TIME_ENTRY_REQUEST_CODE = 1;
    public static final int TECLADO_REQUEST_CODE = 3;
    public static final int ZBAR_SCANNER_REQUEST = 5;
    final MediaPlayer mp = new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button scan = (Button) findViewById(R.id.scan_button);
		   scan.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		        	
		        	Intent intent = new Intent(MainActivity.this, ZBarScannerActivity.class);
		        	intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.CODE128});
		        	startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
		        			        	
		        	//Intent intent = new Intent(
							//"com.google.zxing.client.android.SCAN");
					//intent.putExtra("SCAN_FORMATS", "CODE_128, ITF");
					//intent.setPackage("net.fagner.trf5.inventario2013");
					//startActivityForResult(intent, SCANNER_REQUEST_CODE);
		        }
		    });
        
        
        Button digitar = (Button) findViewById(R.id.digitar_button);
		   digitar.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		        	Intent intent = new Intent(MainActivity.this, TecladoActivity.class);
		        	startActivityForResult(intent, TECLADO_REQUEST_CODE);
		        }
		    });
        
		   Button semtombo = (Button) findViewById(R.id.stombo_button);
		   semtombo.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		        	Intent intent = new Intent(MainActivity.this, ItensActivity.class);
		        	intent.putExtra("tombo", "99999");
		        	startActivityForResult(intent, TIME_ENTRY_REQUEST_CODE);
		        }
		    });
		   
        databaseHelper = new TimeTrackerDatabaseHelper(this);
        
		TextView setorview = (TextView) findViewById(R.id.setor_view);
		TextView salaview = (TextView) findViewById(R.id.sala_view);
		TextView inventarianteview = (TextView) findViewById(R.id.inventariante_view);
		
		String[] temps=databaseHelper.buscadadosinventariada(databaseHelper.buscainventariadamaisrecente());
		setorview.setText("Setor: " + temps[0]);
		salaview.setText("Sala: " + temps[1]);
		inventarianteview.setText("Inventariante: " + temps[3]);
	        
   	    final ListView listView = (ListView)findViewById(R.id.registros_listview);
   	         
        timeTrackerAdapter = new TimeTrackerAdapter(
        		this, databaseHelper.getTimeRecordList());
      
        listView.setAdapter(timeTrackerAdapter);
        
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        	  //alert(parent.getItemAtPosition(position).toString());
           
        	  //Toast.makeText(MainActivity.this, "Você clicou no item " + listView.get(position).getID(), Toast.LENGTH_SHORT).show();

          }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
		
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        	
        final int pos = position;
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Tombo");
            builder.setMessage("Apagar o registro?");

            builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    
                	
                	// Do nothing but close the dialog
                     if (databaseHelper.deletaTombo((int) listView.getItemIdAtPosition(pos))) {
                    timeTrackerAdapter.changeCursor(
        					databaseHelper.getTimeRecordList());
        			timeTrackerAdapter.notifyDataSetChanged();}
                     
                	dialog.dismiss();
                    //databaseHelper.deletaTombo(0);
                    //timeTrackerAdapter.notifyDataSetChanged();
                }

            });

            builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
            
          }
        
        });
             
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getItemId() == R.id.item3) {
    		Intent intent = new Intent(this, SetorActivity.class);
    		startActivityForResult(intent, 25);
    		return true;
    	}
    return super.onOptionsItemSelected(item);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == TIME_ENTRY_REQUEST_CODE) {
    		if (resultCode == RESULT_OK) {
    			String tombo = data.getStringExtra("tombo");
    			String objeto = data.getStringExtra("objeto");
    			String outros = data.getStringExtra("outros");
    			String situacao = data.getStringExtra("situacao");
    			//timeTrackerAdapter.addTimeRecord( new TimeRecord(time, notes));
    			//timeTrackerAdapter.notifyDataSetChanged();
    			//Toast.makeText(this, objeto + "\n" + outros + "\n" + tombo + "\n" + situacao, Toast.LENGTH_SHORT).show();
    			Date d = new Date();
    	     	databaseHelper.saveTimeRecord(String.valueOf(DateFormat.format("hh:mm:ss", d.getTime())), String.valueOf(DateFormat.format("dd/MM/yy", d.getTime())), Integer.parseInt(objeto), outros, Integer.parseInt(tombo), situacao, databaseHelper.buscainventariadamaisrecente());
    			timeTrackerAdapter.changeCursor(
    					databaseHelper.getTimeRecordList());
    			timeTrackerAdapter.notifyDataSetChanged();
    	   		} 
    	}
    		else if (requestCode == TECLADO_REQUEST_CODE) {
    			if (resultCode == RESULT_OK) {
    			    if (databaseHelper.checaTomboExiste(Integer.parseInt(data.getStringExtra("tombo")))) {
        		    	Toast.makeText(this, "tombo existente", Toast.LENGTH_SHORT).show();
        		    }
    			   				
    			else 	{Intent intent = new Intent(MainActivity.this, ItensActivity.class);
				intent.putExtra("tombo", data.getStringExtra("tombo"));
				startActivityForResult(intent, TIME_ENTRY_REQUEST_CODE);
				}}
    		}
    	
       		else if (requestCode == ZBAR_SCANNER_REQUEST ) {
    			if (resultCode == RESULT_OK) {
    				if (databaseHelper.checaTomboExiste(Integer.parseInt(data.getStringExtra("SCAN_RESULT")))) {
        		    	Toast.makeText(this, "tombo existente", Toast.LENGTH_SHORT).show();
    				}
    				 else {
    					 try { 
    						 AssetFileDescriptor afd;
    				            afd = getAssets().openFd("beep.mp3");
    				            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
    				            mp.prepare();
    				            mp.start();
    				       } catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
    				Intent intent = new Intent(MainActivity.this, ItensActivity.class);
    				intent.putExtra("tombo", data.getStringExtra("SCAN_RESULT"));
    				startActivityForResult(intent, TIME_ENTRY_REQUEST_CODE);	
    			}}
    		}
       		else if (requestCode == 25 ) {
    			if (resultCode == RESULT_OK) {
    				TextView setorview = (TextView) findViewById(R.id.setor_view);
    				TextView salaview = (TextView) findViewById(R.id.sala_view);
    				TextView inventarianteview = (TextView) findViewById(R.id.inventariante_view);
    				setorview.setText("Setor: " + data.getStringExtra("setor"));
    				salaview.setText("Sala: " + data.getStringExtra("sala"));
    				inventarianteview.setText("Inventariante: " + data.getStringExtra("inventariante"));
    			}
    		}
    		}
    	}

