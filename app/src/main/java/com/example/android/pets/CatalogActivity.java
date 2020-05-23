package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetsDbHelper;

import static com.example.android.pets.data.PetContract.PetEntry.CONTENT_URI;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private PetsDbHelper mDbHelper;
    private static final int PET_LOADER = 0;
    PetCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        mDbHelper = new PetsDbHelper(this);
        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        //Set up item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                editIntent.setData(currentPetUri);
                startActivity(editIntent);
            }
        });
        //Kick off the loader
        getLoaderManager().initLoader(PET_LOADER,null,this);

    }

    private void insertPet(){
        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);

        Uri newUri = getContentResolver().insert(CONTENT_URI, values);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                //displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define projection that specifies the columns from the table we care about
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED};
        return new CursorLoader(this,
                CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        //Update PetCursorAdapter with this new cursor containing updated data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
    /**
     * Helper method to delete all pets in the database.
     */

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }



}
















/**
 * Temporary helper method to display information in the onscreen TextView about the state of
 * the pets database.
 */
/** private void displayDatabaseInfo() {
 // To access our database, we instantiate our subclass of SQLiteOpenHelper
 // and pass the context, which is the current activity.
 PetsDbHelper mDbHelper = new PetsDbHelper(this);

 // Create and/or open a database to read from it
 SQLiteDatabase db = mDbHelper.getReadableDatabase();

 String[] projection = {
 PetEntry._ID,
 PetEntry.COLUMN_PET_NAME,
 PetEntry.COLUMN_PET_BREED,
 PetEntry.COLUMN_PET_GENDER,
 PetEntry.COLUMN_PET_WEIGHT
 };
 Cursor cursor = db.query(
 PetEntry.TABLE_NAME,
 projection,
 null,
 null,
 null,
 null,
 null,
 null
 );


 Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null);
 //TextView displayView = (ListView) findViewById(R.id.text_view_pet);


 /**
 try {
 // Display the number of rows in the Cursor (which reflects the number of rows in the
 // pets table in the database).
 //displayView.setText("Number of rows in pets database table: " + cursor.getCount()+ "\n");
 displayView.append("\n" + PetEntry._ID
 + "-" + PetEntry.COLUMN_PET_NAME
 + "-" + PetEntry.COLUMN_PET_BREED
 + "-" + PetEntry.COLUMN_PET_GENDER
 + "-" + PetEntry.COLUMN_PET_WEIGHT + "\n");

 //Figure out the index of each column
 int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
 int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
 int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
 int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
 int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
 //Iterate through all returned rows in the cursor

 while(cursor.moveToNext()){
 //Use that index to extract string or int value of the word at the current row the cursor is on
 int currentID = cursor.getInt(idColumnIndex);
 String currentName = cursor.getString(nameColumnIndex);
 String currentBreed = cursor.getString(breedColumnIndex);
 String currentGender = cursor.getString(genderColumnIndex);
 int currentWeight = cursor.getInt(weightColumnIndex);
 //displayView.append(("\n" + currentID + "-" + currentName + "-" + currentBreed + "-" + currentGender + "-" + currentWeight));
 }
 } finally {
 // Always close the cursor when you're done reading from it. This releases all its
 // resources and makes it invalid.
 cursor.close();
 }


 petListView.setAdapter(adapter);
 }



 @Override
 protected void onStart() {
 super.onStart();
 //displayDatabaseInfo();
 }
 **/