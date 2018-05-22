package hoo.etahk.view.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import hoo.etahk.R;
import hoo.etahk.view.App;
import hoo.etahk.view.search.BusSearchActivity;

public class ShortcutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent=getIntent();
        final String action=intent.getAction();

        if(Intent.ACTION_CREATE_SHORTCUT.equals(action)){
            setupShortcut();
            finish();
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ShortcutActivity.this.startActivity(new Intent(ShortcutActivity.this, BusSearchActivity.class));
                ShortcutActivity.this.finish();
            }
        }, 500);
    }

    private void setupShortcut() {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());

        // Then, set up the container intent (the response to the caller)

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, App.Companion.getInstance().getString(R.string.sc_follow_s));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this,  R.drawable.ic_shortcut_follow);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        // Now, return the result to the launcher
        setResult(RESULT_OK, intent);
    }



}